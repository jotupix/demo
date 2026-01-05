#include "pch.h"
#include "afxdialogex.h"
#include "CPageAniDlg.h"
#include "Log.h"
#include "WmMsg.h"
#include "JProgramContent.h"
#include "JGifContent.h"

IMPLEMENT_DYNAMIC(CPageAniDlg, CDialogEx)

CPageAniDlg::CPageAniDlg(CWnd* pParent /*=nullptr*/)
	: CTabPageBaseDlg(IDD_PAGE_ANI, pParent)
{
}

CPageAniDlg::~CPageAniDlg()
{

}

void CPageAniDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_BUTTON_SELECT_GIF, m_btnSelect);
	DDX_Control(pDX, IDC_BUTTON_GIF_SEND, m_btnSend);
}


BEGIN_MESSAGE_MAP(CPageAniDlg, CDialogEx)
	ON_BN_CLICKED(IDC_BUTTON_SELECT_GIF, &CPageAniDlg::OnBnClickedButtonSelectGif)
	ON_BN_CLICKED(IDC_BUTTON_GIF_SEND, &CPageAniDlg::OnBnClickedButtonGifSend)
END_MESSAGE_MAP()


BOOL CPageAniDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	m_gifViewer.SubclassDlgItem(IDC_STATIC_GIF, this);

	return TRUE;
}

void CPageAniDlg::OnBnClickedButtonSelectGif()
{
	CFileDialog dlg(TRUE, _T("gif"), NULL,
		OFN_FILEMUSTEXIST | OFN_PATHMUSTEXIST,
		_T("GIF file (*.gif)|*.gif||"));

	if (dlg.DoModal() == IDOK)
	{
		m_gifPath = dlg.GetPathName();
		if (m_gifViewer.LoadGif(m_gifPath))
		{
			m_gifViewer.Start();

            GIF_INFO info;

            m_gifViewer.GetGifInfo(info);
            Log::Printf("Gif info, width=%d, height=%d, frameCount=%d\r\n", info.width, info.height, info.frameCount);
		}
		else
		{
			AfxMessageBox(_T("Unable to load GIF file"));
		}
	}
}

#if 0
int send_pro(jotupix_program_info_s* psProInfo, jotupix_stream_s* psDataStream, jotupix_send_program_callback_f pfnCallback)
{
    printf("size = %d\n", psDataStream->m_sData.m_u32Size);

    uint8_t buffer[16] = { 0 };

    psDataStream->m_pfnRead(&psDataStream->m_sData, buffer, sizeof(buffer));

    for (int i = 0; i < sizeof(buffer); i++)
    {
        printf("0x%02x,", buffer[i]);
    }

    return 0;
}
#endif

void CPageAniDlg::OnBnClickedButtonGifSend()
{
    if (m_gifPath.IsEmpty())
    {
        AfxMessageBox(_T("Please select the GIF file first!"));
        return;
    }

    // Read GIF files
    CFile file;
    if (!file.Open(m_gifPath, CFile::modeRead | CFile::typeBinary))
    {
        DWORD err = GetLastError();
        AfxMessageBox(_T("Failed to open GIF file!, Error code: %lu"), err);
        return;
    }

    JProgramContent programContents;

    std::shared_ptr<JGifContent> gifContent = std::make_shared<JGifContent>();

    // If the GIF resolution exceeds the screen resolution, it needs to be scaled.
    GIF_INFO info;
    m_gifViewer.GetGifInfo(info);

    // There are limitations to the use of GIFs; they cannot be mixed.
    gifContent->m_blendType = JContentBase::BlendType::COVER;
    gifContent->m_showX = 0;
    gifContent->m_showY = 0;
    gifContent->m_showWidth = info.width;
    gifContent->m_showHeight = info.height;

    gifContent->m_gifData.resize(file.GetLength());

    if (!file.Read(reinterpret_cast<char*>(gifContent->m_gifData.data()), file.GetLength()))
    {
        file.Close();
        AfxMessageBox(_T("Failed to read GIF file!"));
        return;
    }

    file.Close();

    programContents.Add(gifContent);


	std::vector<uint8_t> programData = programContents.Get();
	uint32_t programSize = programData.size();

	//printf("programSize =%d\r\n", programSize);
	//for (int i=0; i < programSize; i++)
	//{
	//	printf("%02x,", m_pu8Buffer[i]);
	//}

	// Initialize stream object


	JotuPix::ProgramInfo sProInfo;

	sProInfo.m_u8ProIndex = 0;
	sProInfo.m_u8ProAllNum = 1;
	sProInfo.m_eCompress = JotuPix::CompressFlag::COMPRESS_FLAG_UNDO;

    std::shared_ptr<JProgramGroupNor> group = std::make_shared<JProgramGroupNor>();

    group->m_ePlayType = JProgramGroupNor::PlayType::PlAY_TYPE_CNT;
    group->m_u32PlayParam = 1; // Play once

    sProInfo.m_proGroupParam = group;

	SendProgram(this, &sProInfo, programData.data(), programSize);
}