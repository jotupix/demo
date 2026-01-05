/**
 * @file    jotupix_log.h
 * @brief   log
 * @note
 *
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */

#ifndef __JOTUPIX_LOG_H__
#define __JOTUPIX_LOG_H__

#ifdef __cplusplus
extern "C" {
#endif

#define     DEBUG_LEVEL              0
#define     INFO_LEVEL               1
#define     ERROR_LEVEL              2
#define     CLOSE_LEVEL              3

#ifndef JTDEBUG_LEVEL
#define JTDEBUG_LEVEL                DEBUG_LEVEL
#endif

// Log output callback
typedef void (*jotupix_log_print_f)(const char* fmt, ...);

#define JTPrint(format, ...)       g_pfnPrintLog(format, ## __VA_ARGS__)

extern jotupix_log_print_f g_pfnPrintLog;

#define _STR(T)         #T
#define JTTAG_STR(T)    "["_STR(T)"]"

#if (DEBUG_LEVEL >= JTDEBUG_LEVEL)
#define JTLogD(TAG, FORMAT, ...)  JTPrint("[Debug]:" JTTAG_STR(TAG) FORMAT, ## __VA_ARGS__)
#else
#define JTLogD(...) (void)(0)
#endif

#if (INFO_LEVEL >= JTDEBUG_LEVEL)
#define JTLogI(TAG, FORMAT, ...)  JTPrint("[Info]:" JTTAG_STR(TAG) FORMAT, ## __VA_ARGS__)
#else
#define JTLogI(...) (void)(0)
#endif

#if (ERROR_LEVEL >= JTDEBUG_LEVEL)
#define JTLogE(TAG, FORMAT, ...)  JTPrint("[Error]:" JTTAG_STR(TAG) FORMAT, ## __VA_ARGS__)
#else
#define JTLogE(...) (void)(0)
#endif

#if (JTDEBUG_LEVEL < CLOSE_LEVEL)
#define JTLogChar(c)  putchar(c)
#else
#define JTLogChar(c)  (void)(0)
#endif

/**
 * @brief This function is used to register a print function that needs to output logs to a specified window.
 *
 * @param[in] pfnPrintLog printf function
 *
 * @return
 */
void jotupix_log_register_print(jotupix_log_print_f pfnPrintLog);

#ifdef __cplusplus
}
#endif

#endif // __JOTUPIX_LOG_H__

