#import "JDeviceManager.h"
#import "JotuPix.h"
#import "JInfo.h"
#import "JProtocol.h"

@interface JDeviceManager ()
{
    JotuPix *_device;
    JInfo *_devInfoCache;
}
@end

@implementation JDeviceManager

+ (instancetype)sharedInstance {
    static JDeviceManager *instance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[JDeviceManager alloc] initPrivate];
    });
    return instance;
}

- (instancetype)initPrivate {
    self = [super init];
    if (self) {

    }
    return self;
}

#pragma mark - Methods

- (void)createDevice:(id<IJSend>)sender {
    JotuPix *dev = [[JotuPix alloc] init];
    [dev init:sender];  
    _device = dev;
}

- (void)removeDevice {
    _device = nil;
}

- (JotuPix *)getDevice {
    return _device;
}

- (void)setDeviceInfoCache:(JInfo *)info {
    _devInfoCache = info;
}

- (JInfo *)getDeviceInfoCache {
    return _devInfoCache;
}

@end
