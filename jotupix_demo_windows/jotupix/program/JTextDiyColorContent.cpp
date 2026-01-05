#include "JTextDiyColorContent.h"
#include "JByteWriter.h"
#include "JColor.h"

const uint16_t JTextDiyColorContent::MulticolorData[] =
{
	static_cast<uint16_t>(JColor::Red),
	static_cast<uint16_t>(JColor::Yellow),
	static_cast<uint16_t>(JColor::Green),
	static_cast<uint16_t>(JColor::Cyan),
	static_cast<uint16_t>(JColor::Blue),
	static_cast<uint16_t>(JColor::Purple)
};

const uint32_t JTextDiyColorContent::MulticolorDataSize = sizeof(MulticolorData) / sizeof(MulticolorData[0]);

std::vector<uint8_t> JTextDiyColorContent::Get()
{
	JByteWriter content;

	content.put_u8((uint8_t)m_contentType);
	content.put_repeat(0, 5);
	content.put_u16(m_moveSpace);
	content.put_u16(m_showX);
	content.put_u16(m_showY);
	content.put_u16(m_showWidth);
	content.put_u16(m_showHeight);
	content.put_u8(m_showMode);
	content.put_u8(m_showSpeed);
	content.put_u8(m_stayTime);
	content.put_u8(0);
	content.put_u16(m_textNum);
	content.put_u16(m_textAllWide & 0xFFFFF);

	for (auto& font : m_textData)
	{
		content.put_u8(font.m_textWidth);
	}

	for (auto& font : m_textData)
	{
		content.put_u16(font.m_textColor);
	}

	content.insert_u32(0, content.size() + 4);

	return content.buffer;
}
