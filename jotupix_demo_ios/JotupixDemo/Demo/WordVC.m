//
//  WordVC.m
//  SDKTest
//
//  Created by go on 12/4/25.
//

#import "WordVC.h"
#import "HLUtils.h"

#import "JProgramContent.h"
#import "JTextFont.h"
#import "JTextContent.h"
#import "JTextDiyColorContent.h"
#import "JTextFullColorContent.h"
#import "JDeviceManager.h"
#import "JColor.h"

@interface WordVC () <IJSend, IJSendProgramCallback>

/// Number of rows of the device LED matrix
@property (nonatomic, assign) int deviceRow;

/// Number of columns of the device LED matrix
@property (nonatomic, assign) int deviceCol;

/// Selected color mode (monochrome, multicolor, rainbow, etc.)
@property (nonatomic, assign) int colorMode;

/// Selected display mode (scroll left, scroll right, static, etc.)
@property (nonatomic, assign) int showMode;

/// User input text view
@property (weak, nonatomic) IBOutlet UITextView *wordTV;

/// Selected color label
@property (weak, nonatomic) IBOutlet UILabel *colorLable;

/// Selected show mode label
@property (weak, nonatomic) IBOutlet UILabel *modelLable;

@end

@implementation WordVC

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self initSubView];
    
    // Default display mode: continuous left scroll
    self.showMode = 2;

    // Tap gesture for dismissing keyboard
    UITapGestureRecognizer *tap =
        [[UITapGestureRecognizer alloc] initWithTarget:self
                                                action:@selector(dismissKeyboard)];
    tap.cancelsTouchesInView = NO;
    [self.view addGestureRecognizer:tap];
    
    // Create a device instance and set this class as the send delegate
    [DeviceManager createDevice:self];
}

#pragma mark - View Life Cycle

/// Configure device row/column from the connected BLE device
- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    if ([BleManager sharedManager].connectedPeripheral == nil) {
        // Default size when no device connected
        self.deviceRow = 16;
        self.deviceCol = 64;
    } else {
        // Read LED matrix size from the connected device
        self.deviceRow = [[BleManager sharedManager].currentJotusPeripheral.rowNum intValue];
        self.deviceCol = [[BleManager sharedManager].currentJotusPeripheral.colNum intValue];
    }
}

#pragma mark - UI Setup

/// Configure UITextView appearance (border, corner, etc.)
- (void)initSubView {
    self.wordTV.layer.borderColor = [UIColor lightGrayColor].CGColor;
    self.wordTV.layer.borderWidth = 1.0;
    self.wordTV.layer.cornerRadius = 5.0;
}

#pragma mark - UI Button Handlers

- (IBAction)colorSelect:(id)sender {
    [self showColorSelectionAlert];
}

- (IBAction)modelSelect:(id)sender {
    [self showModelSelectionAlert];
}

/// Main action: Build text program + send to LED device
- (IBAction)sendClick:(id)sender {
    
    if (![HLUtils checkBleDevice]) return;
    
    // Container for all display content
    JProgramContent  *jProgramContent = [[JProgramContent alloc] init];
    
    //
    // STEP 1 – Parse rich text into individual characters
    //
    NSMutableArray *strMutaArr = [[NSMutableArray alloc] init];
    NSAttributedString *originAttributedStr = self.wordTV.attributedText;

    [originAttributedStr enumerateAttribute:NSAttachmentAttributeName
                                    inRange:NSMakeRange(0, originAttributedStr.length)
                                    options:0
                                 usingBlock:^(id value, NSRange range, BOOL *stop) {
        
        NSString *subStr = [originAttributedStr attributedSubstringFromRange:range].string;

        [subStr enumerateSubstringsInRange:NSMakeRange(0, subStr.length)
                                   options:NSStringEnumerationByComposedCharacterSequences
                                usingBlock:^(NSString *substring, NSRange substringRange, NSRange enclosingRange, BOOL *stop2) {
            [strMutaArr addObject:substring];
        }];
    }];
    
    // Device dimensions
    self.deviceRow = [[BleManager sharedManager].currentJotusPeripheral.rowNum intValue];
    self.deviceCol = [[BleManager sharedManager].currentJotusPeripheral.colNum intValue];
    
    int fontSize = self.deviceRow;
    BOOL isBold = YES;
    
    NSMutableArray<JTextFont *> *textData = [[NSMutableArray alloc] init];
    NSInteger textAllWide = 0;

    //
    // STEP 2 – Convert each character to lattice data (pixel matrix)
    //
    for (int i = 0; i < strMutaArr.count; i++) {
        
        NSString *text = strMutaArr[i];
        
        // Convert text → pixel lattice array
        NSArray *latticeArray =
            [HLUtils generateDataFromImageHeight:self.deviceRow
                                            text:text
                                        fontSize:fontSize
                                          isBold:isBold];
        
        // Optimize dot-matrix (padding columns, spacing)
        latticeArray =
            [HLUtils optArrayWithLatticeArray:latticeArray
                             wordShowHeight:self.deviceRow
                                  fontSpace:1
                                   fontSize:fontSize];
        
        // Build JTextFont model
        JTextFont *jTextFont = [[JTextFont alloc] init];
        jTextFont.textType = JTextFontTypeMonochrome;
        jTextFont.textWidth = latticeArray.count;
        jTextFont.textColor = 0x0F00; // default color
        jTextFont.showData = [HLUtils checkedDataWithLatticeArray:latticeArray];

        [textData addObject:jTextFont];
        textAllWide += jTextFont.textWidth;
    }

    //
    // STEP 3 – Build color section based on selected colorMode
    //
    switch (self.colorMode) {
        case 0: // One character, one color
        {
            int colorIndex = 0;
            NSArray *multiColorList = [JTextDiyColorContent multicolorData];
            
            for (JTextFont *font in textData) {
                font.textColor = [multiColorList[colorIndex] intValue];
                colorIndex = (colorIndex + 1) % multiColorList.count;
            }
            
            JTextDiyColorContent *c = [[JTextDiyColorContent alloc] init];
            c.moveSpace = self.deviceCol;
            c.showX = 0;
            c.showY = 0;
            c.showWidth = self.deviceCol;
            c.showHeight = self.deviceRow;
            c.showMode = self.showMode;
            c.showSpeed = 235;
            c.stayTime = 1;
            c.textNum = textData.count;
            c.textAllWide = textAllWide;
            c.textData = textData;
            [jProgramContent addContent:c];
        }
            break;
            
        case 1: // All red
        {
            for (JTextFont *font in textData) {
                font.textColor = JColorRed;
            }
            
            JTextDiyColorContent *c = [[JTextDiyColorContent alloc] init];
            c.moveSpace = self.deviceCol;
            c.showX = 0;
            c.showY = 0;
            c.showWidth = self.deviceCol;
            c.showHeight = self.deviceRow;
            c.showMode = self.showMode;
            c.showSpeed = 235;
            c.stayTime = 1;
            c.textNum = textData.count;
            c.textAllWide = textAllWide;
            c.textData = textData;
            [jProgramContent addContent:c];
        }
            break;
            
        case 2: // All yellow
        {
            for (JTextFont *font in textData) {
                font.textColor = JColorYellow;
            }
            
            JTextDiyColorContent *c = [[JTextDiyColorContent alloc] init];
            c.moveSpace = self.deviceCol;
            c.showX = 0;
            c.showY = 0;
            c.showWidth = self.deviceCol;
            c.showHeight = self.deviceRow;
            c.showMode = self.showMode;
            c.showSpeed = 235;
            c.stayTime = 1;
            c.textNum = textData.count;
            c.textAllWide = textAllWide;
            c.textData = textData;
            [jProgramContent addContent:c];
        }
            break;

        case 3: // Glowing color mode 1
        {
            JTextFullColorContent *fc = [[JTextFullColorContent alloc] init];
            fc.showX = 0;
            fc.showY = 0;
            fc.showWidth = self.deviceCol;
            fc.showHeight = self.deviceRow;
            fc.textColorType = JTextFullColorTypeHorScroll;
            fc.textColorSpeed = 200;
            fc.textColorDir = JTextFullColorDirRight;
            fc.textFullColor = [JTextFullColorContent textFullColorRainbow];
            [jProgramContent addContent:fc];
        }
            break;
            
        case 4: // Glowing color mode 2
        {
            JTextFullColorContent *fc = [[JTextFullColorContent alloc] init];
            fc.showX = 0;
            fc.showY = 0;
            fc.showWidth = self.deviceCol;
            fc.showHeight = self.deviceRow;
            fc.textColorType = JTextFullColorTypeHorScroll;
            fc.textColorSpeed = 200;
            fc.textColorDir = JTextFullColorDirRight;
            fc.textFullColor = [JTextFullColorContent textFullColorThree];
            [jProgramContent addContent:fc];
        }
            break;
    }

    //
    // STEP 4 – Add text content section
    //
    JTextContent *textContent = [[JTextContent alloc] init];
    textContent.bgColor = 0x0000;
    textContent.blendType = JBlendTypeCover;
    textContent.showX = 0;
    textContent.showY = 0;
    textContent.showWidth = self.deviceCol;
    textContent.showHeight = self.deviceRow;
    textContent.showMode = self.showMode;
    textContent.showSpeed = 235;
    textContent.stayTime = 1;
    textContent.moveSpace = self.deviceCol;
    textContent.textNum = textData.count;
    textContent.textAllWide = textAllWide;
    textContent.textData = textData;

    [jProgramContent addContent:textContent];
    
    //
    // STEP 5 – Build program header information
    //
    if (![HLUtils checkBleDevice]) return;

    JProgramInfo *info = [[JProgramInfo alloc] init];
    info.proIndex = 0;
    info.proAllNum = 1;
    info.compressFlag = JCompressFlagDo;
    
    JProgramGroupNor *group = [[JProgramGroupNor alloc] init];
    group.playType = 0;
    group.playParam = 1; // Play once
    info.groupParam = group;
    
    //
    // STEP 6 – Send program to the device
    //
    NSData *data = jProgramContent.get;
    [[DeviceManager getDevice] sendProgram:info
                                      data:data.bytes
                                    length:data.length
                                  callback:self];
}

#pragma mark - Private UI Helpers

/// Color selection popup
- (void)showColorSelectionAlert {
    UIAlertController *ac =
        [UIAlertController alertControllerWithTitle:nil
                                            message:nil
                                     preferredStyle:UIAlertControllerStyleAlert];

    UIAlertAction *a1 =
        [UIAlertAction actionWithTitle:@"One character one color"
                                 style:UIAlertActionStyleDefault
                               handler:^(UIAlertAction *a) {
        self.colorLable.text = @"One character one color";
        self.colorMode = 0;
    }];
    [ac addAction:a1];

    UIAlertAction *a2 =
        [UIAlertAction actionWithTitle:@"Red"
                                 style:UIAlertActionStyleDefault
                               handler:^(UIAlertAction *a) {
        self.colorLable.text = @"Red";
        self.colorMode = 1;
    }];
    [ac addAction:a2];

    UIAlertAction *a3 =
        [UIAlertAction actionWithTitle:@"Yellow"
                                 style:UIAlertActionStyleDefault
                               handler:^(UIAlertAction *a) {
        self.colorLable.text = @"Yellow";
        self.colorMode = 2;
    }];
    [ac addAction:a3];

    UIAlertAction *a4 =
        [UIAlertAction actionWithTitle:@"Glowing Colors 1"
                                 style:UIAlertActionStyleDefault
                               handler:^(UIAlertAction *a) {
        self.colorLable.text = @"Glowing Colors 1";
        self.colorMode = 3;
    }];
    [ac addAction:a4];

    UIAlertAction *a5 =
        [UIAlertAction actionWithTitle:@"Glowing Colors 2"
                                 style:UIAlertActionStyleDefault
                               handler:^(UIAlertAction *a) {
        self.colorLable.text = @"Glowing Colors 2";
        self.colorMode = 4;
    }];
    [ac addAction:a5];

    [self presentViewController:ac animated:YES completion:nil];
}

/// Model selection popup
- (void)showModelSelectionAlert {
    UIAlertController *ac =
        [UIAlertController alertControllerWithTitle:nil
                                            message:nil
                                     preferredStyle:UIAlertControllerStyleAlert];

    UIAlertAction *a1 =
        [UIAlertAction actionWithTitle:@"Static"
                                 style:UIAlertActionStyleDefault
                               handler:^(UIAlertAction *a) {
        self.modelLable.text = @"Static";
        self.showMode = 1;
    }];
    [ac addAction:a1];

    UIAlertAction *a2 =
        [UIAlertAction actionWithTitle:@"Continuous left shift"
                                 style:UIAlertActionStyleDefault
                               handler:^(UIAlertAction *a) {
        self.modelLable.text = @"Continuous left shift";
        self.showMode = 2;
    }];
    [ac addAction:a2];

    UIAlertAction *a3 =
        [UIAlertAction actionWithTitle:@"Continuously to the right"
                                 style:UIAlertActionStyleDefault
                               handler:^(UIAlertAction *a) {
        self.modelLable.text = @"Continuously to the right";
        self.showMode = 3;
    }];
    [ac addAction:a3];

    UIAlertAction *a4 =
        [UIAlertAction actionWithTitle:@"Upward"
                                 style:UIAlertActionStyleDefault
                               handler:^(UIAlertAction *a) {
        self.modelLable.text = @"Upward";
        self.showMode = 4;
    }];
    [ac addAction:a4];

    [self presentViewController:ac animated:YES completion:nil];
}

#pragma mark - IJSend

/// BLE data sending with debugging logs
- (int)send:(const uint8_t *)data length:(uint32_t)len {
    NSLog(@"Data Length: %u", len);

    if ([BleManager sharedManager].connectedPeripheral &&
        [BleManager sharedManager].writeCharacteristic) {

        NSData *packet = [NSData dataWithBytes:data length:len];

        // For debugging: convert bytes to hex string
        NSMutableString *hex = [NSMutableString string];
        const uint8_t *bytes = packet.bytes;

        for (NSUInteger i = 0; i < packet.length; i++) {
            [hex appendFormat:@"%02x ", bytes[i]];
        }
        NSLog(@"Sending Data: %@", hex);

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

/// Sending progress callback
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

#pragma mark - Keyboard

/// Dismiss the keyboard
- (void)dismissKeyboard {
    [self.view endEditing:YES];
}

@end
