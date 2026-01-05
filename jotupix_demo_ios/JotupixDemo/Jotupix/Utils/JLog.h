#import <Foundation/Foundation.h>

#ifdef __cplusplus
extern "C" {
#endif

#define JDEBUG_LEVEL 0
#define JINFO_LEVEL  1
#define JERROR_LEVEL 2
#define JCLOSE_LEVEL 3

#ifndef JLOG_LEVEL
#define JLOG_LEVEL JDEBUG_LEVEL
#endif

// Log output callback
typedef void (*JLogPrintf)(const char *fmt, ...);

extern JLogPrintf g_pfnPrintLog;

#define JPrint(format, ...) g_pfnPrintLog(format, ## __VA_ARGS__)

#if (JDEBUG_LEVEL >= JLOG_LEVEL)
#define JLogD(TAG, FORMAT, ...) JPrint("[Debug]:[%s] " FORMAT, TAG, ## __VA_ARGS__)
#else
#define JLogD(...) (void)(0)
#endif

#if (JINFO_LEVEL >= JLOG_LEVEL)
#define JLogI(TAG, FORMAT, ...) JPrint("[Info]:[%s] " FORMAT, TAG, ## __VA_ARGS__)
#else
#define JLogI(...) (void)(0)
#endif

#if (JERROR_LEVEL >= JLOG_LEVEL)
#define JLogE(TAG, FORMAT, ...) JPrint("[Error]:[%s] " FORMAT, TAG, ## __VA_ARGS__)
#else
#define JLogE(...) (void)(0)
#endif

/**
 * @brief This function is used to register a print function that needs to output logs to a specified window.
 * @param[in] pfnPrintLog printf function
 * @return
 */
void jLogInit(JLogPrintf pfnPrintLog);

#ifdef __cplusplus
}
#endif
