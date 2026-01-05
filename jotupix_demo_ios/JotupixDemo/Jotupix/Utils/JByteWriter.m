// JByteWriter.m
#import "JByteWriter.h"

@interface JByteWriter ()
{
    NSMutableData *_data;   /// Internal dynamic byte buffer
}
@end

@implementation JByteWriter

/// Initializes the writer with an empty mutable byte buffer.
- (instancetype)init {
    if (self = [super init]) {
        _data = [[NSMutableData alloc] init];
    }
    return self;
}

/// Returns a pointer to the internal mutable byte array.
- (uint8_t *)buffer {
    return (uint8_t *)_data.mutableBytes;
}

/// Returns the current number of bytes stored in the buffer.
- (NSUInteger)size {
    return _data.length;
}

#pragma mark - Basic Writers

/// Writes an 8‑bit unsigned integer.
- (void)put_u8:(uint8_t)v {
    [_data appendBytes:&v length:1];
}

/// Writes a 16‑bit unsigned integer in big‑endian order.
- (void)put_u16:(uint16_t)v {
    uint8_t b[2];
    b[0] = (v >> 8) & 0xFF;
    b[1] = (v >> 0) & 0xFF;
    [_data appendBytes:b length:2];
}

/// Writes a 32‑bit unsigned integer in big‑endian order.
- (void)put_u32:(uint32_t)v {
    uint8_t b[4];
    b[0] = (v >> 24) & 0xFF;
    b[1] = (v >> 16) & 0xFF;
    b[2] = (v >> 8)  & 0xFF;
    b[3] = (v >> 0)  & 0xFF;
    [_data appendBytes:b length:4];
}

#pragma mark - Write raw bytes

/// Writes a raw pointer byte array into the buffer.
- (void)put_bytes:(const uint8_t *)data length:(NSUInteger)len {
    if (data && len > 0) {
        [_data appendBytes:data length:len];
    }
}

/// Writes the bytes contained in an NSData object.
- (void)put_bytes:(NSData *)data {
    if (data.length > 0) {
        [_data appendData:data];
    }
}

#pragma mark - Repeated bytes

/// Appends a byte value repeated a specific number of times.
- (void)put_repeat:(uint8_t)value count:(NSUInteger)count {
    if (count == 0) return;
    
    NSMutableData *temp = [NSMutableData dataWithLength:count];
    memset(temp.mutableBytes, value, count);
    [_data appendData:temp];
}

#pragma mark - Insert uint32 (big-endian)

/// Inserts a 32‑bit unsigned integer in big‑endian order at a given position.
/// Performs a bounds check and expands the buffer before inserting.
- (void)insert_u32:(NSUInteger)pos value:(uint32_t)value {
    if (pos > _data.length) {
        @throw [NSException exceptionWithName:@"ByteWriterOutOfRange"
                                       reason:@"insert_u32: position out of range"
                                     userInfo:nil];
    }

    // Expand by 4 bytes to make space.
    [_data increaseLengthBy:4];

    uint8_t *base = (uint8_t *)_data.mutableBytes;

    // Shift existing bytes to the right.
    memmove(base + pos + 4,
            base + pos,
            _data.length - pos - 4);

    // Write 32‑bit value in big‑endian format.
    base[pos + 0] = (value >> 24) & 0xFF;
    base[pos + 1] = (value >> 16) & 0xFF;
    base[pos + 2] = (value >> 8)  & 0xFF;
    base[pos + 3] = (value >> 0)  & 0xFF;
}

@end
