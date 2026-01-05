#pragma once
#include "afxdialogex.h"

#include <atomic>
#include <functional>

#include "resource.h"

#include "CTabPageBaseDlg.h"

class ISendProgressCancelCallback
{
public:
	virtual void onCancelCallbck() = 0;
};

class CSendProgressDlg : public CTabPageBaseDlg
{
	DECLARE_DYNAMIC(CSendProgressDlg)

public:
	CSendProgressDlg(CWnd* pParent = nullptr);
	virtual ~CSendProgressDlg();

	void UpdateProgress(int percent);

	void SetCancelCallback(ISendProgressCancelCallback *pfnCallback) {
		m_cancelCallback = pfnCallback;
	}

#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_SEND_PROGRESS };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);

	virtual BOOL OnInitDialog();

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedCancel();

private:
	CProgressCtrl m_progress;
	ISendProgressCancelCallback *m_cancelCallback;
};
