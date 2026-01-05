//
//  DeviceVC.m
//  SDKTest
//
//  Created by go on 5/10/25.
//

#import "DeviceVC.h"
#import "JotusPeripheral.h"
#import "BleManager.h"

@interface DeviceVC () <UITableViewDataSource, UITableViewDelegate>

/// TableView displaying scanned BLE devices
@property (strong, nonatomic) UITableView *tableView;

/// List of scanned BLE peripherals (JotusPeripheral objects)
@property (strong, nonatomic) NSMutableArray *deviceArray;

@end

@implementation DeviceVC

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Configure navigation bar UI
    [self initNavigationBar];
    
    // Initialize table view and layout
    [self initSubViews];
    
    // Get scanned BLE devices from the BLE manager
    self.deviceArray = [BleManager sharedManager].scanedModelsArray;
}

#pragma mark - UI Setup

/// Initialize navigation bar appearance
- (void)initNavigationBar {
    self.view.backgroundColor = [UIColor whiteColor];
    self.navigationItem.title = @"Device";
}

/// Initialize table view and add it to the current view
- (void)initSubViews {
    self.tableView = [[UITableView alloc] initWithFrame:self.view.bounds
                                                  style:UITableViewStylePlain];
    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    self.tableView.rowHeight = 56;
    self.tableView.tableFooterView = [UIView new]; // Hide empty rows
    self.tableView.backgroundColor = [UIColor whiteColor];
    self.tableView.separatorColor = [UIColor blackColor];
    
    [self.view addSubview:self.tableView];
}

#pragma mark - UITableViewDataSource

/// Number of rows = number of scanned BLE devices
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.deviceArray.count;
}

/// Build each device cell displaying name, ID, resolution, and RSSI
- (UITableViewCell *)tableView:(UITableView *)tableView
         cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    UITableViewCell *cell =
        [tableView dequeueReusableCellWithIdentifier:@"CellIdentifier"];
    
    if (!cell) {
        // Create a new cell if none is available for reuse
        cell = [[UITableViewCell alloc]
                initWithStyle:UITableViewCellStyleSubtitle
                reuseIdentifier:@"CellIdentifier"];
    }
    
    // Retrieve the peripheral model object
    JotusPeripheral *peripheral = self.deviceArray[indexPath.row];
    
    // Title: Device name + deviceId
    cell.textLabel.text =
        [NSString stringWithFormat:@"%@-%@", peripheral.name, peripheral.deviceId];
    
    // Subtitle: Display size (row * col) + RSSI signal strength
    cell.detailTextLabel.text =
        [NSString stringWithFormat:@"%@*%@   RSSI:%@",
         peripheral.rowNum, peripheral.colNum, peripheral.RSSI];
    
    return cell;
}

#pragma mark - UITableViewDelegate

/// Called when user taps on a device to connect
- (void)tableView:(UITableView *)tableView
didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    // Disconnect any previously connected device
    [[BleManager sharedManager] disconnectDevice];
    
    // Fetch selected peripheral model
    JotusPeripheral *jotusPeripheral = self.deviceArray[indexPath.row];
    
    // Update BLE manager with selected peripheral
    [BleManager sharedManager].connectedPeripheral = jotusPeripheral.peripheral;
    [BleManager sharedManager].currentJotusPeripheral = jotusPeripheral;
    
    // Start BLE connection
    [[BleManager sharedManager].centralManager
        connectPeripheral:jotusPeripheral.peripheral
                  options:nil];
    
    // Go back to previous view controller
    [self.navigationController popViewControllerAnimated:YES];
}

@end
