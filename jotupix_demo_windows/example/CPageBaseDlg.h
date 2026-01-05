#pragma once
#include "afxdialogex.h"
#include "CTabPageBaseDlg.h"

#include "resource.h"

// CPageBaseDlg dialog box

class CPageBaseDlg : public CTabPageBaseDlg
{
	DECLARE_DYNAMIC(CPageBaseDlg)

public:
	CPageBaseDlg(CWnd* pParent = nullptr); 
	virtual ~CPageBaseDlg();

#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_PAGE_BASE };
#endif

protected:
	virtual BOOL OnInitDialog();
	virtual void DoDataExchange(CDataExchange* pDX);
	virtual void OnTimer(UINT_PTR nIDEvent);

	BOOL m_bStatus;

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonSwitch();
	afx_msg void OnEnChangeEditBn();
	CButton m_btnSwitch;
	afx_msg void OnCbnSelchangeComboFlip();
	CComboBox m_comboFlip;
	CEdit m_editBn;
	afx_msg void OnBnClickedButton1();
	afx_msg void OnBnClickedButtonReset();
};
