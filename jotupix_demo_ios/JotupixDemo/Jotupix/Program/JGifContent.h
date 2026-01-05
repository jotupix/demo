// JGifContent.h

#import <Foundation/Foundation.h>
#import "JContentBase.h"

@interface JGifContent : JContentBase

@property (nonatomic, assign) JBlendType blendType;
@property (nonatomic, assign) uint16_t showX;
@property (nonatomic, assign) uint16_t showY;
@property (nonatomic, assign) uint16_t showWidth;
@property (nonatomic, assign) uint16_t showHeight;

/// Raw GIF data buffer
@property (nonatomic, strong) NSData *gifData;

@end
