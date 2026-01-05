//
//  JColor.h
//  SDKTest
//
//  Created by go on 12/8/25.
//

#ifndef JColor_h
#define JColor_h

#import <Foundation/Foundation.h>

typedef NS_ENUM(uint16_t, JColor) {
    JColorRed    = 0x0F00,
    JColorGreen  = 0x00F0,
    JColorBlue   = 0x000F,
    JColorYellow = 0x0FF0,
    JColorCyan   = 0x00FF,
    JColorPurple = 0x0F0F,
    JColorWhite  = 0x0FFF,
};

#endif /* JColor_h */
