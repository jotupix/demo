// JContentBase.h

#import <Foundation/Foundation.h>

/// Enumeration defining different types of graphical or program content.
/// Each value represents a specific category that may follow different
/// formatting, encoding, or rendering rules when packaged.
typedef NS_ENUM(NSInteger, JContentType) {
    JContentTypeText       = 1,     /// Text content
    JContentTypeGraffiti   = 2,     /// Graffiti or free‑drawn content
    JContentTypeAnimation  = 3,     /// Animated content
    JContentTypeBorder     = 4,     /// Border or frame content
    JContentTypeFullColor  = 5,     /// Full‑color graphic content
    JContentTypeDIYColor   = 6,     /// User‑designed or custom‑color content
    JContentTypeGIF        = 0x0C,  /// GIF image content
};

/// Enumeration representing blending modes used when compositing content.
/// MIX   = blend with existing pixels.
/// COVER = overwrite existing pixels.
typedef NS_ENUM(NSInteger, JBlendType) {
    JBlendTypeMix   = 0,
    JBlendTypeCover = 1,
};

/// Abstract base class for all content objects.
/// This class defines the interface used to generate data formatted
/// according to communication protocol specifications. Subclasses must
/// provide concrete implementations for packaging their content.
@interface JContentBase : NSObject

/// Method that must be overridden by subclasses.
/// Responsible for generating a data buffer containing the packaged
/// content according to protocol requirements.
///
/// @return A data object containing the formatted content bytes.
- (NSData *)get;

/// Stores the content category associated with this object.
/// This value indicates how the content should be interpreted or processed.
@property (nonatomic, assign) JContentType contentType;

@end
