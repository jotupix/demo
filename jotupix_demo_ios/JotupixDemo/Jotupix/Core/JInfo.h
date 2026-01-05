// JInfo.h
#import <Foundation/Foundation.h>
#import <stdint.h>

@interface JInfo : NSObject

@property (nonatomic, assign) uint8_t   switchStatus;
@property (nonatomic, assign) uint8_t   bn;
@property (nonatomic, assign) uint8_t   flip;
@property (nonatomic, assign) uint8_t   supportLocalMic;
@property (nonatomic, assign) uint8_t   localMicStatus;
@property (nonatomic, assign) uint8_t   localMicMode;
@property (nonatomic, assign) uint8_t   enableShowId;
@property (nonatomic, assign) uint8_t   proMaxNum;
@property (nonatomic, assign) uint8_t   enableRemote;
@property (nonatomic, assign) uint8_t   timerMaxNum;
@property (nonatomic, assign) uint8_t   devType;

@property (nonatomic, assign) uint32_t  projectCode;
@property (nonatomic, assign) uint16_t  version;
@property (nonatomic, assign) uint8_t   developerFlag;
@property (nonatomic, assign) uint16_t  pktMaxSize;
@property (nonatomic, assign) uint16_t  devId;
@property (nonatomic, assign) uint16_t  devWidth;
@property (nonatomic, assign) uint16_t  devHeight;

@property (nonatomic, copy)   NSString *devName;

@end
