#pragma once

#include "CPageBaseDlg.h"
#include "CPageAniDlg.h"
#include "serial.h"
#include "CPageTextDlg.h"

class CJotuPixDlg : public CDialogEx, public IJSend, public IJGetDevInfoCallback
{
public:
	CJotuPixDlg(CWnd* pParent = nullptr);

#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_MAIN };
#endif

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);
	virtual void OnTimer(UINT_PTR nIDEvent);

	void onUartReadEvent(const char* pu8Data, uint32_t u32Len);

	int Send(const uint8_t* pu8Data, uint32_t u32Len) override;

	void onEvent(JInfo devInfo) override;

protected:
	HICON m_hIcon;

	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	afx_msg void OnSize(UINT nType, int cx, int cy);
	afx_msg void OnAppAbout();
	afx_msg void OnDestroy();

	void Log(const CString& msg);
	void Log(const CString& msg, bool bBreak);

	DECLARE_MESSAGE_MAP()

private:
	CMenu m_Menu;
	CStatusBar	m_Statusbar;
	CFont m_fontStatus;
	CComboBox m_listUartPort;
	CComboBox m_listUartBaudrate;
	CEdit m_logEdit;
	CButton m_btnConnect;
	CTabCtrl m_tabCtrl;

	CPageBaseDlg m_pageBase;
	CPageAniDlg m_pageAni;
	CPageTextDlg m_pageText;

	int m_currentTab;

	// uart status
	BOOL m_isOpen;

	int m_rxCount;
	int m_txCount;

	//CSerialPort m_SerialPort;
	serial::Serial m_Serial;
	int m_ReceiveTimeoutMS;
	CString m_currentOpenedPort;

	void InitUart();

	static void PrintfCallback(void* Object, const CString& msg);

	CString CharArrayToHexString(const char* data, size_t len);

	uint32_t m_u32CurrMs;

	std::vector<CString> m_cachedPorts;

	void RefreshSerialPortList();

public:
	afx_msg void OnBnClickedButtonOpr();
	afx_msg void OnBnClickedButtonSavelogo();
	afx_msg void OnBnClickedButtonClearlog();
	afx_msg void OnTcnSelchangeTabPro(NMHDR* pNMHDR, LRESULT* pResult);
	afx_msg void OnStatusBarUrlClicked();

	virtual void OnOK() override {}
	//virtual void OnCancel() override {}


	// If there are multiple devices, multiple objects need to be created.
	static JotuPix m_sJotupixCtx;
};
