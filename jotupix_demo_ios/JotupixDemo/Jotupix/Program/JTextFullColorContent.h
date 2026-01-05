// JTextFullColorContent.h
#import <Foundation/Foundation.h>
#import "JContentBase.h"

@interface JTextFullColorContent : JContentBase

typedef NS_ENUM(uint8_t, JTextFullColorType) {
    JTextFullColorTypeHorScroll = 1,
    JTextFullColorTypeStatic,
    JTextFullColorTypeVertScroll,
    JTextFullColorTypeVertRelativeScroll,
    JTextFullColorTypeJump,
    JTextFullColorTypeHorCover,
    JTextFullColorTypeHorDiagonalScroll,
    JTextFullColorTypeRotation
};

typedef NS_ENUM(uint8_t, JTextFullColorDir) {
    JTextFullColorDirLeft = 0,
    JTextFullColorDirRight,
    JTextFullColorDirUp,
    JTextFullColorDirDown,
    JTextFullColorDirCenter,
    JTextFullColorDirSide
};

/// Static color arrays 
+ (NSArray<NSNumber *> *)textFullColorRainbow;
+ (uint32_t)textFullColorRainbowSize;

+ (NSArray<NSNumber *> *)textFullColorThree;
+ (uint32_t)textFullColorThreeSize;

@property (nonatomic, assign) uint16_t showX;
@property (nonatomic, assign) uint16_t showY;
@property (nonatomic, assign) uint16_t showWidth;
@property (nonatomic, assign) uint16_t showHeight;

@property (nonatomic, assign) JTextFullColorType textColorType;
@property (nonatomic, assign) uint8_t textColorSpeed;
@property (nonatomic, assign) JTextFullColorDir textColorDir;

/// Full color array (equivalent to vector<uint16_t>)
@property (nonatomic, strong) NSArray<NSNumber *> *textFullColor;

@end
