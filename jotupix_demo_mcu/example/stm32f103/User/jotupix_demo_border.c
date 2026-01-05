/**
 * @file    jotupix_tick.h
 * @brief   
 * @note
 *
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */
#include "jotupix_demo.h"

const uint8 BORDER[40] = {
0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// 8byte，The reserved bytes are defaulted to 0.
0x01, 									// 1byte，How many items of content does this program consist of?
0x00, 									// 1byte，The reserved bytes are defaulted to 0.

0x00, 0x00, 0x00, 0x1E,					// 4byte，The total length of all the data in this section
E_CONTENT_TYPE_BORDER, 					// 1byte，Indicates the type of this content. The value is fixed at 04.
0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,// 7byte，The reserved bytes are defaulted to 0.
0x00,									// 1byte，Mixing method: 0 - Mixed, 1 - Overlaid
0x00, 0x00,								// 2byte，This content indicates the starting column, which is equivalent to the x-coordinate.
0x00, 0x00,								// 2byte，This content indicates the starting row, which is equivalent to the Y coordinate.
0x00, 0x60,								// 2byte，This content indicates the width.
0x00, 0x10,								// 2byte，This content shows a high degree.
0x01,									// 1byte，Border display effect: 0 - Reserved, 1 - Clockwise rotation, 2 - Counterclockwise rotation, 3 - Blinking, 4 - Stopped
0xC8,									// 1byte，Border change speed, range: 1 to 255 (the higher the value, the faster the speed)
0x01,									// 1byte，Border content height, value range: 1 - half of the display height
0x00, 0x04, 							// 2byte，The total length of the border data
// nbyte，Border display data
0x0F,0x00, 0x00,0x00,
};
	
