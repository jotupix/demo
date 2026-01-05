#include "JTextContent.h"
#include "JByteWriter.h"

std::vector<uint8_t> JTextContent::Get()
{
	JByteWriter content;

	content.put_u8((uint8_t)m_contentType);
	content.put_repeat(0, 5);
	content.put_u16(m_bgColor);
	content.put_u8((uint8_t)m_blendType);
	content.put_u16(m_showX);
	content.put_u16(m_showY);
	content.put_u16(m_showWidth);
	content.put_u16(m_showHeight);
	content.put_u8(m_showMode);
	content.put_u8(m_showSpeed);
	content.put_u8(m_stayTime);
	content.put_u16(m_moveSpace);
	content.put_u16(m_textNum);
	content.put_u32(m_textAllWide);

	for (auto& font : m_textData)
	{
		content.put_u8(font.m_textWidth);
		content.put_u8((uint8_t)font.m_textType);
		content.put_bytes(font.m_showData);
	}

	content.insert_u32(0, content.size() + 4);

	return content.buffer;
}
