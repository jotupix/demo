//
//  BaseVC.m
//  SDKTest
//
//  Created by go on 4/21/25.
//

#import "BaseVC.h"
#import "DeviceVC.h"
#import "BleManager.h"
#import "FLAnimatedImage.h"
#import "HLUtils.h"
#import "JDeviceManager.h"

@interface BaseVC () <IJSend>

/// Label showing current brightness value
@property (weak, nonatomic) IBOutlet UILabel *brightNessL;

/// Label showing current screen flip mode
@property (weak, nonatomic) IBOutlet UILabel *modelLable;

/// Image view used for displaying animated GIFs (if needed)
@property (nonatomic, strong) FLAnimatedImageView *gifImageView;

@end

@implementation BaseVC

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Initialize BLE configuration
    [self initSetBle];
    
    // Create a device instance and set this class as the send delegate
    [DeviceManager createDevice:self];
}

#pragma mark - UI Actions

/// Called when user taps on screen flip mode selection
- (IBAction)modelSelect:(id)sender {
    [self showSelectionAlert];
}

#pragma mark - BLE Initialization

/// Initialize BLE manager and schedule scanning after delay
- (void)initSetBle {
    // Ensure BLE Manager singleton is created
    [BleManager sharedManager];
    
    // Delay scanning to allow proper initialization
    [self performSelector:@selector(delayedMethod) withObject:nil afterDelay:2.0];
}

/// Called after delay to start scanning BLE peripherals
- (void)delayedMethod {
    [[BleManager sharedManager] startDiscoverPeripheral];
}

#pragma mark - IJSend Protocol (Device → BLE data sending)

/// Send raw bytes to BLE device
/// @param data Raw byte buffer
/// @param len Length of the data buffer
/// @return 0 if successful
- (int)send:(const uint8_t *)data length:(uint32_t)len {
    if ([BleManager sharedManager].connectedPeripheral &&
        [BleManager sharedManager].writeCharacteristic) {

        NSData *packet = [NSData dataWithBytes:data length:len];

        // Write data to BLE characteristic without requiring response
        [[BleManager sharedManager].connectedPeripheral writeValue:packet
             forCharacteristic:[BleManager sharedManager].writeCharacteristic
                          type:CBCharacteristicWriteWithoutResponse];

    } else {
        NSLog(@"Device not connected or write characteristic not defined");
        return -1;
    }
    return 0;  // success
}

#pragma mark - Device Controls

/// Toggle device ON/OFF via UISwitch
- (IBAction)switch:(UISwitch *)sender {
    if (![HLUtils checkBleDevice]) return;

    BOOL isOn = sender.isOn;
    if (isOn) {
        [[DeviceManager getDevice] sendSwitchStatus:1];
    } else {
        [[DeviceManager getDevice] sendSwitchStatus:0];
    }
}

/// Adjust brightness using slider
- (IBAction)setBrightNessSlid:(id)sender {
    if (![HLUtils checkBleDevice]) return;

    UISlider *slider = (UISlider *)sender;
    NSInteger value = (NSInteger)(slider.value);

    // Update UI
    self.brightNessL.text = [NSString stringWithFormat:@"Brightness:%ld", value];

    // Send brightness value to device
    [[DeviceManager getDevice] sendBrightness:value];
}

#pragma mark - Screen Flip Mode Selector

/// Display an alert allowing the user to choose screen flip mode
- (void)showSelectionAlert {
    // Create alert controller (no title)
    UIAlertController *alertController =
        [UIAlertController alertControllerWithTitle:nil
                                            message:nil
                                     preferredStyle:UIAlertControllerStyleAlert];

    // Option 1: No flip
    UIAlertAction *option1Action =
        [UIAlertAction actionWithTitle:@"Do not flip"
                                 style:UIAlertActionStyleDefault
                               handler:^(UIAlertAction *action) {
        NSLog(@"Do not flip");
        [[DeviceManager getDevice] sendScreenFlip:0];
        self.modelLable.text = @"Do not flip";
    }];
    [alertController addAction:option1Action];

    // Option 2: XY flip
    UIAlertAction *option2Action =
        [UIAlertAction actionWithTitle:@"XY Flip"
                                 style:UIAlertActionStyleDefault
                               handler:^(UIAlertAction *action) {
        NSLog(@"XY Flip");
        [[DeviceManager getDevice] sendScreenFlip:1];
        self.modelLable.text = @"XY Flip";
    }];
    [alertController addAction:option2Action];

    // Option 3: X flip
    UIAlertAction *option3Action =
        [UIAlertAction actionWithTitle:@"X Flip"
                                 style:UIAlertActionStyleDefault
                               handler:^(UIAlertAction *action) {
        NSLog(@"X Flip");
        [[DeviceManager getDevice] sendScreenFlip:2];
        self.modelLable.text = @"X Flip";
    }];
    [alertController addAction:option3Action];

    // Option 4: Y flip
    UIAlertAction *option4Action =
        [UIAlertAction actionWithTitle:@"Y Flip"
                                 style:UIAlertActionStyleDefault
                               handler:^(UIAlertAction *action) {
        NSLog(@"Y Flip");
        [[DeviceManager getDevice] sendScreenFlip:3];
        self.modelLable.text = @"Y Flip";
    }];
    [alertController addAction:option4Action];

    // Present the alert controller
    [self presentViewController:alertController animated:YES completion:nil];
}

@end
