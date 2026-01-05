/**
 * @file    jotupix_color.h
 * @brief   Some commonly used color definitions
 * @note
 *
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */
#ifndef __JOTUPIX_COLOR_H__
#define __JOTUPIX_COLOR_H__

#ifdef __cplusplus
    extern "C" {
#endif
    
#include "jotupix_typedef.h"

typedef enum _JOTUPIX_COLOR_E
{
    E_COLOR_RED = 0x0F00,
    E_COLOR_GREEN = 0x00F0,
    E_COLOR_BLUE = 0x000F,
    E_COLOR_YELLOW = 0x0FF0,
    E_COLOR_CYAN = 0x00FF,
    E_COLOR_PURPlE = 0x0F0F,
    E_CLOR_WHITE = 0x0FFF,
}JOTUPIX_COLOR_E;

#ifdef __cplusplus
}
#endif

#endif // __JOTUPIX_COLOR_H__

