#include "pch.h"
#include "afxdialogex.h"
#include "CPageBaseDlg.h"
#include "Log.h"
#include "jotupix.h"
#include "JotuPixDlg.h"
#include "JDeviceManager.h"

#define INPUT_TIMER_ID		1

BEGIN_MESSAGE_MAP(CPageBaseDlg, CDialogEx)

	ON_BN_CLICKED(IDC_BUTTON_SWITCH, &CPageBaseDlg::OnBnClickedButtonSwitch)
	ON_EN_CHANGE(IDC_EDIT_BN, &CPageBaseDlg::OnEnChangeEditBn)
	ON_CBN_SELCHANGE(IDC_COMBO_FLIP, &CPageBaseDlg::OnCbnSelchangeComboFlip)
	ON_WM_TIMER()
	ON_BN_CLICKED(IDC_BUTTON_RESET, &CPageBaseDlg::OnBnClickedButtonReset)
END_MESSAGE_MAP()

IMPLEMENT_DYNAMIC(CPageBaseDlg, CDialogEx)

CPageBaseDlg::CPageBaseDlg(CWnd* pParent /*=nullptr*/)
	: CTabPageBaseDlg(IDD_PAGE_BASE, pParent)
	, m_bStatus(false)
{

}

CPageBaseDlg::~CPageBaseDlg()
{

}

BOOL CPageBaseDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	m_bStatus = TRUE;
	m_btnSwitch.SetWindowText(_T("Close"));

	// Flip Options
	CString FlipArray[] = { _T("No flip"),  _T("XY flip"), _T("X flip"), _T("Y flip")};
	for (int i = 0; i < sizeof(FlipArray) / sizeof(CString); i++)
	{
		m_comboFlip.InsertString(i, FlipArray[i]);
	}

	m_comboFlip.SetCurSel(0);

	return TRUE;
}

void CPageBaseDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_BUTTON_SWITCH, m_btnSwitch); // Binding Controls
	DDX_Control(pDX, IDC_COMBO_FLIP, m_comboFlip);
	DDX_Control(pDX, IDC_EDIT_BN, m_editBn);
}


void CPageBaseDlg::OnBnClickedButtonSwitch()
{
	std::shared_ptr<JotuPix> device = JDeviceManager::Instance().GetDevice();
	if (device == nullptr)
	{
		AfxMessageBox(_T("Device not created"));
		return;
	}

	if (m_bStatus)
	{
		m_bStatus = FALSE;
		m_btnSwitch.SetWindowText(_T("Open"));

		device->SendSwitchStatus(0);
	}
	else
	{
		m_bStatus = TRUE;
		m_btnSwitch.SetWindowText(_T("Close"));

		device->SendSwitchStatus(1);
	}
}

#define MAX_SPEED_VAL		255

void CPageBaseDlg::OnEnChangeEditBn()
{
	CString str;
	m_editBn.GetWindowText(str);

	if (str.IsEmpty())
		return;

	// Check if it is a number
	for (int i = 0; i < str.GetLength(); i++)
	{
		if (!_istdigit(str[i])) {
			MessageBeep(0);
			str.Delete(i);
			m_editBn.SetWindowText(str);
			m_editBn.SetSel(-1, -1); // Place the cursor at the end
			return;
		}
	}

	// Convert to integer and limit the range
	int val = _ttoi(str);
	if (val > MAX_SPEED_VAL) {
		val = MAX_SPEED_VAL;
		str.Format(_T("%d"), val);
		m_editBn.SetWindowText(str);
		m_editBn.SetSel(-1, -1);
	}

	KillTimer(INPUT_TIMER_ID);
	SetTimer(INPUT_TIMER_ID, 1000, NULL); // Triggered 1 second after user stops typing
}

void CPageBaseDlg::OnTimer(UINT_PTR nIDEvent)
{
	if (nIDEvent == INPUT_TIMER_ID)
	{
		KillTimer(INPUT_TIMER_ID);
		CString str;
		m_editBn.GetWindowText(str);

		std::shared_ptr<JotuPix> device = JDeviceManager::Instance().GetDevice();
		if (device == nullptr)
		{
			AfxMessageBox(_T("Device not created"));
			return;
		}
		
		int bn = _ttoi(str);
		device->SendBrightness(bn);
	}

	CDialogEx::OnTimer(nIDEvent);
}

void CPageBaseDlg::OnCbnSelchangeComboFlip()
{
	int nIndex = m_comboFlip.GetCurSel();

	std::shared_ptr<JotuPix> device = JDeviceManager::Instance().GetDevice();
	if (device == nullptr)
	{
		AfxMessageBox(_T("Device not created"));
		return;
	}

	device->SendScreenFlip(nIndex);
}


void CPageBaseDlg::OnBnClickedButtonReset()
{
	std::shared_ptr<JotuPix> device = JDeviceManager::Instance().GetDevice();
	if (device == nullptr)
	{
		AfxMessageBox(_T("Device not created"));
		return;
	}

	device->SendReset();
}
