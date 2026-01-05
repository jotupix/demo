#pragma once

class CBitFont
{
public:
	CBitFont(void);
	~CBitFont(void);

	enum class Style {
		THIN = 0,
		BOLD,
		ITALIC,
	};

	enum class Scan
	{
		HORZ,  // Horizontal
		VERT, // Vertical
	};

	/*
		设置字体样式和大小
		name[in]: 系统字库名称
		size[in]: 字体大小
		style[in]: 字体粗细体，取值：正常、粗体、斜体
		bpp[in]: 灰度等级，用来决定绘制质量，比如是否开启抗锯齿
			如果 bpp == 1（比如 LED 单色点阵屏），就：

			使用 NONANTIALIASED_QUALITY：不做抗锯齿，避免字体出现灰色边缘，像素保持清晰二值效果。

			否则 (bpp > 1)，就：

			使用 ANTIALIASED_QUALITY：让字体平滑柔和。
	*/
	void SetFont(WCHAR *name, INT size, Style style, INT bpp);

	/*
		设置字体绘制的时候的偏移量
	*/
	void SetOffset(INT dx, INT dy);

	/*
		设置字体绘制的时候的宽高
	*/
	void SetSize(INT width, INT height);

	/*
		绘制当前一个字体
	*/
	void PaintChar(WCHAR ch);

	/*
		获取当前字体的显示数据
		pBits[in]: 字体数据缓冲区
		size[in]: 字体数据缓冲区大小
		scan[in]: 扫描方式，横向、竖向
		msb[in]: 数据高位在前，还是低位在前，true表示高位在前

		返回：数据长度，-1表示失败
	*/
	INT  GetBits(BYTE* pFontBuffer, INT fontBufferSize, Scan scan, BOOL msb);

	/*
		获取绘图句柄，上层可以直接用来绘制文字显示内容到VIEW上，用来做预览
	*/
	HDC  GetDC();

	/*
		获取指定坐标的文字显示数据，一般用来标识这个像素点是否有效
	*/
	BYTE GetPixel(int x, int y);

private:
	void CreateBitmap();
	INT  GetBitsHorz(BYTE* pBits, INT size, BOOL msb);
	INT  GetBitsVert(BYTE* pBits, INT size, BOOL msb);
	void GrayScale(void);

public:
	HDC m_hdc;
	HFONT m_hFont;
	HBITMAP m_hBitmap;
	DWORD* m_pPixels;
	INT m_nBytesPerLine;
	INT m_nOffsetX;
	INT m_nOffsetY;
	INT m_nWidth;
	INT m_nHeight;
	INT m_nCharWidth;
	INT m_nFontBpp; // Bits Per Pixel
};
