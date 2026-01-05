#pragma once

#include "JContentBase.h"
#include "JTextFont.h"

class JTextDiyColorContent : public JContentBase
{
public:
	JTextDiyColorContent() {
		m_contentType = ContentType::DIYCOLOR;
	}

	// Multicolor text color examples
	static const uint16_t MulticolorData[];
	static const uint32_t MulticolorDataSize;

	uint16_t m_moveSpace;
	uint16_t m_showX;
	uint16_t m_showY;
	uint16_t m_showWidth;
	uint16_t m_showHeight;
	uint8_t m_showMode;
	uint8_t m_showSpeed;
	uint8_t m_stayTime;
	uint16_t m_textNum;
	uint32_t m_textAllWide;

	std::vector<JTextFont> m_textData;

	std::vector<uint8_t> JContentBase::Get() override;
};

