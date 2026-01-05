// JLzss.h
#import <Foundation/Foundation.h>

#define LZSS_N           512
#define LZSS_F           18
#define LZSS_THRESHOLD   2
#define LZSS_NIL         LZSS_N

@interface JLzss : NSObject

/// Perform LZSS encoding
- (NSData *)encode:(const uint8_t *)data length:(uint32_t)len;

@end
