#pragma once
class Log
{
public:
	using PrintLog = void(*)(void* obj, const CString& message);

	static void Init(void *obj, PrintLog callback);

	static void Printf(const CString& str);

	static void Printf(const char* fmt, ...);

private:
	static PrintLog m_Callback;
	static void* m_Object;
};

