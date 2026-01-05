#include "pch.h"
#include "afxdialogex.h"
#include "CSendProgressDlg.h"
#include "WmMsg.h"


IMPLEMENT_DYNAMIC(CSendProgressDlg, CDialogEx)

CSendProgressDlg::CSendProgressDlg(CWnd* pParent /*=nullptr*/)
	: CTabPageBaseDlg(IDD_SEND_PROGRESS, pParent)
	, m_cancelCallback(nullptr)
{

}

CSendProgressDlg::~CSendProgressDlg()
{
}

void CSendProgressDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_PROGRESS1, m_progress);
}


BEGIN_MESSAGE_MAP(CSendProgressDlg, CDialogEx)
	ON_BN_CLICKED(IDCANCEL, &CSendProgressDlg::OnBnClickedCancel)
END_MESSAGE_MAP()


BOOL CSendProgressDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();
	m_progress.SetRange(0, 100);
	m_progress.SetPos(0);

	//CWnd* pParent = GetParent();
	CWnd* pParent = AfxGetMainWnd(); 

	if (pParent)
	{
		CRect rcParent, rcDlg;
		pParent->GetWindowRect(&rcParent);
		GetWindowRect(&rcDlg);

		int dlgWidth = rcDlg.Width();
		int dlgHeight = rcDlg.Height();

		int x = rcParent.left + (rcParent.Width() - dlgWidth) / 2;
		int y = rcParent.top + (rcParent.Height() - dlgHeight) / 2;

		MoveWindow(x, y, dlgWidth, dlgHeight);
	}

	return TRUE;
}

void CSendProgressDlg::UpdateProgress(int percent)
{
	if (percent < 0) percent = 0;
	if (percent > 100) percent = 100;

	m_progress.SetPos(percent);
}

void CSendProgressDlg::OnBnClickedCancel()
{
	//CDialogEx::OnCancel();

	if (m_cancelCallback != nullptr)
	{
		m_cancelCallback->onCancelCallbck();
	}

	//DestroyWindow();
}
