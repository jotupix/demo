#import <Foundation/Foundation.h>
#import "JotuPix.h"

#define DeviceManager  [JDeviceManager sharedInstance]

@class JotuPix;
@class JInfo;
@protocol IJSend;

@interface JDeviceManager : NSObject

// Singleton
+ (instancetype)sharedInstance;

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

- (void)createDevice:(id<IJSend>)sender;
- (void)removeDevice;
- (JotuPix *)getDevice;

- (void)setDeviceInfoCache:(JInfo *)info;
- (JInfo *)getDeviceInfoCache;

@end
