/**
 * @file    jotupix_tick.h
 * @brief   
 * @note
 *
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */

#ifndef __JOTUPIX_TICK_H__
#define __JOTUPIX_TICK_H__

#ifdef __cplusplus
extern "C" {
#endif

#define time_after(a, b) ((int)(b) - (int)(a) < 0) 
#define time_before(a,b) time_after(b,a)

#define time_after_eq(a, b) ((int)(a) - (int)(b) >= 0) 
#define time_before_eq(a, b) time_after_eq(b, a)

#define time_get_next_tick(s, t) ((s)+(t)) 

#ifdef __cplusplus
}
#endif


#endif //__JOTUPIX_TICK_H__

