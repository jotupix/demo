//
//  AnimaVC.m
//  SDKTest
//
//  Created by go on 12/4/25.
//

#import "AnimaVC.h"
#import "FLAnimatedImage.h"
#import "BleManager.h"
#import "HLUtils.h"

#import "JProgramContent.h"
#import "JGifContent.h"
#import "JDeviceManager.h"

@interface AnimaVC ()<UITableViewDataSource,UITableViewDelegate,IJSend,IJSendProgramCallback>

/// Table view that shows selectable GIF animation previews
@property (weak, nonatomic) IBOutlet UITableView *tableV;

/// Index of the selected animation item
@property (nonatomic, assign) NSInteger selectedIndex;

@end

@implementation AnimaVC

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Setup table view delegates
    self.tableV.dataSource = self;
    self.tableV.delegate = self;
    
    // Create a device instance and set this class as the send delegate
    [DeviceManager createDevice:self];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    // Reload to refresh GIF previews
    [self.tableV reloadData];
}

#pragma mark - Send GIF to Device

/// Send selected GIF animation to the LED device
- (IBAction)send:(id)sender {
    
    if (![HLUtils checkBleDevice]) return;
    
    //
    // 1. Create GIF content configuration
    //
    JGifContent *jGifContent = [[JGifContent alloc] init];
    jGifContent.showX = 0;
    jGifContent.showY = 0;
    jGifContent.showWidth  = [[BleManager sharedManager].currentJotusPeripheral.colNum integerValue];
    jGifContent.showHeight = [[BleManager sharedManager].currentJotusPeripheral.rowNum integerValue];
    
    // Retrieve selected GIF
    NSString *gifName = [NSString stringWithFormat:@"DynamicWord_bg_%d", self.selectedIndex + 1];
    NSURL *gifUrl = [[NSBundle mainBundle] URLForResource:gifName withExtension:@"gif"];
    NSData *gifData = [NSData dataWithContentsOfURL:gifUrl];
    jGifContent.gifData = gifData;
    
    //
    // 2. Package GIF into program content
    //
    JProgramContent *jProgramContent = [[JProgramContent alloc] init];
    [jProgramContent addContent:jGifContent];
    
    //
    // 3. Setup program header info
    //
    JProgramInfo *info = [[JProgramInfo alloc] init];
    info.proIndex = 0;
    info.proAllNum = 1;
    info.compressFlag = JCompressFlagDo;
    
    JProgramGroupNor *group = [[JProgramGroupNor alloc] init];
    group.playType = 0;  // Normal play mode
    group.playParam = 1; // Play once
    info.groupParam = group;
    
    //
    // 4. Send data through BLE
    //
    NSData *data = jProgramContent.get;
    
    [[DeviceManager getDevice] sendProgram:info
                                      data:data.bytes
                                    length:data.length
                                  callback:self];
}

#pragma mark - IJSend (BLE data sender)

/// Send raw BLE data
- (int)send:(const uint8_t *)data length:(uint32_t)len {
    
    NSLog(@"Data Length: %u", len);
    
    // Ensure BLE is connected and characteristic is ready
    if ([BleManager sharedManager].connectedPeripheral &&
        [BleManager sharedManager].writeCharacteristic) {
        
        NSData *packet = [NSData dataWithBytes:data length:len];
        
        // Debug: Convert to hex string
        const uint8_t *bytes = packet.bytes;
        NSMutableString *hexString = [NSMutableString string];
        
        for (NSUInteger i = 0; i < packet.length; i++) {
            [hexString appendFormat:@"%02x ", bytes[i]];
        }
        NSLog(@"Sending Data: %@", hexString);
        
        // Write to BLE characteristic
        [[BleManager sharedManager].connectedPeripheral writeValue:packet
            forCharacteristic:[BleManager sharedManager].writeCharacteristic
                         type:CBCharacteristicWriteWithoutResponse];
    } else {
        NSLog(@"Device not connected or write characteristic not defined");
        return -1;
    }
    
    return 0;
}

#pragma mark - IJSendProgramCallback

/// Program sending progress callback
- (void)onEvent:(JSendStatus)status percent:(uint8_t)percent {
    NSLog(@"status: %d percent: %d", status, percent);
    
    if (status == JSendStatusProgress) {
        if (percent == 0) {
            [HLHUDHelper showLoadingWithTitle:@"Send" detailText:@"0%"];
        } else {
            HLHUD *hud = [HLHUDHelper currentHud];
            hud.detailLabel.text = [NSString stringWithFormat:@"%d%%", percent];
            NSLog(@"Send Progress: %d%%", percent);
        }
    }
    
    if (status == JSendStatusCompleted) {
        [HLHUDHelper showLoadingWithTitle:@"Send" detailText:@"100%"];
        [HLHUDHelper showSuccessWithTitle:@"Send Successful"];
    }
    
    if (status == JSendStatusFail) {
        [HLHUDHelper showErrorWithTitle:@"Send Fail"];
    }
}

#pragma mark - UITableViewDataSource

/// Row height for each GIF preview
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 56;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

/// Number of GIF options (6 GIF files)
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return 6;
}

/// Build each cell containing the GIF preview
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {

    static NSString *cellIdentifier = @"DefaultCell";
    
    // Create or reuse cell
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    
    if (!cell) {
        // Create cell with default style
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault
                                      reuseIdentifier:cellIdentifier];
        
        // Add GIF display view
        FLAnimatedImageView *gifImageView =
            [[FLAnimatedImageView alloc] initWithFrame:cell.contentView.bounds];
        
        gifImageView.tag = 1001;
        gifImageView.contentMode = UIViewContentModeScaleAspectFill;
        gifImageView.clipsToBounds = YES;
        
        [cell.contentView addSubview:gifImageView];
    }
    
    // Retrieve the image view
    FLAnimatedImageView *gifImageView = [cell.contentView viewWithTag:1001];
    gifImageView.frame = cell.contentView.bounds;
    
    // Load corresponding GIF
    NSString *gifName = [NSString stringWithFormat:@"DynamicWord_bg_%d", indexPath.row + 1];
    NSURL *url = [[NSBundle mainBundle] URLForResource:gifName withExtension:@"gif"];
    NSData *gifData = [NSData dataWithContentsOfURL:url];
    
    FLAnimatedImage *gifImage = [FLAnimatedImage animatedImageWithGIFData:gifData];
    gifImageView.animatedImage = gifImage;
    
    // Adjust padding inside cell
    gifImageView.frame = CGRectMake(15, 5,
                                    cell.contentView.bounds.size.width - 30,
                                    cell.contentView.bounds.size.height - 10);
    
    //
    // Highlight selected cell with red border
    //
    if (indexPath.row == self.selectedIndex) {
        cell.layer.borderWidth = 2.0;
        cell.layer.borderColor = [UIColor redColor].CGColor;
    } else {
        cell.layer.borderWidth = 0.0;
        cell.layer.borderColor = [UIColor clearColor].CGColor;
    }
    
    return cell;
}

#pragma mark - UITableViewDelegate

/// Handle selection of GIF item
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    // Update selection index
    self.selectedIndex = indexPath.row;
    
    // Refresh table to update highlight/border
    [self.tableV reloadData];
}

@end
