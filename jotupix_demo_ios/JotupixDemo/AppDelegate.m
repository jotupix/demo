//
//  AppDelegate.m
//  SDKTest
//
//  Created by go on 4/21/25.
//

#import "AppDelegate.h"
#import "BaseVC.h"
#import "JLog.h"
#import "JDeviceManager.h"

#define JOTUPIX_THREAD_TIMER_TICK 100

@interface AppDelegate ()

@property (nonatomic, strong) NSTimer *deviceTimer;
@property (nonatomic, assign) uint32_t currentMs;

@end

@implementation AppDelegate


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    jLogInit(NULL);
    [self startDeviceTimer];
    return YES;
}


#pragma mark - UISceneSession lifecycle


- (UISceneConfiguration *)application:(UIApplication *)application configurationForConnectingSceneSession:(UISceneSession *)connectingSceneSession options:(UISceneConnectionOptions *)options {
    // Called when a new scene session is being created.
    // Use this method to select a configuration to create the new scene with.
    return [[UISceneConfiguration alloc] initWithName:@"Default Configuration" sessionRole:connectingSceneSession.role];
}


- (void)application:(UIApplication *)application didDiscardSceneSessions:(NSSet<UISceneSession *> *)sceneSessions {
    // Called when the user discards a scene session.
    // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
    // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
}

// Start timer (call after device is connected)
- (void)startDeviceTimer {
    self.currentMs = 0;
    
    // Fire every 10 milliseconds
    self.deviceTimer = [NSTimer scheduledTimerWithTimeInterval:JOTUPIX_THREAD_TIMER_TICK / 1000.0
                                                        target:self
                                                      selector:@selector(onDeviceTick)
                                                      userInfo:nil
                                                       repeats:YES];
    
    // Ensure timer works during scrolling
    [[NSRunLoop currentRunLoop] addTimer:self.deviceTimer forMode:NSRunLoopCommonModes];
}

// Stop timer (call when disconnecting or exiting)
- (void)stopDeviceTimer {
    [self.deviceTimer invalidate];
    self.deviceTimer = nil;
}

// Timer callback
- (void)onDeviceTick {
    self.currentMs += JOTUPIX_THREAD_TIMER_TICK;
    
    // Get device instance and call Tick
    JotuPix *device = [[JDeviceManager sharedInstance] getDevice];
    if (device != nil) {
        [device tick:self.currentMs];
    }
}

// Remember to stop timer in dealloc
- (void)dealloc {
    [self stopDeviceTimer];
}


@end
