/**
 * @file    jotupix_tick.h
 * @brief   
 * @note
 *
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */
#include "jotupix_content_base.h"
/*
Different display modes of text or graffiti：
	value 	mode	 							notes
	1 		static								The content is displayed frame by frame in a static manner, cycling continuously.
	2 		Continuous left shift 				Content moves leftward in a loop and is displayed continuously.
	3 		Continuous rightward movement 		The content moves to the right in a loop and is displayed continuously.
	4 		up 									Content moves upward in a loop and is displayed continuously.
	5 		down 								The content moves downward in a loop and is displayed continuously.
	6 		stacking 							The content piles up row by row downward.
	7		scroll 								The content unfolds cyclically from the center to both sides like a scroll painting.
	8 		flicker 							Each frame content flashes once and displays in a loop.
	9		Move to the left 					The content moves to the left in a loop and is displayed continuously. After each screen is shown, there is a pause for a specified duration.
	10 		Move to the right 					The content moves to the right in a loop and is displayed continuously. After each screen is shown, there is a pause for a specified duration.
	11 		Cover the left side
	12 		Cover the right side
	13 		niveau stecken
*/
extern const uint8 GRAFFITI[2086];
extern const uint8 ANIMATION[12334];
extern const uint8 BORDER[40];
extern const uint8 FULLCOLOR[332];
extern const uint8 DIYCOLOR[286];
extern const uint8 GIF[25648];

