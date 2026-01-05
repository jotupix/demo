// JProtocol.m
#import "JProtocol.h"
#import "JLog.h"

#define TAG     "JProtocol"

// Protocol control characters
#define PROTOCOL_START_CHAR           0x01    // Start marker
#define PROTOCOL_ESC_CHAR             0x02    // Escape marker
#define PROTOCOL_ESC_XOR_CHAR         0x04    // Escape XOR mask
#define PROTOCOL_END_CHAR             0x03    // End marker

#define PROTOCOL_SEND_MAX_LEN         65535   // Max send length

typedef NS_ENUM(uint8_t, PROTOCOL_RECV_STATUS_E)
{
    E_PROTOCOL_RECV_STATUS_DATA = 0,      // Currently receiving actual data bytes
    E_PROTOCOL_RECV_STATUS_START,         // Waiting for next frame start
    E_PROTOCOL_RECV_STATUS_DATALEN_H,     // Receiving data length high byte
    E_PROTOCOL_RECV_STATUS_DATALEN_L      // Receiving data length low byte
};

@interface JProtocol () {
    // tx buffer and sender
    id<IJSend> _sender;
    uint8_t _txBuffer[JPROTOCOL_TX_BUFF_SIZE];

    // rx state variables
    id<IJParseCallback> _callback;
    uint8_t _parseBuffer[JPROTOCOL_RX_PARSE_BUFF_SIZE];
    uint8_t _recvStatus;
    uint16_t _recvIndex;
    uint16_t _parseDataLen;
    BOOL _inEsc;
}
@end

@implementation JProtocol

#pragma mark - Init

/// Initialize protocol with sender and callback
- (void)init:(id<IJSend>)sender callback:(id<IJParseCallback>)callback {
    _sender = sender;
    _callback = callback;
}

#pragma mark - Send

/// Pack and send data using start/length/escape/end protocol
- (int)send:(const uint8_t *)pu8Data length:(uint32_t)len {
    if (!_sender) {
        JLogE(TAG, "Send data fail, send is NULL\r\n");
        return -1;
    }
    if (!pu8Data || len == 0 || len > PROTOCOL_SEND_MAX_LEN) {
        JLogE(TAG, "Send data fail, input error\r\n");
        return -1;
    }

    uint16_t index = 0;

    _txBuffer[index++] = PROTOCOL_START_CHAR;  // Start marker

    // High byte of length
    uint8_t tmp = (len >> 8) & 0xFF;
    [self xorAndAppend:tmp index:&index];

    // Low byte of length
    tmp = len & 0xFF;
    [self xorAndAppend:tmp index:&index];

    // Append data with escape processing
    for (uint32_t i = 0; i < len; i++) {
        [self xorAndAppend:pu8Data[i] index:&index];

        // Flush partial buffer if near full
        if (index > JPROTOCOL_TX_BUFF_SIZE - 2) {
            int ret = [_sender send:_txBuffer length:index];
            if (ret != 0) return -1;
            index = 0;
        }
    }

    _txBuffer[index++] = PROTOCOL_END_CHAR;   // End marker

    return [_sender send:_txBuffer length:index];
}

/// Escape byte handler
- (void)xorAndAppend:(uint8_t)value index:(uint16_t *)index {
    if (value > 0 && value < PROTOCOL_ESC_XOR_CHAR) {
        _txBuffer[(*index)++] = PROTOCOL_ESC_CHAR;            // escape prefix
        _txBuffer[(*index)++] = (value | PROTOCOL_ESC_XOR_CHAR); // escaped value
    } else {
        _txBuffer[(*index)++] = value;
    }
}

#pragma mark - Parse

/// Parse incoming raw data stream using protocol state machine
- (void)parse:(const uint8_t *)pu8Data length:(uint32_t)len {
    if (!pu8Data || len == 0) {
        JLogE(TAG, "Parse data fail, input error\r\n");
        return;
    }

    for (uint32_t i = 0; i < len; i++) {
        uint8_t byte = pu8Data[i];

        switch (byte) {

            case PROTOCOL_START_CHAR:
                // Reset state for a new frame
                _parseDataLen = 0;
                _recvIndex = 0;
                _inEsc = NO;
                _recvStatus = E_PROTOCOL_RECV_STATUS_DATALEN_H;
                break;

            case PROTOCOL_ESC_CHAR:
                _inEsc = YES;   // Next byte is escaped
                break;

            case PROTOCOL_END_CHAR:
                // End of frame → validate + callback
                if (_recvStatus == E_PROTOCOL_RECV_STATUS_DATA &&
                    _parseDataLen > 0 &&
                    _parseDataLen <= JPROTOCOL_RX_PARSE_BUFF_SIZE) {

                    if (_callback) {
                        // Notify upper layer
                        [_callback onParseComplete:_parseBuffer length:_parseDataLen];
                    }
                }
                _recvStatus = E_PROTOCOL_RECV_STATUS_START;
                break;

            default:
                // Handle escape transformation
                if (_inEsc) {
                    byte ^= PROTOCOL_ESC_XOR_CHAR; // XOR unescape 
                    _inEsc = NO;
                }

                switch (_recvStatus) {

                    case E_PROTOCOL_RECV_STATUS_DATA:
                        // Collect payload bytes
                        if (_recvIndex < _parseDataLen) {
                            _parseBuffer[_recvIndex++] = byte;
                        }
                        break;

                    case E_PROTOCOL_RECV_STATUS_DATALEN_H:
                        // Store high length byte
                        _parseDataLen = (byte << 8);
                        _recvStatus = E_PROTOCOL_RECV_STATUS_DATALEN_L;
                        break;

                    case E_PROTOCOL_RECV_STATUS_DATALEN_L:
                        // Store low length byte + validate range
                        _parseDataLen |= byte;

                        if (_parseDataLen == 0 ||
                            _parseDataLen > JPROTOCOL_RX_PARSE_BUFF_SIZE) {
                            // Invalid length, reset
                            _parseDataLen = 0;
                            _recvStatus = E_PROTOCOL_RECV_STATUS_START;
                        } else {
                            // Ready to receive payload
                            _recvIndex = 0;
                            _recvStatus = E_PROTOCOL_RECV_STATUS_DATA;
                        }
                        break;

                    default:
                        break;
                }
                break;
        }
    }
}

@end
