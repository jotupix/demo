#pragma once

#include "resource.h"

#include "CTabPageBaseDlg.h"

class CAboutDlg : public CDialogEx
{
public:
	CAboutDlg();

#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_ABOUTBOX };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

protected:
	DECLARE_MESSAGE_MAP()
public:
};

