/**
 * @file    jotupix_typedef.h
 * @brief   Data type definition
 * @note
 *  - All functions are NOT thread-safe; synchronization is required
 *    in multi-threaded environments.
 *  - The SDK requires 4KB of memory.
 *
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */

#ifndef __JOTUPIX_TYPEDEF_H__
#define __JOTUPIX_TYPEDEF_H__

#include <string.h>

#ifdef __cplusplus
extern "C" {
#endif

#if defined(_WIN32) && defined(_WIN64)
typedef int					 BOOL;
#else
typedef unsigned char        BOOL;
#endif

typedef unsigned char         uint8;
typedef signed char           int8;

typedef unsigned short		uint16;
typedef unsigned int		uint32;
typedef signed short		int16;
typedef signed int			int32;

#ifndef FALSE
#define FALSE   0
#endif

#ifndef TRUE
#define TRUE    (!FALSE)
#endif

#ifndef NULL
#define NULL    0
#endif

#ifndef ON
#define ON      1
#endif

#ifndef OFF
#define OFF     0
#endif

#include "jotupix_log.h"
#include "jotupix_config.h"

#ifdef __cplusplus
}
#endif

#endif // __JOTUPIX_TYPEDEF_H__