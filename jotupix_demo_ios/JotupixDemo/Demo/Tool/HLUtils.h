//
//  HLUtils.h
//  SDKTest
//
//  Created by go on 12/11/25.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "BleManager.h"
#import "HLHUDHelper.h"

NS_ASSUME_NONNULL_BEGIN

@interface HLUtils : NSObject

+(BOOL)checkBleDevice;

+(NSArray *)generateDataFromImageHeight:(int )row text:(NSString *)text fontSize:(int)fontSize isBold:(BOOL)isBold;

+ (NSArray *)getDataFromFontImage:(UIImage *)image  scale:(CGFloat)scale;

+ (NSArray *)optArrayWithLatticeArray:(NSArray *)latticeArray wordShowHeight:(NSInteger)wordShowHeight fontSpace:(int)fontSpace fontSize:(int)fontSize;

+ (NSData *)checkedDataWithLatticeArray:(NSArray *)finalArray;

@end

NS_ASSUME_NONNULL_END
