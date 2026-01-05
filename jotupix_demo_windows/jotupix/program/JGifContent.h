#pragma once
#include <vector>
#include "JContentBase.h"

class JGifContent : public JContentBase
{
public:
	JGifContent() {
		m_contentType = ContentType::GIF;
	}

	BlendType m_blendType;
	uint16_t m_showX;
	uint16_t m_showY;
	uint16_t m_showWidth;
	uint16_t m_showHeight;

	// Data can also be represented by file paths.
	std::vector<uint8_t> m_gifData;

	std::vector<uint8_t> JContentBase::Get() override;
};

