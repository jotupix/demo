#pragma once
#include <vector>
#include <memory>
#include "JContentBase.h"

/**
 * @brief Container for multiple content objects.
 *
 * This class manages a collection of JContentBase objects. It provides
 * methods to add content, clear all content, and serialize the entire
 * content list into a byte vector.
 */
class JProgramContent
{
public:
	/**
	 * @brief Add a content object to the container.
	 *
	 * @param content Shared pointer to a JContentBase object.
	 */
	void Add(std::shared_ptr<JContentBase> content);

	/**
	 * @brief Remove all content from the container.
	 */
	void Clear();

	/**
	 * @brief Pack content data according to the protocol format.
	 *
	 * generate the protocol-compliant byte sequence that represents its content.
	 *
	 * @return std::vector<uint8_t> A byte buffer containing the packaged content.
	 */
	std::vector<uint8_t> Get();

private:
	std::vector<std::shared_ptr<JContentBase>> m_contentData;
};

