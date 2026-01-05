// JProgramContent.m
#import "JProgramContent.h"
#import "JByteWriter.h"

@interface JProgramContent ()
@property (nonatomic, strong) NSMutableArray<JContentBase *> *contentList;   /// Internal storage for content objects
@end

@implementation JProgramContent

/// Initializes the container with an empty content list.
- (instancetype)init {
    if (self = [super init]) {
        _contentList = [NSMutableArray array];
    }
    return self;
}

#pragma mark - API

/// Adds a content object to the container.
/// Ignores nil values.
- (void)addContent:(JContentBase *)content {
    if (content) {
        [self.contentList addObject:content];
    }
}

/// Clears all content objects from the container.
- (void)clear {
    [self.contentList removeAllObjects];
}

/// Serializes all content items into a single data block following the protocol layout.
///
/// Layout:
///   [0..7]  : 8 reserved bytes
///   [8]     : number of content blocks
///   [9]     : reserved byte
///   [...]   : serialized content blocks (in the same order added)
///
/// Each content object is responsible for producing its own formatted data.
- (NSData *)get {

    JByteWriter *proContent = [[JByteWriter alloc] init];

    // reserved (8 bytes)
    [proContent put_repeat:0 count:8];

    // number of content blocks
    [proContent put_u8:(uint8_t)self.contentList.count];

    // reserved
    [proContent put_u8:0];

    // write each content block
    for (JContentBase *content in self.contentList) {
        NSData *block = [content get];
        if (block) {
            [proContent put_bytes:block];
        }
    }

    // return NSData, NOT a raw pointer
    return [NSData dataWithBytes:proContent.buffer length:proContent.size];
}

@end
