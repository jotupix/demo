/**
 * @file    jotupix_log.c
 * @brief   log
 * @note
 *
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */
#include "jotupix_log.h"

static void iPrintLogEmpty(const char* fmt, ...);

jotupix_log_print_f g_pfnPrintLog = iPrintLogEmpty;

void jotupix_log_register_print(jotupix_log_print_f pfnPrintLog)
{
    g_pfnPrintLog = pfnPrintLog;
}

static void iPrintLogEmpty(const char* fmt, ...)
{
    //
}

