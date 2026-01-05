// JProgramContent.h
#import <Foundation/Foundation.h>
#import "JContentBase.h"

/// A container class used to manage multiple content objects.
/// Provides methods to add content, clear the list,
/// and serialize all content into a protocol‑formatted byte sequence.
@interface JProgramContent : NSObject

/// Adds a content object into the internal list.
/// Comparable to adding a content element into a managed container.
- (void)addContent:(JContentBase *)content;

/// Removes all stored content objects.
- (void)clear;

/// Serializes all stored content into a single data buffer.
/// The returned data follows the required protocol format.
- (NSData *)get;

@end
