#pragma once

#ifndef __AFXWIN_H__
	#error "Include 'pch.h' before including this file to generate PCH."
#endif

#include "resource.h"		// Main symbol


// CJotuPixApp:
// For the implementation of this class, please refer to JotuPix.cpp
//

class CJotuPixApp : public CWinApp
{
public:
	CJotuPixApp();

public:
	virtual BOOL InitInstance();

	DECLARE_MESSAGE_MAP()
};

extern CJotuPixApp theApp;
