//
//  SetVC.m
//  SDKTest
//
//  Created by go on 12/4/25.
//

#import "SetVC.h"
#import "DeviceVC.h"
#import "BleManager.h"
#import "PromptV.h"
#import "HLUtils.h"
#import "JDeviceManager.h"

@interface SetVC () <IJGetDevInfoCallback, IJSend>

@end

@implementation SetVC

- (void)viewDidLoad {
    [super viewDidLoad];
    // Additional initialization can be added here if needed.
    
    // Create a device instance and set this class as the send delegate
    [DeviceManager createDevice:self];
}

#pragma mark - UI Actions

/// Button action: Request device information from the connected BLE device
/// Triggered when user taps "Get Device Info"
- (IBAction)getDeviceClick:(id)sender {
    if (![HLUtils checkBleDevice]) return;  // Ensure device is connected
    

    
    // Request device information
    [[DeviceManager getDevice] getDevInfo:self];
}

#pragma mark - IJSend Protocol

/// Send raw data bytes to the BLE device
/// @param data Pointer to byte buffer
/// @param len Number of bytes to send
/// @return 0 if successful
- (int)send:(const uint8_t *)data length:(uint32_t)len {
    if ([BleManager sharedManager].connectedPeripheral &&
        [BleManager sharedManager].writeCharacteristic) {
        
        NSData *packet = [NSData dataWithBytes:data length:len];
        
        // Write data to BLE device without requiring a response
        [[BleManager sharedManager].connectedPeripheral writeValue:packet
             forCharacteristic:[BleManager sharedManager].writeCharacteristic
                          type:CBCharacteristicWriteWithoutResponse];
        
    } else {
        NSLog(@"Device not connected or write characteristic not defined");
        return -1;
    }
    return 0;
}

#pragma mark - IJGetDevInfoCallback

/// Callback: Device information received from BLE device
/// @param info A JInfo object containing all device parameters
- (void)onEvent:(JInfo *)info {
    NSLog(@"===== Device Info =====");
    NSLog(@"Name: %@", info.devName);
    NSLog(@"ID: %04X", info.devId);
    NSLog(@"Size: %dx%d", info.devWidth, info.devHeight);
    NSLog(@"Brightness: %d", info.bn);
    NSLog(@"Flip: %d", info.flip);
    NSLog(@"Version: %d", info.version);
    
    // Create a prompt view with confirmation block (currently empty)
    PromptV *promptV = [[PromptV alloc] initWithSureBlock:^() {
        // Additional confirmation logic can be added here
    }];
    
    // Prepare device info text for display
    NSString *infoStr = [NSString stringWithFormat:
                         @" devName: %@\n"
                         " devId: %lX\n"
                         " Size: %dx%d\n"
                         " Brightness: %ld\n"
                         " Flip: %ld\n"
                         " Version: %ld",
                         info.devName,
                         info.devId,
                         info.devHeight, info.devHeight,   // NOTE: devWidth might be intended?
                         info.bn,
                         info.flip,
                         info.version];
    
    // Display popup
    [promptV showWTitle:@"Device Information" Content:infoStr];
    [promptV show];
}

@end
