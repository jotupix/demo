/**
 * @file    jotupix_content_base.h
 * @brief   
 * @note
 *
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */
#ifndef __JOTUPIX_CONTENT_BASE_H__
#define __JOTUPIX_CONTENT_BASE_H__

#ifdef __cplusplus
    extern "C" {
#endif
    
#include "jotupix_typedef.h"


/**
 * @brief Content type enumeration.
 *
 * Indicates the specific category of graphical content being transferred.
 * Each type corresponds to different encoding or rendering rules.
 */
typedef enum _JOTUPIX_CONTENT_TYPE_E
{
    E_CONTENT_TYPE_TEXT = 1,
    E_CONTENT_TYPE_GRAFFITI,
    E_CONTENT_TYPE_ANIMATION,
    E_CONTENT_TYPE_BORDER,
    E_CONTENT_TYPE_FULLCOLOR,
    E_CONTENT_TYPE_DIYCOLOR,
    E_CONTENT_TYPE_GIF = 0x0c,
}JOTUPIX_CONTENT_TYPE_E;

typedef enum _JOTUPIX_BLEND_TYPE_E
{
    E_BLEND_TYPE_MIX = 0,
    E_BLEND_TYPE_COVER,
}JOTUPIX_BLEND_TYPE_E;

#ifdef __cplusplus
}
#endif

#endif // __JOTUPIX_CONTENT_BASE_H__

