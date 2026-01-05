#pragma once

#include <stdint.h>
#include <vector>

class JTextFont
{
public:
	enum class TextType{
		MONOCHROME, // Monochrome text
		MULTICOLOR, // Colored text, where the text content is represented by the color of each dot.
	};

	TextType m_textType;
	uint8_t m_textWidth;
	uint16_t m_textColor; // This only applies when the text type is monochrome.
	std::vector<uint8_t> m_showData;
};