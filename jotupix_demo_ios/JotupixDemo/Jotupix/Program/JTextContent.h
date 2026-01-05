// JTextContent.h
#import <Foundation/Foundation.h>
#import "JContentBase.h"
#import "JTextFont.h"

@interface JTextContent : JContentBase

@property (nonatomic, assign) uint16_t bgColor;
@property (nonatomic, assign) JBlendType blendType;

@property (nonatomic, assign) uint16_t showX;
@property (nonatomic, assign) uint16_t showY;
@property (nonatomic, assign) uint16_t showWidth;
@property (nonatomic, assign) uint16_t showHeight;

@property (nonatomic, assign) uint8_t showMode;
@property (nonatomic, assign) uint8_t showSpeed;
@property (nonatomic, assign) uint8_t stayTime;

@property (nonatomic, assign) uint16_t moveSpace;
@property (nonatomic, assign) uint16_t textNum;
@property (nonatomic, assign) uint32_t textAllWide;

/// Equivalent to std::vector<JTextFont>
@property (nonatomic, strong) NSMutableArray<JTextFont *> *textData;

@end
