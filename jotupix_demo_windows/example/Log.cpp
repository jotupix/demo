#include "pch.h"
#include "Log.h"

Log::PrintLog Log::m_Callback;
void* Log::m_Object;

void Log::Init(void* obj, PrintLog callback)
{
	m_Callback = callback;
	m_Object = obj;
}

void Log::Printf(const CString& str)
{
	m_Callback(m_Object, str);
}

void Log::Printf(const char* fmt, ...)
{
    char buf[1024];

    va_list args;
    va_start(args, fmt);
    vsnprintf(buf, sizeof(buf), fmt, args);
    va_end(args);

    CString str(buf);

    m_Callback(m_Object, str);
}
