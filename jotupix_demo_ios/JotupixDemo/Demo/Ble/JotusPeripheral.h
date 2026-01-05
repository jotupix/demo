//
//  JotusPeripheral.h
//  SDKTest
//
//  Created by go on 5/10/25.
//

#import <CoreBluetooth/CoreBluetooth.h>

NS_ASSUME_NONNULL_BEGIN

/**
 * JotusPeripheral
 *
 * Model representing a discovered Bluetooth peripheral device.
 * Contains basic device information such as name, UUID, MAC address,
 * signal strength, and display configuration.
 */
@interface JotusPeripheral : NSObject

/** Peripheral broadcast name */
@property (nonatomic, copy) NSString *name;

/** Peripheral identifier (UUID in string format) */
@property (copy, nonatomic) NSString *UUIDString;

/** RSSI signal strength of the peripheral */
@property (nonatomic, strong) NSNumber *RSSI;

/** Actual MAC address parsed from advertisement data */
@property (nonatomic, copy) NSString *actulMacAddress;

/** Device identifier defined by the manufacturer */
@property (nonatomic, copy) NSString *deviceId;

/** CoreBluetooth peripheral instance */
@property (nonatomic, strong) CBPeripheral *peripheral;

/** Number of display rows (e.g., pixel rows or grid rows) */
@property (nonatomic, strong) NSNumber *rowNum;

/** Number of display columns (e.g., pixel columns or grid columns) */
@property (nonatomic, strong) NSNumber *colNum;

@end

NS_ASSUME_NONNULL_END
