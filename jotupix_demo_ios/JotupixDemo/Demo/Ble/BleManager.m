#import "BleManager.h"
#import "HLHUDHelper.h"

#import "JDeviceManager.h"

#define ServiceUUID @"FFF0"
#define NewCharacteristicUUID @"FFF1"

@interface BleManager ()



@end

@implementation BleManager

// Singleton
+ (instancetype)sharedManager {
    static BleManager *instance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[self alloc] init];
    });
    return instance;
}

// Initialization
- (instancetype)init {
    self = [super init];
    if (self) {
        self.centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
        self.scanedModelsArray = [NSMutableArray array];
    }
    return self;
}

#pragma mark - public methods
- (void)startDiscoverPeripheral
{
    //[self.scanedModelsArray removeAllObjects];
    [self.centralManager scanForPeripheralsWithServices:nil options:@{CBCentralManagerScanOptionAllowDuplicatesKey:@YES}];
    NSLog(@"Start scanning for devices...");
}

#pragma mark - Bluetooth connection methods

// Disconnect
- (void)disconnectDevice {
    if (self.connectedPeripheral) {
        [self.centralManager cancelPeripheralConnection:self.connectedPeripheral];
        self.connectedPeripheral = nil;
        self.writeCharacteristic = nil;
    }
}

// Send command
- (void)sendCommand:(NSData *)commandData {
    if (self.connectedPeripheral && self.writeCharacteristic) {
        [self.connectedPeripheral writeValue:commandData
                           forCharacteristic:self.writeCharacteristic
                                        type:CBCharacteristicWriteWithResponse];
    } else {
        NSLog(@"Device not connected or write characteristic not defined");
    }
}

#pragma mark - CBCentralManagerDelegate

// Bluetooth state changes
- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    if (central.state == CBManagerStatePoweredOn) {

    } else {
        [HLHUDHelper showErrorWithTitle:@"Bluetooth not available"];
    }
}

// Discover device
- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary<NSString *,id> *)advertisementData RSSI:(NSNumber *)RSSI {
    
    NSData *manufacturerData = [advertisementData objectForKey:@"kCBAdvDataManufacturerData"];
    if (!manufacturerData) {
        return;
    }
    
    NSString *peripheralName = [advertisementData objectForKey:@"kCBAdvDataLocalName"];
    
    if (![peripheralName isEqualToString:@"CoolLEDUX"]){
        return;
    }
    
    Byte *resultByte = (Byte*)[manufacturerData bytes]; // Extract byte array
    NSMutableArray *tempArray = [NSMutableArray array];
    for (int i = 0; i < manufacturerData.length; i++) {
        int number = (int)resultByte[i];
        [tempArray addObject:@(number)];
    }
    NSArray *resultArray = tempArray;
    
    NSString *actulMacAddress = @"";
    NSString *deviceID = @"";
    if (resultArray.count >= 2) {
        deviceID = [deviceID stringByAppendingFormat:@"%02x",[resultArray[1] intValue]];
        deviceID = [deviceID stringByAppendingFormat:@"%02x",[resultArray[0] intValue]];
    }
    
    if (resultArray.count >= 8) {
        for (int i = 7; i >= 2; i --) {
            actulMacAddress = [actulMacAddress stringByAppendingFormat:@"%02x:",[resultArray[i] intValue]];
        }
        actulMacAddress = [actulMacAddress substringToIndex:actulMacAddress.length - 1];
    }
    
    actulMacAddress = [actulMacAddress uppercaseString];
    
    for (JotusPeripheral *model in self.scanedModelsArray) {
        if ([peripheral.identifier.UUIDString isEqualToString:model.UUIDString]) {
            model.RSSI = RSSI;
            break;
        }
    }
    
    BOOL isIn = NO;
    for (JotusPeripheral *model in self.scanedModelsArray) {
        if ([peripheral.identifier.UUIDString isEqualToString:model.UUIDString]) {
            isIn = YES;
            break;
        }
    }
    
    // Already added to array or name does not exist
    if (isIn || !peripheralName) {
        return;
    }
    
    NSNumber *rowNum = nil;
    NSNumber *colNum = nil;
    NSNumber *ledxType = nil;
    NSNumber *firmwarIndex = nil;
    BOOL canAnalyze = [peripheralName isEqualToString:@"CoolLEDUX"];
    if (resultArray.count >= 12 && canAnalyze) {
        rowNum = [NSNumber numberWithInt:[resultArray[8] intValue]];
        
        int col = [resultArray[9] intValue] * 16 * 16 + [resultArray[10] intValue];
        colNum = [NSNumber numberWithInt:col];
        
        ledxType = [NSNumber numberWithInt:[resultArray[11] intValue]];
        firmwarIndex = [NSNumber numberWithInt:[resultArray[12] intValue]];
    }
    
    // Add peripheral
    JotusPeripheral *model = [[JotusPeripheral alloc] init];
    model.name = peripheralName;
    model.RSSI = RSSI;
    model.peripheral = peripheral;
    model.actulMacAddress = [actulMacAddress uppercaseString];
    model.deviceId = [deviceID uppercaseString];
    model.rowNum = rowNum;
    model.colNum = colNum;
    model.UUIDString = peripheral.identifier.UUIDString;
    
    // Remove duplicate model objects
    BOOL isContainModel = NO;
    for (JotusPeripheral *modelOrigin in self.scanedModelsArray) {
        if([modelOrigin.UUIDString isEqual:peripheral.identifier.UUIDString]){
            isContainModel = YES;
            break;
        }
    }
    if(!isContainModel){
        [self.scanedModelsArray addObject:model];
    }
}

// Connection successful
- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral {
    NSLog(@"Connected to device: %@", peripheral.name);
    [HLHUDHelper showSuccessWithTitle:[NSString stringWithFormat:@"Connected to device: %@", peripheral.name]];
    self.connectedPeripheral = peripheral;
    self.connectedPeripheral.delegate = self;
    [self.connectedPeripheral discoverServices:nil];
}

// Connection failed
- (void)centralManager:(CBCentralManager *)central didFailToConnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {
    NSLog(@"Device connection failed: %@", error.localizedDescription);
}

// Disconnected
- (void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {
    NSLog(@"Device disconnected: %@", peripheral.name);
    self.connectedPeripheral = nil;
    self.writeCharacteristic = nil;
}

#pragma mark - CBPeripheralDelegate

// Discover services
- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error {
    if (error) {
        NSLog(@"Error discovering services: %@", error.localizedDescription);
        return;
    }
    for (CBService *service in peripheral.services) {
        if ([service.UUID isEqual:[CBUUID UUIDWithString:ServiceUUID]]) {
            [peripheral discoverCharacteristics:nil forService:service];
            break;
        }
    }
}

// Discover characteristics
- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error {
    if (error) {
        NSLog(@"Error discovering characteristics: %@", error.localizedDescription);
        return;
    }
    for (CBCharacteristic *characteristic in service.characteristics) {
        
        CBCharacteristicProperties properties = characteristic.properties;
        
        if (![characteristic.UUID isEqual:[CBUUID UUIDWithString:NewCharacteristicUUID]]) {
            continue;
        }
        
        if (properties & CBCharacteristicPropertyRead) {
            [peripheral readValueForCharacteristic:characteristic];
        }
        
        // Assuming this is the characteristic we need to write data to
        if ((characteristic.properties & CBCharacteristicPropertyWrite) || (characteristic.properties & CBCharacteristicPropertyWriteWithoutResponse)) {
            self.writeCharacteristic = characteristic;
            [peripheral setNotifyValue:YES forCharacteristic:characteristic];
        }
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error
{
    NSData *data = characteristic.value;
    if (!data || data.length == 0) {
        return;
    }

    const uint8_t *bytes = data.bytes;
    uint32_t len = (uint32_t)data.length;
    [[DeviceManager getDevice] parseRecvData:bytes len:len];
}

@end
