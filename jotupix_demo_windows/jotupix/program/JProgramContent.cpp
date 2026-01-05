#include "JProgramContent.h"
#include "JTextContent.h"
#include "JByteWriter.h"
#include "JTextDiyColorContent.h"
#include "JTextFullColorContent.h"
#include "JGifContent.h"

void JProgramContent::Add(std::shared_ptr<JContentBase> content)
{
	m_contentData.push_back(content);
}

void JProgramContent::Clear()
{
	m_contentData.clear();
}

std::vector<uint8_t> JProgramContent::Get()
{
	JByteWriter proContent;

	proContent.put_repeat(0, 8); // reserved
	proContent.put_u8(m_contentData.size());
	proContent.put_u8(0); // reserved
	
	for (const auto& content : m_contentData)
	{
		proContent.put_bytes(content->Get());
	}

	return proContent.buffer;
}
