//
//  JotuPix.m
//

#import "JotuPix.h"
#import "JByteWriter.h"
#import "JCrc.h"
#import "JLog.h"

#define TAG  "JotuPix"

/// Program sending timeout (ms)
#define SEND_PRO_TIMEOUT_TICK       (5 * 1000)

/// Max payload size per packet
#define DEFAULT_PKT_MAX_SIZE        1024

/// Max retry count for failed packet
#define RETRY_MAX_CNT               3

/// Max device-name length
#define MAX_NAME_LEN                20

#pragma mark - ProgramGroupBase

@implementation JProgramGroupBase
@end

#pragma mark - ProgramGroupNor

@implementation JProgramGroupNor

/// Initialize NOR group with default play settings
- (instancetype)init {
    if (self = [super init]) {
        self.groupType = JProgramGroupTypeNOR;
        _playType = JProgramGroupNorPlayTypeCount;
        _playParam = 1;
    }
    return self;
}

@end

#pragma mark - ProgramInfo

@implementation JProgramInfo
@end


#pragma mark - ProgramSender

@implementation JProgramSender

/// Initialize internal program-sending state container
- (instancetype)init {
    if (self = [super init]) {
        _programInfo = [[JProgramInfo alloc] init];
        _proData = [[NSMutableData alloc] init];
    }
    return self;
}

@end


#pragma mark - JotuPix

@interface JotuPix () {
    JLzss *_lzss;           /// LZSS compressor instance
    JProtocol *_protocol;   /// Protocol handler
    JPacket *_packet;       /// Packet builder

    JProgramSender *_sender;    /// Internal sending state
    uint32_t _currTick;         /// Current system tick (ms)

    /// Device-info callback reference
    __weak id<IJGetDevInfoCallback> _devInfoCallback;
}
@end


@implementation JotuPix

#pragma mark - Init

/// Constructor: initialize core components
- (instancetype)init {
    if (self = [super init]) {
        _lzss = [[JLzss alloc] init];
        _protocol = [[JProtocol alloc] init];
        _packet = [[JPacket alloc] init];
        _sender = [[JProgramSender alloc] init];
        _currTick = 0;
    }
    return self;
}

/// Bind protocol sender and parse-callback
- (void)init:(id<IJSend>)sender {
    [_protocol init:sender callback:self];
}

#pragma mark - Parse

/// Feed raw data to protocol
- (void)parseRecvData:(const uint8_t *)data len:(uint32_t)len {
    [_protocol parse:data length:len];
}

#pragma mark - Tick

/// Tick function used for timeout detection
- (void)tick:(uint32_t)currMsTick {
    _currTick = currMsTick;
    
    /// If sending is active, check timeout
    if (_sender.sendStatus) {
        if (time_after_eq(_currTick, _sender.timeoutTick)) {
            _sender.sendStatus = NO;
            JLogE(TAG, "Program transmission timed out.\n");
            
            if (_sender.callback) {
                [_sender.callback onEvent:JSendStatusFail percent:0];
            }
        }
    }
}

#pragma mark - Print Hex

/// Print data buffer in hex for debugging
- (void)printLogArrToHex:(NSString *)title data:(const uint8_t *)data len:(uint32_t)len {
    if (title) {
        JPrint("%s", title.UTF8String);
    }
    for (uint32_t i = 0; i < len; i++) {
        JPrint("%02x ", data[i]);
    }
    JPrint("\n");
}

#pragma mark - Send Command

/// Send raw command bytes
- (int)sendCommand:(const uint8_t *)data len:(uint32_t)len {
    return [_protocol send:data length:len];
}

#pragma mark - Send Program (Entry)

/// Start program transmission
- (int)sendProgram:(JProgramInfo *)info
              data:(const uint8_t *)programData
            length:(uint32_t)dataLen
          callback:(id<IJSendProgramCallback>)callback
{
    /// Validate input
    if (!info || !programData || dataLen == 0) {
        JLogE(TAG, "SendProgram fail, invalid params\n");
        return -1;
    }
    
    /// Reset sending state
    _sender.sendStatus = NO;
    _sender.callback = nil;
    _sender.pktId = 0;
    _sender.timeoutTick = 0;
    _sender.retryCnt = 0;
    _sender.currPktPayloadLen = 0;
    _sender.currSendLen = 0;
    _sender.proData.length = 0;
    
    /// Copy metadata
    _sender.programInfo.proIndex     = info.proIndex;
    _sender.programInfo.proAllNum    = info.proAllNum;
    _sender.programInfo.compressFlag = info.compressFlag;
    _sender.programInfo.groupParam   = info.groupParam;
    
    /// Original length
    _sender.proOrigLen = dataLen;
    
    /// Compute CRC
    JCrc *crc = [[JCrc alloc] init];
    [crc reset];
    _sender.proOrigCRC = [crc calculateWithBytes:programData length:dataLen];
    
    /// Compress if needed
    if (info.compressFlag == JCompressFlagDo) {
        NSData *compressed = [_lzss encode:programData length:dataLen];
        [_sender.proData appendData:compressed];
    } else {
        [_sender.proData appendBytes:programData length:dataLen];
    }
    
    /// Save callback
    _sender.callback = callback;
    
    /// Set timeout tick
    _sender.timeoutTick = time_get_next_tick(_currTick, SEND_PRO_TIMEOUT_TICK);
    _sender.sendStatus = YES;
    
    /// Log metadata
    JLogD(TAG, "Program info:\n");
    JPrint(" -index: %d\n", info.proIndex);
    JPrint(" -all num: %d\n", info.proAllNum);
    JPrint(" -compress flag: %d\n", info.compressFlag);
    JPrint(" -size: %d\n", dataLen);
    JPrint(" -crc: 0x%x\n", _sender.proOrigCRC);
    
    /// Send first header packet
    int ret = [self sendProgramStart];
    if (ret != 0) {
        JLogE(TAG, "Send program start fail\n");
        [self cancelSendProgram];
        return -1;
    }
    
    /// Notify progress = 0
    if (_sender.callback) {
        [_sender.callback onEvent:JSendStatusProgress percent:0];
    }
    
    return 0;
}

#pragma mark - sendProgramStart (First Frame)

/// Build and send the first “start program” packet
- (int)sendProgramStart
{
    JByteWriter *bw = [[JByteWriter alloc] init];
    
    JProgramInfo *info = _sender.programInfo;
    JProgramGroupBase *group = info.groupParam;
    
    /// Command: Start setting program content
    [bw put_u8:JActionStartSetProgram];
    
    /// CRC + original data length
    [bw put_u32:_sender.proOrigCRC];
    [bw put_u32:_sender.proOrigLen];
    
    /// Program index and total number
    [bw put_u8:info.proIndex];
    [bw put_u8:info.proAllNum];
    
    /// Default parameter = 1
    [bw put_u8:1];
    
    /// Reserved bytes (7 zeros)
    [bw put_repeat:0 count:7];
    
    /// Compression flag
    [bw put_u8:info.compressFlag];
    
    /// Group type (NOR only)
    [bw put_u8:group.groupType];
    
    /// Group parameters
    switch (group.groupType)
    {
        case JProgramGroupTypeNOR: {
            /// NOR: play type + play parameter
            JProgramGroupNor *nor = (JProgramGroupNor *)group;
            [bw put_u8:nor.playType];
            [bw put_u32:nor.playParam];
        } break;
            
        default:
            JLogE(TAG, "Unsupported group type\n");
            return -1;
    }
    
    JLogD(TAG, "Send Start Program Setup Command.\n");
    
    return [_protocol send:bw.buffer length:(uint32_t)bw.size];
}

#pragma mark - Send Next Packet

/**
 * Send next chunk of program data
 *
 * <0 fail
 * =0 complete
 * >0 payload length sent
 */
- (int)iSendProgramNextPacket {
    int ret = -1;
    
    /// ===== Progress reporting =====
    if (_sender.callback) {
        if (_sender.proData.length == 0) {
            /// No data case (should not happen)
            JLogD(TAG, "No data to send.\n");
            [_sender.callback onEvent:JSendStatusCompleted percent:100];
            return 0;
        }
        
        if (_sender.currSendLen == _sender.proData.length) {
            /// All data sent successfully
            _sender.sendStatus = NO;
            JLogD(TAG, "Send program success.\n");
            
            [_sender.callback onEvent:JSendStatusCompleted percent:100];
            return 0;
        } else {
            /// Progress = sent / total
            uint8_t percent =
            (_sender.currSendLen * 100) / _sender.proData.length;
            
            [_sender.callback onEvent:JSendStatusProgress percent:percent];
        }
    }
    
    /// ===== Determine current packet length =====
    uint32_t remain = (uint32_t)(_sender.proData.length - _sender.currSendLen);
    
    if (remain > DEFAULT_PKT_MAX_SIZE) {
        _sender.currPktPayloadLen = DEFAULT_PKT_MAX_SIZE;
    } else {
        _sender.currPktPayloadLen = (uint16_t)remain;
    }
    
    JLogD(TAG,
          "Send packet id=%d len=%d\n",
          _sender.pktId,
          _sender.currPktPayloadLen);
    
    /// ===== Build JPacketData =====
    JPacketData pkt = {0}; /// initialized to 0
    pkt.msgType = JActionSetProgram;              /// Command type
    pkt.packetId = _sender.pktId;                 /// Sequence number
    pkt.allDataLen = (uint32_t)_sender.proData.length;    /// Total length
    pkt.transType = JPacketTransType_LEN;         /// Transmission type
    pkt.transCompleteFlag = 0;                    /// More to come
    
    /// Payload pointer
    pkt.payload =
    (const uint8_t *)(_sender.proData.bytes + _sender.currSendLen);
    pkt.payloadLen = _sender.currPktPayloadLen;
    
    /// ===== Send packet =====
    ret = [_packet send:&pkt protocol:_protocol];
    if (ret < 0) {
        JLogD(TAG, "Failed to send packet id=%d\n", _sender.pktId);
        return -1;
    }
    
    /// ===== Update sending progress =====
    _sender.currSendLen += _sender.currPktPayloadLen;
    
    /// Update timeout timestamp
    _sender.timeoutTick = time_get_next_tick(_currTick, SEND_PRO_TIMEOUT_TICK);
    
    return _sender.currPktPayloadLen;
}

#pragma mark - Retry Sending Program

/// Reset packet state and resend from beginning
- (void)iRetrySendProgram
{
    JLogD(TAG, "Retry send program, cnt=%d\n", _sender.retryCnt);
    
    /// Reset to first packet
    _sender.pktId = 0;
    _sender.currSendLen = 0;
    
    [self iSendProgramNextPacket];
}

#pragma mark - Cancel Program

/// Cancel transmission
- (int)cancelSendProgram
{
    _sender.sendStatus = NO;
    _sender.proData.length = 0;
    _sender.programInfo.groupParam = nil;
    
    return 0;
}

#pragma mark - Simple Commands

/// Send switch ON/OFF command
- (int)sendSwitchStatus:(uint8_t)status
{
    uint8_t buf[2] = {0};
    buf[0] = JActionSetSwitchStatus;
    buf[1] = status;
    return [_protocol send:buf length:2];
}

/// Send brightness (0–100 or device-defined range)
- (int)sendBrightness:(uint8_t)brightness
{
    uint8_t buf[2] = {0};
    buf[0] = JActionSetBrightness;
    buf[1] = brightness;
    return [_protocol send:buf length:2];
}

/// Send screen flip command
- (int)sendScreenFlip:(uint8_t)flip
{
    uint8_t buf[2] = {0};
    buf[0] = JActionSetFlip;
    buf[1] = flip;
    return [_protocol send:buf length:2];
}

/// Reset command (not implemented)
- (int)sendReset
{
    return -1;
}


#pragma mark - Device Info

/// Request device info from device
- (int)getDevInfo:(id<IJGetDevInfoCallback>)callback
{
    if (!callback) {
        JLogE(TAG, "GetDevInfo fail, callback null\n");
        return -1;
    }

    _devInfoCallback = callback;

    uint8_t cmd = JActionGetDevInfo;
    return [_protocol send:&cmd length:1];
}


#pragma mark - Startup Screen

/// Send startup-screen command to device
- (int)sendStartupScreen
{
    uint8_t buf[3] = {0};
    buf[0] = 0x23;
    buf[1] = 0x01;
    buf[2] = 0x00;

    return [_protocol send:buf length:3];
}

#pragma mark - Parse Complete

/// Protocol parse-complete callback
- (void)onParseComplete:(const uint8_t *)data length:(uint32_t)len
{
    if (!data || len == 0) return;

    /// Extract message type
    JAction msgType = (JAction)data[0];
    [self printLogArrToHex:@"parse: " data:data len:len];

    switch (msgType)
    {
        // ======================================================
        // Start Set Program (First ACK)
        // ======================================================

        case JActionStartSetProgram:
            if (_sender.sendStatus) {

                if (data[1] == 0) {
                    /// ACK: continue sending next data packet
                    [self iSendProgramNextPacket];

                } else if (data[1] == 1) {
                    /// ACK: device already has this program
                    _sender.sendStatus = NO;
                    if (_sender.callback) {
                        [_sender.callback onEvent:JSendStatusCompleted percent:100];
                    }

                } else {
                    /// Error during start-setting
                    _sender.sendStatus = NO;
                    if (_sender.callback) {
                        [_sender.callback onEvent:JSendStatusFail percent:0];
                    }
                }
            }
            break;

        // ======================================================
        // Program Packet Acknowledgment
        // ======================================================

        case JActionSetProgram: {
            if (_sender.sendStatus) {
                uint8_t retCode = data[4];

                if (retCode != 0) {
                    /// Packet failed -> retry or fail
                    _sender.sendStatus = NO;
                    _sender.retryCnt++;

                    if (_sender.retryCnt > RETRY_MAX_CNT) {
                        _sender.retryCnt = 0;
                        if (_sender.callback) {
                            [_sender.callback onEvent:JSendStatusFail percent:0];
                        }
                        break;
                    }

                    /// Retry from start of program
                    [self iRetrySendProgram];
                } else {
                    /// Packet succeeded → send next packet
                    _sender.pktId++;
                    [self iSendProgramNextPacket];
                }
            }
        } break;

        // ======================================================
        // Simple command responses
        // ======================================================

        case JActionSetSwitchStatus:
            break;

        case JActionSetFlip:
            break;

        case JActionSetBrightness:
            break;


        // ======================================================
        // Device Info Response
        // ======================================================

        case JActionGetDevInfo:
        {
            /// Parse JInfo fields
            JInfo *info = [[JInfo alloc] init];

            info.switchStatus    = data[1];
            info.bn              = data[2];
            info.flip            = data[3];
            info.supportLocalMic = data[4];
            info.localMicStatus  = data[5];
            info.localMicMode    = data[6];
            info.enableShowId    = data[7];
            info.proMaxNum       = data[8];
            info.enableRemote    = data[9];
            info.timerMaxNum     = data[10];
            info.devType         = data[11];

            /// Project code (4 bytes BE)
            info.projectCode =
                ((uint32_t)data[12] << 24) |
                ((uint32_t)data[13] << 16) |
                ((uint32_t)data[14] << 8 ) |
                ((uint32_t)data[15] << 0 );

            /// Version (2 bytes BE)
            info.version =
                ((uint16_t)data[16] << 8) |
                ((uint16_t)data[17] << 0);

            /// Packet max size (optional)
            if (len > 20) {
                info.pktMaxSize =
                    ((uint16_t)data[19] << 8) |
                    ((uint16_t)data[20] << 0);
            } else {
                info.pktMaxSize = DEFAULT_PKT_MAX_SIZE;
            }

            /// Device ID (optional)
            if (len > 22) {
                info.devId =
                    ((uint16_t)data[22] << 8) |
                    ((uint16_t)data[21] << 0);
            }

            /// Resolution width (optional)
            if (len > 24) {
                info.devWidth =
                    ((uint16_t)data[23] << 8) |
                    ((uint16_t)data[24] << 0);
            }

            /// Resolution height (optional)
            if (len > 26) {
                info.devHeight =
                    ((uint16_t)data[25] << 8) |
                    ((uint16_t)data[26] << 0);
            }

            /// Device name (optional)
            if (len > 27) {
                uint8_t nameLen = data[27];
                if (nameLen > MAX_NAME_LEN)
                    nameLen = MAX_NAME_LEN;

                info.devName = [[NSString alloc]
                                 initWithBytes:data + 28
                                        length:nameLen
                                      encoding:NSUTF8StringEncoding];
            }

            /// Return to callback
            if (_devInfoCallback) {
                [_devInfoCallback onEvent:info];
            }
        }
        break;

        // ======================================================
        // Ignore others
        // ======================================================

        default:
            break;
    }
}

@end

