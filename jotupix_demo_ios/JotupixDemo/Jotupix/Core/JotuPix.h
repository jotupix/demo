//
//  JotuPix.h
//

#import <Foundation/Foundation.h>
#import "JDefine.h"
#import "JProtocol.h"
#import "JLzss.h"
#import "JPacket.h"
#import "JTick.h"
#import "JInfo.h"

NS_ASSUME_NONNULL_BEGIN

#pragma mark - Program Send Callback

/// Program sending callback status (completed / progress / fail)
typedef NS_ENUM(NSUInteger, JSendStatus) {
    JSendStatusCompleted = 0,
    JSendStatusProgress,
    JSendStatusFail,
};

@protocol IJSendProgramCallback <NSObject>

/// Callback for program transmission status updates
- (void)onEvent:(JSendStatus)status percent:(uint8_t)percent;

@end


#pragma mark - Program Group Base

/// Base class for program group parameters
typedef NS_ENUM(NSUInteger, JProgramGroupType) {
    JProgramGroupTypeNOR = 0,
};

@interface JProgramGroupBase : NSObject

/// Group type (currently only NOR)
@property(nonatomic, assign) JProgramGroupType groupType;

@end


#pragma mark - Device Info Callback

@protocol IJGetDevInfoCallback <NSObject>

/// Callback for receiving device info
- (void)onEvent:(JInfo *)devInfo;

@end


#pragma mark - NOR Program Group

/// Play type for NOR program group
typedef NS_ENUM(NSUInteger, JProgramGroupNorPlayType) {
    JProgramGroupNorPlayTypeCount = 0,     /// Play by count
    JProgramGroupNorPlayTypeDuration,      /// Play by duration
    JProgramGroupNorPlayTypeTime,          /// Play by clock time
};

@interface JProgramGroupNor : JProgramGroupBase

/// Play mode
@property(nonatomic, assign) JProgramGroupNorPlayType playType;

/// Play parameter (count / seconds / etc.)
@property(nonatomic, assign) uint32_t playParam;

@end


#pragma mark - Program Info

/// Program metadata information
typedef NS_ENUM(NSUInteger, JCompressFlag) {
    JCompressFlagDo = 0,    /// Enable compression
    JCompressFlagUndo,      /// No compression
};

@interface JProgramInfo : NSObject

/// Program index
@property(nonatomic, assign) uint8_t proIndex;

/// Total number of programs
@property(nonatomic, assign) uint8_t proAllNum;

/// Compression flag
@property(nonatomic, assign) JCompressFlag compressFlag;

/// Group parameter object (NOR group)
@property(nonatomic, strong) JProgramGroupBase *groupParam;

@end


#pragma mark - ProgramSender (Internal Struct)

/// Internal program sending state container
@interface JProgramSender : NSObject

/// Whether sending is active
@property(nonatomic, assign) BOOL sendStatus;

/// Callback reference
@property(nonatomic, weak, nullable) id<IJSendProgramCallback> callback;

/// Packet ID (auto-increment)
@property(nonatomic, assign) uint16_t pktId;

/// Timeout tick for sending
@property(nonatomic, assign) uint32_t timeoutTick;

/// Retry count
@property(nonatomic, assign) uint8_t retryCnt;

/// Current packet payload size
@property(nonatomic, assign) uint16_t currPktPayloadLen;

/// Program metadata
@property(nonatomic, strong) JProgramInfo *programInfo;

/// Total sent length
@property(nonatomic, assign) uint32_t currSendLen;

/// Program data buffer (compressed or raw)
@property(nonatomic, strong) NSMutableData *proData;

/// Original CRC
@property(nonatomic, assign) uint32_t proOrigCRC;

/// Original data length
@property(nonatomic, assign) uint32_t proOrigLen;

@end


#pragma mark - JotuPix

/// Main communication and program transmission class
typedef NS_ENUM(NSUInteger, JAction) {
    JActionUnknown = 0,

    JActionMusic = 0x01,
    JActionStartSetProgram,
    JActionSetProgram,
    JActionSetBrightness,
    JActionSetSwitchStatus,
    JActionSetLocalMusic,
    JActionPlayProgramByIndex,
    JActionDelProgramByIndex,
    JActionUpdateTime,
    JActionSetTimers,
    JActionGetTimers,
    JActionSetFlip,

    JActionCheckPassword,
    JActionSetPassword,
    JActionOperateCountdown = 0x0F,
    JActionOperateStopwatch = 0x10,
    JActionOperateScoreboard = 0x11,
    JActionSetGraphics = 0x12,
    JActionOperateLight = 0x13,
    JActionSetDevInfo = 0x1E,
    JActionGetDevInfo = 0x1F,
};

@interface JotuPix : NSObject <IJParseCallback>

#pragma mark - Init

/// Initialize protocol with a sender object
- (void)init:(id<IJSend>)sender;

#pragma mark - Tick

/// UI-thread periodic tick for timeout handling
- (void)tick:(uint32_t)currMsTick;

#pragma mark - Parse

/// Parse received raw bytes
- (void)parseRecvData:(const uint8_t *)data len:(uint32_t)len;

#pragma mark - Command Sender

/// Send raw command to protocol
- (int)sendCommand:(const uint8_t *)data len:(uint32_t)len;

#pragma mark - Program Sending

/// Start program transmission
- (int)sendProgram:(JProgramInfo *)info
              data:(const uint8_t *)programData
            length:(uint32_t)dataLen
          callback:(id<IJSendProgramCallback>)callback;

/// Cancel ongoing program sending
- (int)cancelSendProgram;

#pragma mark - Simple Commands

/// Set switch ON/OFF
- (int)sendSwitchStatus:(uint8_t)status;

/// Set brightness
- (int)sendBrightness:(uint8_t)brightness;

/// Set screen flip
- (int)sendScreenFlip:(uint8_t)flip;

/// Reset command (not implemented)
- (int)sendReset;

#pragma mark - Device Info

/// Request device info
- (int)getDevInfo:(id<IJGetDevInfoCallback>)callback;

/// Send startup-screen command
- (int)sendStartupScreen;

#pragma mark - Parse Complete Callback

/// Protocol parse complete callback
- (void)onParseComplete:(const uint8_t *)data length:(uint32_t)len;

@end

NS_ASSUME_NONNULL_END
