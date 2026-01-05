#include "pch.h"
#include "framework.h"
#include "JotuPixApp.h"
#include "JotuPixDlg.h"
#include "afxdialogex.h"
#include "CAboutDlg.h"
#include "jotupix.h"
#include "Log.h"
//#include "DeviceInfo.h"
#include "JDeviceManager.h"
#include "JLog.h"

#pragma comment(lib, "SetupAPI.lib")

using std::string;
using std::exception;
using std::endl;
using std::vector;

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

#define JOTUPIX_THREAD_TIMER_ID  2
#define JOTUPIX_THREAD_TIMER_TICK		100  // ms

#define TIMER_ID_REFRESH_PORTS  101
#define TIMER_ID_UART_READ		102

#define TIMER_ID_UART_TICK		50  // ms

static UINT indicators[] =	//Add status bar: Messages, Flags, Time
{
	IDS_STRING_WEBSET,
	IDS_STRING_VERSION,
};

JotuPix CJotuPixDlg::m_sJotupixCtx;

//CSerialPort CJotuPixDlg::m_SerialPort;

CJotuPixDlg::CJotuPixDlg(CWnd* pParent /*=nullptr*/)
	: CDialogEx(IDD_MAIN, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);

	m_ReceiveTimeoutMS = 0;
	m_currentTab = 0;
	m_isOpen = FALSE;
	m_rxCount = 0;
	m_txCount = 0;
	m_u32CurrMs = 0;
}

void CJotuPixDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);

	DDX_Control(pDX, IDC_COMBO_PORT, m_listUartPort);
	DDX_Control(pDX, IDC_COMBO_BAUDRATE, m_listUartBaudrate);
	DDX_Control(pDX, IDC_TAB_PRO, m_tabCtrl);
}

BEGIN_MESSAGE_MAP(CJotuPixDlg, CDialogEx)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	ON_WM_SIZE()
	ON_WM_DESTROY()
	ON_COMMAND(ID_MENU_ABOUT, &CJotuPixDlg::OnAppAbout)
	ON_BN_CLICKED(IDC_BUTTON_OPR, &CJotuPixDlg::OnBnClickedButtonOpr)
	ON_BN_CLICKED(IDC_BUTTON_SAVELOGO, &CJotuPixDlg::OnBnClickedButtonSavelogo)
	ON_BN_CLICKED(IDC_BUTTON_CLEARLOG, &CJotuPixDlg::OnBnClickedButtonClearlog)
	ON_WM_TIMER()
	ON_NOTIFY(TCN_SELCHANGE, IDC_TAB_PRO, &CJotuPixDlg::OnTcnSelchangeTabPro)
END_MESSAGE_MAP()



void InitConsoleWindows()
{
	/* Allocate a new console for the calling process */
	AllocConsole();

#if _MSC_VER <= 1200 // This is VC6.0.
	freopen("CONOUT$", "w+t", stdout);
#else // This is for VC2003 and above
	FILE* stream;
	freopen_s(&stream, "CONOUT$", "wt", stdout);
#endif // _MSC_VER > 1000
}

BOOL CJotuPixDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();	

	//InitConsoleWindows();

	//SetWindowText(L"JotuPix");
	m_Menu.LoadMenu(IDR_MENU1);  //  IDR_MENU1
	SetMenu(&m_Menu);

	m_Statusbar.Create(this);                 // Create a status bar
	m_Statusbar.SetIndicators(indicators, sizeof(indicators) / sizeof(UINT));   // Set the number of status bar items
	m_Statusbar.SetPaneInfo(0, IDS_STRING_WEBSET, SBPS_POPOUT | SBPS_STRETCH, 120);
	m_Statusbar.SetPaneInfo(1, IDS_STRING_VERSION, SBPS_POPOUT, 120);
	RepositionBars(AFX_IDW_CONTROLBAR_FIRST, AFX_IDW_CONTROLBAR_LAST, 0);// Specify the location of the status bar in the display window

	// Add the "About..." menu item to the system menu.

	// IDM_ABOUTBOX must be within the scope of system commands.
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != nullptr)
	{
		BOOL bNameValid;
		CString strAboutMenu;
		bNameValid = strAboutMenu.LoadString(IDS_ABOUTBOX);
		ASSERT(bNameValid);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// Sets the icon for this dialog box. The frame will automatically change when the application's main window is not a dialog box.
	
	SetIcon(m_hIcon, TRUE);			// Set larger icons
	SetIcon(m_hIcon, FALSE);		// Set small icon

	// TODO: Add additional initialization code here

	m_btnConnect.SubclassDlgItem(IDC_BUTTON_OPR, this);
	m_logEdit.SubclassDlgItem(IDC_EDIT_LOG, this);
	m_logEdit.SetReadOnly(TRUE);
	m_logEdit.LimitText(0);

	// init tab ctrl
	m_tabCtrl.InsertItem(0, _T("Base"));
	m_tabCtrl.InsertItem(1, _T("Ani"));
	m_tabCtrl.InsertItem(2, _T("Text"));

	m_pageBase.Create(IDD_PAGE_BASE, &m_tabCtrl);
	m_pageAni.Create(IDD_PAGE_ANI, &m_tabCtrl);
	m_pageText.Create(IDD_PAGE_TEXT, &m_tabCtrl);


	CRect rc;
	m_tabCtrl.GetClientRect(&rc);
	rc.top += 20; // Leave enough space for the tab label
	m_pageBase.MoveWindow(&rc);
	m_pageAni.MoveWindow(&rc);
	m_pageText.MoveWindow(&rc);

	m_pageBase.ShowWindow(SW_SHOW);
	m_pageAni.ShowWindow(SW_HIDE);
	m_pageText.ShowWindow(SW_HIDE);
	m_currentTab = 0;

	Log::Init(this, PrintfCallback);

	InitUart();

	JDeviceManager::Instance().CreateDevice(this);
	JLogInit(Log::Printf);

	m_u32CurrMs = 0;
	SetTimer(JOTUPIX_THREAD_TIMER_ID, JOTUPIX_THREAD_TIMER_TICK, NULL);

	return TRUE; 
}

void CJotuPixDlg::PrintfCallback(void* Object, const CString& msg)
{
	CJotuPixDlg* pThis = static_cast<CJotuPixDlg*>(Object);
	if (pThis == nullptr)
	{
		pThis->Log(_T("Object is NULL!"));
		return;
	}

	pThis->Log(msg, FALSE);
}

int CJotuPixDlg::Send(const unsigned char* pu8Data, unsigned int u32Len)
{
	if (!m_Serial.isOpen())
	{
		Log(_T("Serial port not open!"));
		return -1;
	}

	int nRet = m_Serial.write(pu8Data, u32Len);
	if (nRet != u32Len)
	{
		Log(_T("Uart write fail, nRet=" + nRet));
		return -1;
	}

	CString str = CharArrayToHexString((const char*)pu8Data, u32Len);

	Log(_T("send: ") + str);

	return 0;
}

void CJotuPixDlg::InitUart()
{
	m_ReceiveTimeoutMS = 0;
	m_isOpen = FALSE;
	m_rxCount = 0;
	m_txCount = 0;

	CString temp;
	// uart baudrate select init
	int BaudRateArray[] = { 9600, 19200, 38400, 57600, 115200, 400000, 1000000 };
	for (int i = 0; i < sizeof(BaudRateArray) / sizeof(int); i++)
	{
		temp.Format(_T("%d"), BaudRateArray[i]);
		m_listUartBaudrate.InsertString(i, temp);
	}

	temp.Format(_T("%d"), 400000);
	m_listUartBaudrate.SetCurSel(m_listUartBaudrate.FindString(0, temp));

	// get uart com port
	m_listUartPort.ResetContent();

	vector<serial::PortInfo> devices_found = serial::list_ports();
	vector<serial::PortInfo>::iterator iter = devices_found.begin();

	TCHAR m_regKeyValue[256];
	while (iter != devices_found.end())
	{
		serial::PortInfo device = *iter++;

		//printf("(%s, %s, %s)\n", device.port.c_str(), device.description.c_str(),
		//	device.hardware_id.c_str());

#ifdef UNICODE
		int iLength;
		const char* _char = device.port.c_str();
		iLength = MultiByteToWideChar(CP_ACP, 0, _char, strlen(_char) + 1, NULL, 0);
		MultiByteToWideChar(CP_ACP, 0, _char, strlen(_char) + 1, m_regKeyValue, iLength);
#else
		strcpy_s(m_regKeyValue, 256, m_portsList[i].portName);
#endif
		m_listUartPort.AddString(m_regKeyValue);
	}

	m_listUartPort.SetCurSel(0);

	SetTimer(TIMER_ID_REFRESH_PORTS, 1000, NULL);
	SetTimer(TIMER_ID_UART_READ, TIMER_ID_UART_TICK, NULL);
}

void CJotuPixDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialogEx::OnSysCommand(nID, lParam);
	}
}

// If you add a minimize button to the dialog box, you'll need the following code
// to draw the icon. For MFC applications using a document/view model,
// this will be done automatically by the framework.

void CJotuPixDlg::OnPaint()
{
	if (IsIconic())
	{
		CPaintDC dc(this); // Device context used for drawing

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		// Center the icon within the workspace rectangle
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// Draw icon
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialogEx::OnPaint();
	}
}

//This function is called by the system to retrieve the cursor when the user drags the minimized window.
//Display.
HCURSOR CJotuPixDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}

void CJotuPixDlg::OnSize(UINT nType, int cx, int cy)
{
	CDialogEx::OnSize(nType, cx, cy);

	//printf("onSize: %d, %d\n", cx, cy);

	if (m_Statusbar.GetSafeHwnd())
	{
		RepositionBars(AFX_IDW_CONTROLBAR_FIRST, AFX_IDW_CONTROLBAR_LAST, 0); 
	}
}

void CJotuPixDlg::OnAppAbout()
{
	CAboutDlg dlgAbout;
	dlgAbout.DoModal();
}

void CJotuPixDlg::Log(const CString& msg)
{
	int len = m_logEdit.GetWindowTextLength();
	m_logEdit.SetSel(len, len);
	m_logEdit.ReplaceSel(msg + _T("\r\n"));

	// roll to last line
	m_logEdit.LineScroll(m_logEdit.GetLineCount());
}

void CJotuPixDlg::Log(const CString& msg, bool bBreak)
{
	int len = m_logEdit.GetWindowTextLength();
	m_logEdit.SetSel(len, len);

	if (bBreak)
	{
		m_logEdit.ReplaceSel(msg + _T("\r\n"));
	}
	else
	{
		m_logEdit.ReplaceSel(msg);
	}

	// roll to last line
	m_logEdit.LineScroll(m_logEdit.GetLineCount());
}

void CJotuPixDlg::OnBnClickedButtonOpr()
{
	if (m_Serial.isOpen())
	{
		m_Serial.close();
		m_btnConnect.SetWindowText(_T("Open"));
		m_currentOpenedPort = "";
		Log::Printf("Serial port close!\r\n");
	}
	else if (m_listUartPort.GetCount() > 0)
	{
		char portName[256] = { 0 };
		int SelBaudRate;
		int SelParity;
		int SelDataBits;
		int SelStop;

		UpdateData(true);
		CString temp;
		m_listUartPort.GetWindowText(temp);
#ifdef UNICODE
		strcpy_s(portName, 256, CW2A(temp.GetString()));
#else
		strcpy_s(portName, 256, temp.GetBuffer());
#endif	

		m_listUartBaudrate.GetWindowText(temp);
		SelBaudRate = _tstoi(temp);

		// The following configuration remains unchanged by default
		SelParity = serial::parity_none;
		SelDataBits = 8;
		SelStop = 1;

		m_Serial.setPort(portName);
		m_Serial.setBaudrate(SelBaudRate);
		m_Serial.setParity((serial::parity_t)SelParity);
		m_Serial.setStopbits((serial::stopbits_t)SelStop);
		m_Serial.setBytesize((serial::bytesize_t)SelDataBits);
		m_Serial.setTimeout(serial::Timeout::max(), 250, 0, 250, 0);
		m_Serial.open();

		if (m_Serial.isOpen())
		{
			m_btnConnect.SetWindowText(_T("Close"));
			m_currentOpenedPort = portName;
			Log::Printf("Serial port is open!\r\n");

			// Get dev info
			JDeviceManager::Instance().GetDevice()->GetDevInfo(this);
		}
		else
		{
			m_btnConnect.SetWindowText(_T("Open"));
			AfxMessageBox(_T("The serial port is occupied!"));
			m_currentOpenedPort = "";
		}
	}
	else
	{
		AfxMessageBox(_T("No serial port found!"));
		m_currentOpenedPort = "";
	}
}

void CJotuPixDlg::OnBnClickedButtonClearlog()
{
	m_logEdit.SetWindowText(_T(""));
}

void CJotuPixDlg::OnBnClickedButtonSavelogo()
{
	CFileDialog dlg(FALSE, _T("txt"), _T("log.txt"), OFN_OVERWRITEPROMPT, _T("Log file (*.txt)|*.txt||"));
	if (dlg.DoModal() == IDOK)
	{
		CString path = dlg.GetPathName();
		CString content;
		m_logEdit.GetWindowText(content);

		CStdioFile file;
		// is only write ascii
		if (file.Open(path, CFile::modeCreate | CFile::modeReadWrite | CFile::shareDenyNone | CStdioFile::typeUnicode))
		{
			file.WriteString(content);
			file.Close();
		}
	}
}

void CJotuPixDlg::OnDestroy()
{
	CDialogEx::OnDestroy();

	m_Serial.close();

	KillTimer(JOTUPIX_THREAD_TIMER_ID);
}

void CJotuPixDlg::onUartReadEvent(const char* pu8Data, uint32_t u32Len)
{
	CString str = CharArrayToHexString((const char *)pu8Data, u32Len);

	Log(_T("rec: ") + str);

	std::shared_ptr<JotuPix> dev = JDeviceManager::Instance().GetDevice();
	if (dev != nullptr)
	{
		dev->ParseRecvData((const unsigned char*)pu8Data, u32Len);
	}
}

CString CJotuPixDlg::CharArrayToHexString(const char* data, size_t len)
{
	CString result;

	for (size_t i = 0; i < len; ++i)
	{
		CString hexByte;
		hexByte.Format(_T("%02X "), (unsigned char)data[i]);
		result += hexByte;
	}

	// Remove the last space (optional)
	if (!result.IsEmpty())
	{
		result = result.Left(result.GetLength() - 1);
	}

	return result;
}

void CJotuPixDlg::OnTimer(UINT_PTR nIDEvent)
{
	if (nIDEvent == JOTUPIX_THREAD_TIMER_ID)
	{
		m_u32CurrMs += JOTUPIX_THREAD_TIMER_TICK;
		std::shared_ptr<JotuPix> dev = JDeviceManager::Instance().GetDevice();
		if (dev != nullptr)
		{
			dev->Tick(m_u32CurrMs);
		}
	}
	else if (nIDEvent == TIMER_ID_REFRESH_PORTS)
	{
		RefreshSerialPortList();
	}
	else if (nIDEvent == TIMER_ID_UART_READ)
	{
		try
		{
			if (m_Serial.available())
			{
				string data = m_Serial.read(128);
				if (data.length() > 0)
				{
					onUartReadEvent(data.c_str(), data.length());
				}
			}
		}
		catch (const std::exception&)
		{

		}
	}

	CDialogEx::OnTimer(nIDEvent);
}

void CJotuPixDlg::RefreshSerialPortList()
{
	//std::vector<SerialPortInfo> ports = CSerialPortInfo::availablePortInfos();
	vector<serial::PortInfo> ports = serial::list_ports();
	std::vector<CString> newList;

	for (auto& p : ports)
	{
		CString sPort(p.port.c_str());
		newList.push_back(sPort);
	}

	// If the list is the same as the cached list, do not refresh.
	if (newList == m_cachedPorts)
		return;

	// Otherwise, update the UI
	m_listUartPort.ResetContent();
	for (auto& s : newList)
		m_listUartPort.AddString(s);

	m_cachedPorts = newList;

	// If the current selection is not in the new list, deselect.
	if (!newList.empty())
	{
		m_listUartPort.SetCurSel(0);
	}

	if (m_Serial.isOpen())
	{
		bool stillExists = false;
		for (auto& s : m_cachedPorts)
		{
			if (s == m_currentOpenedPort)
			{
				stillExists = true;
				break;
			}
		}

		if (!stillExists)
		{
			//m_SerialPort.close();
			m_btnConnect.SetWindowText(_T("Open"));

			Log::Printf("Serial port close!\r\n");
		}
	}
}

void CJotuPixDlg::OnTcnSelchangeTabPro(NMHDR* pNMHDR, LRESULT* pResult)
{
	int nSel = m_tabCtrl.GetCurSel();

	// Hide the previous page
	switch (m_currentTab)
	{
	case 0: m_pageBase.ShowWindow(SW_HIDE); break;
	case 1: m_pageAni.ShowWindow(SW_HIDE); break;
	case 2: m_pageText.ShowWindow(SW_HIDE); break;
	}

	// Display the currently selected page
	switch (nSel)
	{
	case 0: m_pageBase.ShowWindow(SW_SHOW); break;
	case 1: m_pageAni.ShowWindow(SW_SHOW); break;
	case 2: m_pageText.ShowWindow(SW_SHOW); break;
	}

	m_currentTab = nSel;

	*pResult = 0;
}

void CJotuPixDlg::OnStatusBarUrlClicked()
{
	ShellExecute(nullptr, _T("open"), _T("https://www.baidu.com"), nullptr, nullptr, SW_SHOWNORMAL);
}


void CJotuPixDlg::onEvent(JInfo devInfo)
{
	JDeviceManager::Instance().SetDeviceInfoCache(devInfo);
}