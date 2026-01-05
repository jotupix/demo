#pragma once
#include "afxdialogex.h"

#include "CTabPageBaseDlg.h"
#include "jotupix.h"
#include "CSendProgressDlg.h"
#include "CProgramBase.h"
#include <vector>
#include "BitFont.h"

class CPageTextDlg : public CTabPageBaseDlg, public CProgramBase
{
	DECLARE_DYNAMIC(CPageTextDlg)

public:
	CPageTextDlg(CWnd* pParent = nullptr); 
	virtual ~CPageTextDlg();

#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_PAGE_TEXT };
#endif

protected:
	virtual BOOL OnInitDialog();
	virtual void DoDataExchange(CDataExchange* pDX);

	DECLARE_MESSAGE_MAP()
public:
	CEdit m_editText;
	CComboBox m_comboTextColor;
	CComboBox m_comboTextMode;
	CEdit m_editTextSpeed;
	afx_msg void OnBnClickedButtonTextSend();
	afx_msg void OnEnChangeEditTextSpeed();

	/*
	* Display the dot matrix content of the text
	*/
	void PrintFontData(const uint8_t* pu8FontData, uint32_t u32RowNum, uint32_t u32ColumnNum);

private:
	bool IsEmptyColumn(const std::vector<uint8_t>& fontBits, int col, int bytesPerCol);

	/**
	 * @brief Trim empty columns on both sides of the font bitmap
	 *
	 * @param fontBits   Raw font bitmap, 1 bit per pixel, column-major order
	 * @param width      Original width in pixels
	 * @param height     Original height in pixels
	 * @param newWidth   Output trimmed width (after removing empty columns and adding spacing)
	 * @param spacing    Number of empty columns to append after the character (default = 1)
	 *
	 * @return Trimmed font bitmap data
	 */
	std::vector<uint8_t> TrimEmptyColumns(const std::vector<uint8_t>& fontBits, int width, int height, int& newWidth, int spacing = 1);

	CBitFont m_bitFont;
};
