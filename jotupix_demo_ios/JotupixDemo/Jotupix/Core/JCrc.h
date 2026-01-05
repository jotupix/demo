// JCrc.h
#import <Foundation/Foundation.h>

@interface JCrc : NSObject

/// Reset the CRC calculation state.
/// This restores the internal CRC32 value to the default start value (0xFFFFFFFF),
- (void)reset;

/// Update the CRC32 accumulator using a buffer of bytes.
///
/// @param data Pointer to the input data buffer
/// @param len  Number of bytes to process
///
/// @return Updated CRC32 value after processing
- (uint32_t)calculateWithBytes:(const uint8_t *)data length:(uint32_t)len;

/// Retrieve the current CRC32 value.
///
/// @return Current CRC32 accumulated value
- (uint32_t)getValue;

/// Get pointer to the static CRC32 lookup table.
+ (const uint32_t *)crcTable;

/// Get the number of entries in the CRC32 lookup table.
+ (NSUInteger)crcTableSize;

@end
