#include "JTextFullColorContent.h"
#include "JByteWriter.h"

const uint16_t JTextFullColorContent::TextFullColorRainbow[] =
{
    0x0F00, 0x0F20, 0x0F40, 0x0F60, 0x0F80, 0x0FA0, 0x0FC0, 0x0FF0,
    0x0CF0, 0x0AF0, 0x08F0, 0x06F0, 0x04F0, 0x02F0, 0x00F0, 0x00F2,
    0x00F4, 0x00F6, 0x00F8, 0x00FA, 0x00FC, 0x00FF, 0x00CF, 0x00AF,
    0x008F, 0x006F, 0x004F, 0x002F, 0x000F, 0x020F, 0x040F, 0x060F,
    0x080F, 0x0A0F, 0x0C0F, 0x0F0F, 0x0F0C, 0x0F0A, 0x0F08, 0x0F06, 
	0x0F04, 0x0F02, 0x0F00
};

const uint32_t JTextFullColorContent::TextFullColorRainbowSize = sizeof(TextFullColorRainbow) / sizeof(TextFullColorRainbow[0]);

const uint16_t JTextFullColorContent::TextFullColorThree[]
{
    0x00FF,0x0F0F,0x0FF0,
};

const uint32_t JTextFullColorContent::TextFullColorThreeSize = sizeof(TextFullColorThree) / sizeof(TextFullColorThree[0]);

std::vector<uint8_t> JTextFullColorContent::Get()
{
	JByteWriter content;

	content.put_u8((uint8_t)m_contentType);
	content.put_repeat(0, 7);
	content.put_u16(m_showX);
	content.put_u16(m_showY);
	content.put_u16(m_showWidth);
	content.put_u16(m_showHeight);
	content.put_u8((uint8_t)m_textColorType);
	content.put_u8(m_textColorSpeed);
	content.put_u8((uint8_t)m_textColorDir);
	content.put_u8(0);

	content.put_u16(m_textFullColor.size() * 2);

	for (int i=0; i< m_textFullColor.size(); i++)
	{
		content.put_u16(m_textFullColor[i]);
	}

	content.insert_u32(0, content.size() + 4);

	return content.buffer;
}
