#pragma once

#include <stdint.h>
#include <vector>

/**
 * @brief Base class for all content types used in program data.
 *
 * This abstract class defines the interface and common properties for various
 * content types (text, animation, border, etc.). Each derived class must
 * implement its own data packaging method according to the communication
 * protocol requirements.
 */
class JContentBase
{
public:
	virtual ~JContentBase() {}

	/**
	 * @brief Pack content data according to the protocol format.
	 *
	 * Each derived class must implement this function to generate the
	 * protocol-compliant byte sequence that represents its content.
	 *
	 * @return std::vector<uint8_t> A byte buffer containing the packaged content.
	 */
	virtual std::vector<uint8_t> Get() = 0;

	/**
	 * @brief Content type enumeration.
	 *
	 * Indicates the specific category of graphical content being transferred.
	 * Each type corresponds to different encoding or rendering rules.
	 */
	enum class ContentType
	{
		TEXT = 1,
		GRAFFITI,
		ANIMATION,
		BORDER,
		FULLCOLOR,
		DIYCOLOR,
		GIF = 0x0c,
	};

	/**
	 * @brief Blending mode used when compositing content.
	 *
	 * MIX   - Content blends with existing pixels.
	 * COVER - Content overwrites existing pixels.
	 */
	enum class BlendType
	{
		MIX,
		COVER,
	};

	ContentType GetContentType()
	{
		return m_contentType;
	}

protected:
	ContentType m_contentType;
};