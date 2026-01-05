#include "pch.h"
#include "afxdialogex.h"
#include "CPageTextDlg.h"
#include <vector>
#include <algorithm>
#include <cstdint>
#include <iterator>

#include "resource.h"

//#include "DeviceInfo.h"
#include "Log.h"
#include "JByteWriter.h"
#include "JTextContent.h"
#include "JTextDiyColorContent.h"
#include "JProgramContent.h"
#include "JColor.h"
#include "JTextFullColorContent.h"
#include "JDeviceManager.h"

#define MAX_TEXT_NUM		150

IMPLEMENT_DYNAMIC(CPageTextDlg, CDialogEx)

CPageTextDlg::CPageTextDlg(CWnd* pParent /*=nullptr*/)
	: CTabPageBaseDlg(IDD_PAGE_TEXT, pParent)
{

}

CPageTextDlg::~CPageTextDlg()
{
}

void CPageTextDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDIT_TEXT, m_editText);
	DDX_Control(pDX, IDC_COMBO_TEXT_COLOR, m_comboTextColor);
	DDX_Control(pDX, IDC_COMBO_TEXT_MODE, m_comboTextMode);
	DDX_Control(pDX, IDC_EDIT_TEXT_SPEED, m_editTextSpeed);
}

BOOL CPageTextDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	// Text color
	CString TextColorArray[] = { _T("Multicolor"),  _T("Red Color"), _T("Yellow Color"), _T("Colorful") };
	for (int i = 0; i < sizeof(TextColorArray) / sizeof(CString); i++)
	{
		m_comboTextColor.InsertString(i, TextColorArray[i]);
	}
	m_comboTextColor.SetCurSel(0);

	// Text Mode
	CString TextModeArray[] = { _T("Static"),  _T("Left shift"), _T("Right shift"), _T("Up") };
	for (int i = 0; i < sizeof(TextModeArray) / sizeof(CString); i++)
	{
		m_comboTextMode.InsertString(i, TextModeArray[i]);
	}
	m_comboTextMode.SetCurSel(1);

	m_editTextSpeed.SetWindowText(_T("245"));

	m_editText.SetLimitText(MAX_TEXT_NUM);

	return TRUE;
}

BEGIN_MESSAGE_MAP(CPageTextDlg, CDialogEx)
	ON_BN_CLICKED(IDC_BUTTON_TEXT_SEND, &CPageTextDlg::OnBnClickedButtonTextSend)
	ON_EN_CHANGE(IDC_EDIT_TEXT_SPEED, &CPageTextDlg::OnEnChangeEditTextSpeed)
END_MESSAGE_MAP()

#define FONT_SIZE		24

static int iGetFontBytes(WCHAR font, uint8_t* pu8Buffer, uint16_t u16BufferLen)
{
	const uint8_t ni_data[] = { 0x00,0x00,0x00,0x1c,0x00,0xf8,0x1d,0xfb,0xf8,0x1d,0xfb,0xee,0x5d,0x99,0x0e,0x7d,0x9f,0xfe,0x7d,0x9f,0xfe,0x7d,0x9f,0xfc,0x1d,0xf9,0x00,0x1d,0xfb,0xc0,0x1d,0xf9,0xec,0x1c,0x00,0xfc,0x03,0x80,0xfc,0x03,0x8f,0xf8,0x7f,0xff,0xe0,0x7f,0xff,0x00,0x7f,0xff,0xfc,0x13,0x9f,0xfe,0x33,0x9f,0xfe,0x3b,0x80,0x0e,0x13,0x80,0xfe,0x13,0x80,0xfc,0x00,0x00,0x40,0x00,0x00,0x00, };

	memcpy(pu8Buffer, ni_data, sizeof(ni_data));

	return sizeof(ni_data);
}

void CPageTextDlg::PrintFontData(const uint8_t* pu8FontData, uint32_t u32RowNum, uint32_t u32ColumnNum)
{
	uint32_t u32FontRowByteSize = ((u32RowNum + 7) / 8);

	for (int i = 0; i < u32FontRowByteSize; i++) {
		for (int j = 0; j < 8; j++) {
			for (int k = 0; k < u32ColumnNum; k++) {
				if (i * 8 + j < u32RowNum) {
					unsigned char flag = pu8FontData[k * u32FontRowByteSize + i] & (0x80 >> j);
					printf("%s", flag ? "●" : "○");
				}
			}

			printf("\n");
		}
	}
	printf("\n");
}

bool CPageTextDlg::IsEmptyColumn(const std::vector<uint8_t>& fontBits, int col, int bytesPerCol)
{
	const uint8_t* p = &fontBits[col * bytesPerCol];

	for (int i = 0; i < bytesPerCol; ++i)
	{
		if (p[i] != 0)
			return false;
	}

	return true;
}

std::vector<uint8_t> CPageTextDlg::TrimEmptyColumns(const std::vector<uint8_t>& fontBits, int width, int height, int& newWidth, int spacing)
{
	int bytesPerCol = (height + 7) / 8;
	int totalBytes = width * bytesPerCol;

	if ((int)fontBits.size() < totalBytes)
	{
		newWidth = 0;
		return {};
	}

	int left = 0, right = width - 1;

	// 1️⃣ Find the first non-empty column from the left
	for (; left < width; ++left)
	{
		if (!IsEmptyColumn(fontBits, left, bytesPerCol))
			break;
	}

	// 2️⃣ Find the last non-empty column from the right
	for (; right >= 0; --right)
	{
		if (!IsEmptyColumn(fontBits, right, bytesPerCol))
			break;
	}

	// 3️⃣ If all columns are empty, return half width (minimum 1 column)
	if (left > right)
	{
		newWidth = width / 2 + spacing;
		std::vector<uint8_t> result(newWidth * bytesPerCol, 0);
		return result;
	}

	// 4️⃣ Effective width
	int effectiveWidth = right - left + 1;

	// 5️⃣ Add spacing columns
	newWidth = effectiveWidth + spacing;

	// 6️⃣ Copy valid columns + spacing columns
	std::vector<uint8_t> trimmed(newWidth * bytesPerCol, 0);

	for (int x = 0; x < effectiveWidth; ++x)
	{
		for (int yb = 0; yb < bytesPerCol; ++yb)
		{
			trimmed[x * bytesPerCol + yb] =
				fontBits[(left + x) * bytesPerCol + yb];
		}
	}

	// The last 'spacing' columns remain zero (empty)

	return trimmed;
}

void CPageTextDlg::OnBnClickedButtonTextSend()
{
	std::vector<JTextFont> textData;
	JProgramContent programContents;
	JInfo devInfo = JDeviceManager::Instance().GetDeviceInfoCache();

	uint32_t textNum = 0;
	uint32_t textAllWidth = 0;

	textNum = m_editText.GetWindowTextLength();
	if (textNum <= 0)
	{
		MessageBox(L"The input cannot be empty!", L"Warn", MB_OK | MB_ICONWARNING);
		return;
	}

	if (devInfo.m_u16DevHeight == 0 || devInfo.m_u16DevWidth == 0)
	{
		AfxMessageBox(_T("Device information error!"));
		return;
	}

	int fontSize = devInfo.m_u16DevHeight;
	if (fontSize == 0)
	{
		fontSize = 16;
	}

	uint8_t* pu8FontBitBuffer = nullptr;

	WCHAR* fontChar = (WCHAR*)malloc(textNum * 2 + 2);
	if (fontChar == nullptr)
	{
		return;
	}
	m_editText.GetWindowText(fontChar, textNum + 1);

	int fontNeedByteNum = ((fontSize + 7) / 8) * fontSize;

	pu8FontBitBuffer = (uint8_t*)malloc(fontNeedByteNum);
	if (pu8FontBitBuffer == nullptr)
	{
		free(fontChar);
		return;
	}

	// set fontlib info
	m_bitFont.SetFont(nullptr, fontSize, CBitFont::Style::BOLD, 1);
	m_bitFont.SetSize(fontSize, fontSize);
	m_bitFont.SetOffset(-1, 0);

	//int colorSel = m_comboTextColor.GetCurSel();
	CString colorSelStr;
	m_comboTextColor.GetWindowText(colorSelStr);
	int multiColorIndex = 0;
	
	// Generate text display content
	for (int i = 0; i < textNum; i++)
	{
		m_bitFont.PaintChar(fontChar[i]);

		memset(pu8FontBitBuffer, 0, fontNeedByteNum);;

		int nRet = m_bitFont.GetBits(pu8FontBitBuffer, fontNeedByteNum, CBitFont::Scan::VERT, true);
		if (nRet < 0)
		{
			AfxMessageBox(_T("Sent text fail, Get font data fail!"));
			free(fontChar);
			free(pu8FontBitBuffer);
		}

		int trimWidth = 0;
		JByteWriter buffer;
		buffer.put_bytes(pu8FontBitBuffer, nRet);
		std::vector<uint8_t> trimData = TrimEmptyColumns(buffer.buffer, fontSize, fontSize, trimWidth);

		//PrintFontData(trimData.data(), fontSize, trimWidth);

		textAllWidth += trimWidth;

		// If the text is in color, such as an emoji, then the display data needs to be RGB444.
		JTextFont font;
		font.m_textType = JTextFont::TextType::MONOCHROME;
		font.m_textWidth = trimWidth;
		font.m_showData = trimData;

		// { _T("Multicolor"),  _T("Red Color"), _T("Yellow Color"), _T("Colorful") }
		if (colorSelStr == "Multicolor")
		{
			font.m_textColor = JTextDiyColorContent::MulticolorData[multiColorIndex++];
			if (multiColorIndex >= JTextDiyColorContent::MulticolorDataSize)
			{
				multiColorIndex = 0;
			}
		}
		else if (colorSelStr == "Red Color")
		{
			font.m_textColor = (uint16_t)JColor::Red;
		}
		else if (colorSelStr == "Yellow Color")
		{
			font.m_textColor = (uint16_t)JColor::Yellow;
		}
		else // If fullcolor is selected, the text will be colored red by default.
		{
			font.m_textColor = (uint16_t)JColor::Yellow;
		}

		textData.push_back(font);
	}

	uint8_t showMode = m_comboTextMode.GetCurSel() + 1; 

	// show-speed
	CString speedStr;
	m_editTextSpeed.GetWindowText(speedStr);
	uint8_t showSpeed = _ttoi(speedStr);
	uint8_t stayTime = 1;
	uint16_t showX = 0;
	uint16_t showY = 0;
	uint16_t showWidth = devInfo.m_u16DevWidth;
	uint16_t showHeight = devInfo.m_u16DevHeight;
	uint16_t moveSpace = devInfo.m_u16DevWidth;  // The default movement interval is the width of the display screen.

	if (colorSelStr != "Colorful")
	{
		// Create text color content
		std::shared_ptr<JTextDiyColorContent> diyColorContent = std::make_shared<JTextDiyColorContent>();

		// The parameters of diycolor must be consistent with the textContent parameter.
		diyColorContent->m_moveSpace = moveSpace;
		diyColorContent->m_showX = showX;
		diyColorContent->m_showY = showY;
		diyColorContent->m_showWidth = showWidth;
		diyColorContent->m_showHeight = showHeight;
		diyColorContent->m_showMode = showMode;
		diyColorContent->m_showSpeed = showSpeed;
		diyColorContent->m_stayTime = stayTime;
		diyColorContent->m_textNum = textNum;
		diyColorContent->m_textAllWide = textAllWidth;
		diyColorContent->m_textData = textData;

		programContents.Add(diyColorContent);
	}
	else
	{
		// Create text fullcolor content
		std::shared_ptr<JTextFullColorContent> fullColorContent = std::make_shared<JTextFullColorContent>();

		fullColorContent->m_showX = 0;
		fullColorContent->m_showY = 0;
		fullColorContent->m_showWidth = devInfo.m_u16DevWidth;
		fullColorContent->m_showHeight = devInfo.m_u16DevHeight;
		fullColorContent->m_textColorType = JTextFullColorContent::TextColorType::HorScroll;
		fullColorContent->m_textColorSpeed = 200; // This speed is independent of text display speed.
		fullColorContent->m_textColorDir = JTextFullColorContent::TextColorDir::Right;
	
		for (int i = 0; i < JTextFullColorContent::TextFullColorRainbowSize; i++)
		{
			fullColorContent->m_textFullColor.push_back(JTextFullColorContent::TextFullColorRainbow[i]);
		}

		programContents.Add(fullColorContent);
	}

	std::shared_ptr<JTextContent> textContent = std::make_shared<JTextContent>();

	//textContent->m_bgColor = 0x0001; // Background color, Low brightness blue! Customize according to your needs.
	textContent->m_bgColor = 0x0000;
	textContent->m_blendType = JContentBase::BlendType::COVER;
	textContent->m_showX = showX;
	textContent->m_showY = showY;
	textContent->m_showWidth = showWidth;
	textContent->m_showHeight = showHeight;
	textContent->m_showMode = showMode;
	textContent->m_showSpeed = showSpeed;
	textContent->m_stayTime = stayTime;
	textContent->m_moveSpace = moveSpace;
	textContent->m_textNum = textNum;
	textContent->m_textAllWide = textAllWidth;
	textContent->m_textData = textData;

	programContents.Add(textContent);

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
	sProInfo.m_eCompress = JotuPix::CompressFlag::COMPRESS_FLAG_DO;

	std::shared_ptr<JProgramGroupNor> group = std::make_shared<JProgramGroupNor>();

	group->m_ePlayType = JProgramGroupNor::PlayType::PlAY_TYPE_CNT;
	group->m_u32PlayParam = 1; // Play once

	sProInfo.m_proGroupParam = group;

	SendProgram(this, &sProInfo, programData.data(), programSize);

	free(fontChar);
	free(pu8FontBitBuffer);

	return;
}

#define MAX_SPEED_VAL		255

void CPageTextDlg::OnEnChangeEditTextSpeed()
{
	CString str;
	m_editTextSpeed.GetWindowText(str);

	if (str.IsEmpty())
		return;

	// Check if it is a number
	for (int i = 0; i < str.GetLength(); i++)
	{
		if (!_istdigit(str[i])) {
			MessageBeep(0);
			str.Delete(i);
			m_editTextSpeed.SetWindowText(str);
			m_editTextSpeed.SetSel(-1, -1); // Place the cursor at the end
			return;
		}
	}

	// Convert to integer and limit the range
	int val = _ttoi(str);
	if (val > MAX_SPEED_VAL) {
		val = MAX_SPEED_VAL;
		str.Format(_T("%d"), val);
		m_editTextSpeed.SetWindowText(str);
		m_editTextSpeed.SetSel(-1, -1);
	}
}
