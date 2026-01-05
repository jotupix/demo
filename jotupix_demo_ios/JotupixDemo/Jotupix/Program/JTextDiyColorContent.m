#import "JTextDiyColorContent.h"
#import "JByteWriter.h"
#import "JColor.h"

@implementation JTextDiyColorContent

#pragma mark - Static Multicolor Data

+ (NSArray<NSNumber *> *)multicolorData {
    return @[
        @(JColorRed),
        @(JColorYellow),
        @(JColorGreen),
        @(JColorCyan),
        @(JColorBlue),
        @(JColorPurple)
    ];
}

+ (uint32_t)multicolorDataSize {
    return (uint32_t)[self multicolorData].count;
}

#pragma mark - Init

- (instancetype)init {
    if (self = [super init]) {
        self.contentType = JContentTypeDIYColor;
        _textData = [NSMutableArray array];
    }
    return self;
}

#pragma mark - Packing

- (NSData *)get {

    JByteWriter *content = [[JByteWriter alloc] init];

    // Content type
    [content put_u8:(uint8_t)self.contentType];

    // 5 bytes reserved
    [content put_repeat:0 count:5];

    [content put_u16:self.moveSpace];
    [content put_u16:self.showX];
    [content put_u16:self.showY];
    [content put_u16:self.showWidth];
    [content put_u16:self.showHeight];
    [content put_u8:self.showMode];
    [content put_u8:self.showSpeed];
    [content put_u8:self.stayTime];

    // Reserved 1 byte
    [content put_u8:0];

    [content put_u16:self.textNum];

    // content.put_u16(m_textAllWide & 0xFFFFF)
    [content put_u16:(self.textAllWide & 0xFFFFF)];

    // First loop: text width list
    for (JTextFont *font in self.textData) {
        [content put_u8:font.textWidth];
    }

    // Second loop: text color list
    for (JTextFont *font in self.textData) {
        [content put_u16:font.textColor];
    }

    // Insert total block size at beginning (include 4 bytes for size itself)
    uint32_t totalSize = (uint32_t)(content.size + 4);

    // Correct usage — insert at pos=0, value=totalSize
    [content insert_u32:0 value:totalSize];

    // Return NSData, not uint8_t*
    return [NSData dataWithBytes:content.buffer length:content.size];
}

@end
