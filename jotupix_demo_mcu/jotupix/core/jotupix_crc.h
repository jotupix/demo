/**
 * @file    jotupix_crc.h
 * @brief   CRC soft implementation
 * @note
 *
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */
#ifndef __JOTUPIX_CRC_H__
#define __JOTUPIX_CRC_H__

#ifdef __cplusplus
    extern "C" {
#endif
    
#include "jotupix_typedef.h"

/**
 * @brief Resets the CRC calculation state.
 *
 * Initializes or clears the internal CRC accumulator
 * so that a new CRC calculation can begin.
 */
void jotupix_crc_reset(void);

/**
 * @brief Updates the CRC with a new block of data.
 *
 * Processes the given data buffer and updates the current CRC value.
 *
 * @param pu8Data  Pointer to the input data buffer.
 * @param u32Len   Length of the input data in bytes.
 *
 * @return The updated CRC value after processing the given data.
 */
uint32 jotupix_crc_calculate(const uint8 *pu8Data, uint32 u32Len);

/**
 * @brief Retrieves the current CRC value.
 *
 * Returns the accumulated CRC value after one or more 
 * calls to `jotupix_crc_calculate()`.
 *
 * @return The current CRC value.
 */
uint32 jotupix_crc_get(void);


#ifdef __cplusplus
}
#endif

#endif // __JOTUPIX_CRC_H__

