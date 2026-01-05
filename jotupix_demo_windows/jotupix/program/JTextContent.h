#pragma once
#include "JContentBase.h"
#include "JTextFont.h"

class JTextContent : public JContentBase
{
public:
	JTextContent() {
		m_contentType = ContentType::TEXT;
	}

	uint16_t m_bgColor;
	BlendType m_blendType;
	uint16_t m_showX;
	uint16_t m_showY;
	uint16_t m_showWidth;
	uint16_t m_showHeight;
	uint8_t m_showMode;
	uint8_t m_showSpeed;
	uint8_t m_stayTime;
	uint16_t m_moveSpace;
	uint16_t m_textNum;
	uint32_t m_textAllWide;

	std::vector<JTextFont> m_textData;

	std::vector<uint8_t> JContentBase::Get() override;
};

