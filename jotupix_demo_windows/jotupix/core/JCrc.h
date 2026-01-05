#pragma once

#include <vector>
#include "JDefine.h"

class JCrc
{
public:
	/**
	 * @brief Reset the CRC calculation state.
	 *
	 * This method reinitializes the internal CRC value to its default
	 * starting value (0xFFFFFFFF). It should be called before starting a
	 * new CRC32 calculation.
	 */
	void Reset();

	/**
	 * @brief Update the CRC32 value using the provided data buffer.
	 *
	 * This function processes a block of bytes and updates the internal CRC
	 * accumulator. It may be called repeatedly to compute the CRC of a
	 * stream of data.
	 *
	 * @param pu8Data Pointer to the input data buffer.
	 * @param u32Len  Number of bytes in the buffer.
	 * @return uint32_t The updated CRC32 value after processing the data.
	 */
	uint32_t Calculate(const uint8_t* pu8Data, uint32_t u32Len);

	/**
	 * @brief Retrieve the current CRC32 value.
	 *
	 * This function returns the CRC value accumulated so far. 
	 *
	 * @return uint32_t The current CRC32 code.
	 */
	uint32_t Get();

private:
	static const uint32_t CRC32TAB[];

	uint32_t m_u32CrcCode = 0xffffffff;
};

