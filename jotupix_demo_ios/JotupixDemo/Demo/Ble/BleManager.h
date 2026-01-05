#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import "JotusPeripheral.h"

@interface BleManager : NSObject <CBCentralManagerDelegate, CBPeripheralDelegate>

/// Stores the currently discovered Bluetooth device objects <GWPeripheral>
@property (nonatomic,strong) NSMutableArray *scanedModelsArray;

// Currently connected device
@property (nonatomic, strong) CBPeripheral *connectedPeripheral;

@property (nonatomic, strong) JotusPeripheral *currentJotusPeripheral;

// Target characteristic (used for sending data)
@property (nonatomic, strong) CBCharacteristic *writeCharacteristic;

// CoreBluetooth manager
@property (nonatomic, strong) CBCentralManager *centralManager;

// Stores the name of the target device to connect
@property (nonatomic, copy) NSString *targetDeviceName;

// Singleton instance
+ (instancetype)sharedManager;

- (void)startDiscoverPeripheral;

// Disconnect from the Bluetooth device
- (void)disconnectDevice;

// Send command data to the connected device
- (void)sendCommand:(NSData *)commandData;

@end
