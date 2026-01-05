/**
 * @file    jotupix_define.h
 * @brief   Public data structures, function definitions, etc.
 * @note
 *
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */

#ifndef __JOTUPIX_DEFINE_H__
#define __JOTUPIX_DEFINE_H__

#ifdef __cplusplus
extern "C" {
#endif

#include "jotupix_typedef.h"

#define ON      1
#define OFF     0

#define SUCCESS   0
#define FAIL     -1

typedef struct _jotupix_data_s
{
    uint32 m_u32Pos;    // current read position
    uint32 m_u32Size;  // data size
    const uint8 *m_pu8Data;  // data pointer
}jotupix_data_s;

/**
 * @brief Data reading interface, application layer implementation
 *
 * @param[in] psData data object
 * @param[in,out] pu8Buff read data buffer
 * @param[in] u32Len  read length
 *
 * @return read data length
 */
typedef int (*jotupix_read_f)(jotupix_data_s *psData, uint8 *pu8Buff, uint32 u32Len);

/**
 * @brief Set the read position, application layer implementation
 *
 * @param[in] psData data object
 * @param[in] u32Pos  position
 *
 * @return seek postion
 */
typedef int (*jotupix_seek_f)(jotupix_data_s *psData, uint32 u32Pos);

/**
 * @brief The actual data sending interface is registered by the upper layer
 *
 * @param[in] pu8Data data buffer
 * @param[in] u32Len  data length
 *
 * @return 0 on success, other on failure.
 */
typedef int (*jotupix_send_f)(const uint8* pu8Data, uint32 u32Len, void *pvUserData);

/**
 * @brief Data parsing completion callback
 *
 * @param[in] pu8Data data buffer
 * @param[in] u32Len  data length
 *
 * @return
 */
typedef void (*jotupix_parse_complete_f)(const uint8 *pu8Data, uint32 u32Len, void *pvUserData);

typedef struct _jotupix_stream_s
{
    jotupix_data_s m_sData;
    jotupix_read_f m_pfnRead;
    jotupix_seek_f m_pfnSeek;
}jotupix_stream_s;

#ifdef __cplusplus
}
#endif

#endif // __JOTUPIX_DEFINE_H__