#pragma once
#include "afxdialogex.h"
#include "GifViewer.h"
#include "jotupix.h"

#include <vector>
#include <cstdint>
#include <afx.h>
#include "CSendProgressDlg.h"
#include "CTabPageBaseDlg.h"
#include "CProgramBase.h"

#include "resource.h"

// CPageAniDlg dialog box

class CPageAniDlg : public CTabPageBaseDlg, public CProgramBase
{
	DECLARE_DYNAMIC(CPageAniDlg)

public:
	CPageAniDlg(CWnd* pParent = nullptr); 
	virtual ~CPageAniDlg();

	// Dialog box data
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_PAGE_ANI };
#endif

protected:
	virtual BOOL OnInitDialog();
	virtual void DoDataExchange(CDataExchange* pDX);

	DECLARE_MESSAGE_MAP()

public:
	afx_msg void OnBnClickedButtonSelectGif();
	afx_msg void OnBnClickedButtonGifSend();

	CButton m_btnSelect;
	CButton m_btnSend;
	CString m_gifPath;
	CGifViewer m_gifViewer;

private:
};
