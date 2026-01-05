//
//  HLUtils.m
//  SDKTest
//
//  Created by go on 12/11/25.
//

#import "HLUtils.h"

@implementation HLUtils

#pragma mark - BLE Device Check

/// Check if the BLE device is connected before sending data.
/// Shows an error HUD if the device is not connected.
+ (BOOL)checkBleDevice {
    if ([BleManager sharedManager].connectedPeripheral == nil) {
        [HLHUDHelper showErrorWithTitle:@"Please connect to a Bluetooth device"];
        return NO;
    }
    return YES;
}

#pragma mark - Text → Bitmap Font Lattice Conversion

/// Convert text into a pixel lattice array (matrix) by rendering the text into an image.
/// row: LED matrix height
/// text: character to render
/// fontSize: size of the font used for rendering
/// isBold: determines whether bold font is used
+ (NSArray *)generateDataFromImageHeight:(int)row
                                    text:(NSString *)text
                                 fontSize:(int)fontSize
                                   isBold:(BOOL)isBold {
    UIImage *image;

    // Canvas size matches LED matrix height
    CGSize imageSize = CGSizeMake(row, row);

    // Load font (bold or regular)
    UIFont *font = [UIFont fontWithName:(isBold ? @"Thonburi-Bold" : @"Thonburi")
                                  size:fontSize];

    // Measure the text size to ensure it fits the canvas
    CGSize textSize = [text sizeWithAttributes:@{NSFontAttributeName: font}];

    // If text is too wide, extend canvas
    if (textSize.width > imageSize.width)
        imageSize = CGSizeMake(textSize.width, row);

    // If text height exceeds canvas, reduce font size by 20%
    if (textSize.height > imageSize.height) {
        fontSize = fontSize - (int)(fontSize * 0.2);
        font = [UIFont fontWithName:(isBold ? @"Thonburi-Bold" : @"Thonburi")
                               size:fontSize];
        textSize = [text sizeWithAttributes:@{NSFontAttributeName: font}];
    }

    // Center text inside the canvas
    CGPoint textOrigin = CGPointMake(
        (imageSize.width - textSize.width) / 2,
        (imageSize.height - textSize.height) / 2
    );

    // Start bitmap creation
    UIGraphicsBeginImageContextWithOptions(imageSize, NO, 1.0);
    CGContextRef context = UIGraphicsGetCurrentContext();

    // Disable anti-aliasing to produce sharp pixel edges (important for lattice)
    CGContextSetShouldAntialias(context, NO);
    CGContextSetInterpolationQuality(context, kCGInterpolationLow);

    // Fill background as black
    [[UIColor blackColor] set];
    CGContextFillRect(context, CGRectMake(0, 0, imageSize.width, imageSize.height));

    // Draw red text
    UIColor *textColor = [UIColor redColor];
    CGContextSetLineWidth(context, 1.0);
    CGContextSetTextDrawingMode(context, kCGTextFill);

    [text drawAtPoint:textOrigin
       withAttributes:@{
           NSFontAttributeName: font,
           NSForegroundColorAttributeName: textColor
       }];

    // Get rendered image
    image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();

    // Convert rendered text image → binary pixel lattice (1 = pixel on, 0 = off)
    NSArray *pixelData = [HLUtils getDataFromFontImage:image scale:1];
    return pixelData;
}

#pragma mark - Bitmap → Pixel Lattice (0/1 Matrix)

/// Converts a bitmap image into a lattice matrix where:
/// 1 = pixel exists (non-black)
/// 0 = pixel is empty (black)
+ (NSArray *)getDataFromFontImage:(UIImage *)image scale:(CGFloat)scale {

    CGImageRef imageRef = [image CGImage];

    NSUInteger widthImage  = CGImageGetWidth(imageRef);
    NSUInteger heightImage = CGImageGetHeight(imageRef);

    NSUInteger scaledWidth  = widthImage / scale;
    NSUInteger scaledHeight = heightImage / scale;

    // Prepare raw pixel buffer
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    unsigned char *rawData = (unsigned char *)calloc(heightImage * widthImage * 4,
                                                     sizeof(unsigned char));

    NSUInteger bytesPerPixel = 4;
    NSUInteger bytesPerRow = bytesPerPixel * widthImage;
    NSUInteger bitsPerComponent = 8;

    CGContextRef context =
        CGBitmapContextCreate(rawData,
                              widthImage,
                              heightImage,
                              bitsPerComponent,
                              bytesPerRow,
                              colorSpace,
                              kCGImageAlphaPremultipliedLast |
                              kCGBitmapByteOrder32Big);

    CGColorSpaceRelease(colorSpace);

    // Draw into buffer
    CGContextDrawImage(context, CGRectMake(0, 0, widthImage, heightImage), imageRef);

    NSMutableArray *pixelData = [NSMutableArray arrayWithCapacity:scaledWidth * scaledHeight];

    // Column-by-column scan
    for (NSUInteger x = 0; x < scaledWidth; x++) {

        NSMutableArray *columnArray = [NSMutableArray array];

        for (NSUInteger y = 0; y < scaledHeight; y++) {

            NSUInteger byteIndex = (bytesPerRow * y * scale) + (x * scale * bytesPerPixel);

            CGFloat red   = rawData[byteIndex]     / 255.0f;
            CGFloat green = rawData[byteIndex + 1] / 255.0f;
            CGFloat blue  = rawData[byteIndex + 2] / 255.0f;

            // If the pixel is not black, count as "on"
            if (red != 0 || green != 0 || blue != 0) {
                [columnArray addObject:@1];
            } else {
                [columnArray addObject:@0];
            }
        }
        [pixelData addObject:columnArray];
    }

    free(rawData);
    CGContextRelease(context);

    return [pixelData copy];
}

#pragma mark - Pixel Lattice Optimization

/// Optimize lattice:
/// - Trim trailing zero columns
/// - Keep spacing between characters
/// - Remove leading zero columns
/// - Handle rotated character edge cases
+ (NSArray *)optArrayWithLatticeArray:(NSArray *)latticeArray
                      wordShowHeight:(NSInteger)wordShowHeight
                           fontSpace:(int)fontSpace
                            fontSize:(int)fontSize {
    int deviceRow = wordShowHeight;
    int dataLength = (int)latticeArray.count;

    if (dataLength == 0) return nil;

    NSArray *resultArray = @[];
    int copyLength = -1;

    //
    // 1. Scan from the end to find last non-zero column
    //
    for (int i = dataLength - 1; i > 0; i--) {
        NSArray *colData = latticeArray[i];
        for (NSNumber *n in colData) {
            if (n.intValue != 0) {
                copyLength = i + 1;
                goto foundLast;
            }
        }
    }

foundLast: {}

    NSMutableArray *arrayM = [NSMutableArray array];

    //
    // 2. Copy meaningful columns + trailing spacing
    //
    if (copyLength > 0) {

        for (int j = 0; j < copyLength; j++)
            [arrayM addObject:latticeArray[j]];

        // Add blank spacing columns after character
        for (int i = 0; i < fontSpace; i++) {
            NSArray *emptyCol = [self emptyColArrayWith:@0 rows:deviceRow];
            [arrayM addObject:emptyCol];
        }

        resultArray = [arrayM copy];

        //
        // 3. Remove leading empty columns
        //
        int beginLength = -1;

        for (int i = 0; i < resultArray.count; i++) {
            NSArray *col = resultArray[i];
            for (NSNumber *n in col) {
                if (n.intValue != 0) {
                    beginLength = i;
                    goto foundFirst;
                }
            }
        }

foundFirst: {}

        if (beginLength > -1) {
            NSMutableArray *trimmed = [NSMutableArray array];
            for (int j = beginLength; j < resultArray.count; j++)
                [trimmed addObject:resultArray[j]];
            resultArray = [trimmed copy];
        }

        return resultArray;
    }

    //
    // 4. Entire character is empty → return fixed number of blank columns
    //
    if (copyLength < 0) {
        for (int j = 0; j < (fontSize / 2); j++) {
            [arrayM addObject:[self emptyColArrayWith:@0 rows:deviceRow]];
        }
    }

    return arrayM;
}

#pragma mark - Helper: Create Empty Column

/// Creates a single empty column (all zeros) with specific height.
+ (NSArray *)emptyColArrayWith:(NSObject *)element rows:(int)rows {
    if (!element) return nil;

    NSMutableArray *arr = [NSMutableArray array];
    for (int i = 0; i < rows; i++)
        [arr addObject:element];

    return [arr copy];
}

#pragma mark - Convert Lattice → Packed Binary Data

/// Convert lattice array (0/1 matrix) into packed bytes.
/// Every 8 vertical pixels are compressed into one byte.
/// Example:
/// Pixels: [1 0 1 0 0 0 1 1] → Byte: 0b10100011
+ (NSData *)checkedDataWithLatticeArray:(NSArray *)finalArray {

    NSMutableData *data = [NSMutableData data];

    for (NSArray *column in finalArray) {

        int rowCount = (int)column.count;
        int byteCount = ceil(rowCount / 8.0);

        for (int j = 0; j < byteCount; j++) {

            uint8_t sum = 0;

            for (int k = j * 8; k < (j + 1) * 8 && k < rowCount; k++) {

                NSNumber *n = column[k];

                // Pack bit into byte (MSB to LSB)
                sum += n.intValue * (1 << ((j + 1) * 8 - 1 - k));
            }

            [data appendBytes:&sum length:1];
        }
    }

    return data;
}

@end
