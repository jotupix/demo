// JByteWriter.h
#pragma once

#import <Foundation/Foundation.h>
#import <stdint.h>

/// A utility class used to sequentially build a byte buffer.
/// Provides methods for writing integer values in big‑endian format,
/// raw bytes, repeated bytes, and inserting data at specific positions.
@interface JByteWriter : NSObject

/// Pointer to the internal mutable byte buffer.
/// This reflects the underlying storage where bytes are written.
@property (nonatomic, readonly) uint8_t *buffer;

/// Current size of the written byte buffer.
@property (nonatomic, readonly) NSUInteger size;

// Write basic integer values in big‑endian format.

/// Writes a single 8‑bit unsigned integer.
- (void)put_u8:(uint8_t)v;

/// Writes a 16‑bit unsigned integer in big‑endian byte order.
- (void)put_u16:(uint16_t)v;

/// Writes a 32‑bit unsigned integer in big‑endian byte order.
- (void)put_u32:(uint32_t)v;

// Write raw byte arrays.

/// Writes a raw byte buffer of specified length.
- (void)put_bytes:(const uint8_t *)data length:(NSUInteger)len;

/// Writes the contents of an NSData object to the buffer.
- (void)put_bytes:(NSData *)data;

// Write repeated bytes.

/// Appends a byte value repeated a given number of times.
- (void)put_repeat:(uint8_t)value count:(NSUInteger)count;

// Insert 32‑bit big‑endian integer at a specific position.

/// Inserts a 32‑bit unsigned integer in big‑endian order at the given offset.
/// Throws an exception if the position exceeds the current buffer size.
- (void)insert_u32:(NSUInteger)pos value:(uint32_t)value;

@end
