#pragma once
#include "JContentBase.h"
#include "JTextFont.h"
#include <stdint.h>

class JTextFullColorContent : public JContentBase
{
public:
	JTextFullColorContent() {
		m_contentType = ContentType::FULLCOLOR;
	}

	enum class TextColorType
	{
		HorScroll = 1,
		Static,
		VertScroll,
		VertRelativeScroll,
		Jump,
		HorCover,
		HorDiagonalScroll,
		Rotation,
	};

	enum class TextColorDir
	{
		Left = 0,
		Right,
		Up,
		Down,
		Center,
		Side,
	};

	// Full Color Example Colors
	static const uint16_t TextFullColorRainbow[];
	static const uint32_t TextFullColorRainbowSize;

	static const uint16_t TextFullColorThree[];
	static const uint32_t TextFullColorThreeSize;

	uint16_t m_showX;
	uint16_t m_showY;
	uint16_t m_showWidth;
	uint16_t m_showHeight;
	TextColorType m_textColorType;
	uint8_t m_textColorSpeed;
	TextColorDir m_textColorDir;

	std::vector<uint16_t> m_textFullColor;

	std::vector<uint8_t> JContentBase::Get() override;
};

