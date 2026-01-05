#import "JLog.h"

// Default log output function
void DefaultLogOutput(const char *fmt, ...) {
    va_list args;
    va_start(args, fmt);
    
    // Convert char * to NSString
    NSString *formatString = [NSString stringWithUTF8String:fmt];
    NSString *output = [[NSString alloc] initWithFormat:formatString arguments:args];
    
    NSLog(@"%@", output);
    
    va_end(args);
}

// Initialize g_pfnPrintLog with an empty implementation initially
static void iPrintLogEmpty(const char *fmt, ...) {
    // No output
}

JLogPrintf g_pfnPrintLog = iPrintLogEmpty; // Default to empty function

void jLogInit(JLogPrintf pfnPrintLog) {
    if (pfnPrintLog != NULL) {
        g_pfnPrintLog = pfnPrintLog; // Use custom log function if provided
    } else {
        g_pfnPrintLog = DefaultLogOutput; // Fallback to NSLog
    }
}
