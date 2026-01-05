// JGifContent.m

#import "JGifContent.h"
#import "JByteWriter.h"

@implementation JGifContent

- (instancetype)init {
    if (self = [super init]) {
        self.contentType = JContentTypeGIF;
    }
    return self;
}

- (NSData *)get {

    JByteWriter *content = [[JByteWriter alloc] init];

    [content put_u8:(uint8_t)self.contentType];

    [content put_repeat:0 count:7];

    [content put_u8:(uint8_t)self.blendType];

    [content put_u8:0];

    [content put_u16:self.showX];
    [content put_u16:self.showY];
    [content put_u16:self.showWidth];
    [content put_u16:self.showHeight];

    [content put_u32:(uint32_t)self.gifData.length];

    [content put_bytes:self.gifData];

    uint32_t totalSize = (uint32_t)(content.size + 4);
    [content insert_u32:0 value:totalSize];

    // Return NSData, not uint8_t*
    return [NSData dataWithBytes:content.buffer length:content.size];
}

@end
