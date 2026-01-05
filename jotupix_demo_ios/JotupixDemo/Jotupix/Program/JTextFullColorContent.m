// JTextFullColorContent.m
#import "JTextFullColorContent.h"
#import "JByteWriter.h"

@implementation JTextFullColorContent

#pragma mark - Static Color Data

+ (NSArray<NSNumber *> *)textFullColorRainbow {
    return @[
        @0x0F00, @0x0F20, @0x0F40, @0x0F60, @0x0F80, @0x0FA0, @0x0FC0, @0x0FF0,
        @0x0CF0, @0x0AF0, @0x08F0, @0x06F0, @0x04F0, @0x02F0, @0x00F0, @0x00F2,
        @0x00F4, @0x00F6, @0x00F8, @0x00FA, @0x00FC, @0x00FF, @0x00CF, @0x00AF,
        @0x008F, @0x006F, @0x004F, @0x002F, @0x000F, @0x020F, @0x040F, @0x060F,
        @0x080F, @0x0A0F, @0x0C0F, @0x0F0F, @0x0F0C, @0x0F0A, @0x0F08, @0x0F06,
        @0x0F04, @0x0F02, @0x0F00
    ];
}

+ (uint32_t)textFullColorRainbowSize {
    return (uint32_t)[self textFullColorRainbow].count;
}

+ (NSArray<NSNumber *> *)textFullColorThree {
    return @[@0x00FF, @0x0F0F, @0x0FF0];
}

+ (uint32_t)textFullColorThreeSize {
    return (uint32_t)[self textFullColorThree].count;
}

#pragma mark - Init

- (instancetype)init {
    if (self = [super init]) {
        self.contentType = JContentTypeFullColor;   
        _textFullColor = [NSMutableArray array];
    }
    return self;
}

#pragma mark - Packing

- (NSData *)get {

    JByteWriter *content = [[JByteWriter alloc] init];

    // content type
    [content put_u8:(uint8_t)self.contentType];

    // reserved 7 bytes
    [content put_repeat:0 count:7];

    [content put_u16:self.showX];
    [content put_u16:self.showY];
    [content put_u16:self.showWidth];
    [content put_u16:self.showHeight];

    [content put_u8:(uint8_t)self.textColorType];
    [content put_u8:self.textColorSpeed];
    [content put_u8:(uint8_t)self.textColorDir];
    [content put_u8:0]; // reserved

    // length = number of colors * 2
    uint16_t byteLen = (uint16_t)(self.textFullColor.count * 2);
    [content put_u16:byteLen];

    // Write colors
    for (NSNumber *num in self.textFullColor) {
        [content put_u16:(uint16_t)num.unsignedIntValue];
    }

    // Insert total size at front
    uint32_t totalSize = (uint32_t)(content.size + 4);
    // Correct usage — insert at pos=0, value=totalSize
    [content insert_u32:0 value:totalSize];

    // Return NSData, not uint8_t*
    return [NSData dataWithBytes:content.buffer length:content.size];
}

@end
