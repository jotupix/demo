#include "JLog.h"

static void iPrintLogEmpty(const char* fmt, ...);

JLogPrintf g_pfnPrintLog = iPrintLogEmpty;

static void iPrintLogEmpty(const char* fmt, ...)
{
    //
}

void JLogInit(JLogPrintf pfnPrintLog)
{
    g_pfnPrintLog = pfnPrintLog;
}