// JTextFont.h

#import <Foundation/Foundation.h>

typedef NS_ENUM(uint8_t, JTextFontType) {
    JTextFontTypeMonochrome = 0,  // MONOCHROME
    JTextFontTypeMulticolor = 1,  // MULTICOLOR
};

@interface JTextFont : NSObject

/// Text type (Monochrome / Multicolor)
@property (nonatomic, assign) JTextFontType textType;

/// Width of the font data (1, 2, or more)
@property (nonatomic, assign) uint8_t textWidth;

/// Only valid in monochrome mode
@property (nonatomic, assign) uint16_t textColor;

/// Raw dot-map data (vector<uint8_t>)
@property (nonatomic, strong) NSData *showData;

@end
