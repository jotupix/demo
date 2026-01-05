#import "JTextContent.h"
#import "JByteWriter.h"

@implementation JTextContent

- (instancetype)init {
    if (self = [super init]) {
        self.contentType = JContentTypeText;  
        _textData = [NSMutableArray array];
    }
    return self;
}

- (NSData *)get {

    JByteWriter *content = [[JByteWriter alloc] init];

    // content type
    [content put_u8:(uint8_t)self.contentType];

    // reserved = 5 bytes
    [content put_repeat:0 count:5];

    [content put_u16:self.bgColor];
    [content put_u8:(uint8_t)self.blendType];
    [content put_u16:self.showX];
    [content put_u16:self.showY];
    [content put_u16:self.showWidth];
    [content put_u16:self.showHeight];
    [content put_u8:self.showMode];
    [content put_u8:self.showSpeed];
    [content put_u8:self.stayTime];
    [content put_u16:self.moveSpace];
    [content put_u16:self.textNum];
    [content put_u32:self.textAllWide];

    // Each font block
    for (JTextFont *font in self.textData) {
        [content put_u8:font.textWidth];
        [content put_u8:(uint8_t)font.textType];
        [content put_bytes:font.showData];
    }

    // Prepend total block size (includes size of this u32)
    uint32_t totalSize = (uint32_t)(content.size + 4);

    // Correct usage: insert_u32(pos, value)
    [content insert_u32:0 value:totalSize];

    // Must return NSData, not uint8_t*
    return [NSData dataWithBytes:content.buffer length:content.size];
}

@end
