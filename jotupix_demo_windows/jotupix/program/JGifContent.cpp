#include "JGifContent.h"
#include "JByteWriter.h"

std::vector<uint8_t> JGifContent::Get()
{
	JByteWriter content;

	content.put_u8((uint8_t)m_contentType);
	content.put_repeat(0, 7);
	content.put_u8((uint8_t)m_blendType);
	content.put_u8(0);
	content.put_u16(m_showX);
	content.put_u16(m_showY);
	content.put_u16(m_showWidth);
	content.put_u16(m_showHeight);

	content.put_u32(m_gifData.size());
	content.put_bytes(m_gifData);

	content.insert_u32(0, content.size() + 4);

	return content.buffer;
}