/**
 * @file    jotupix_lzss.h
 * @brief   Implementation of lzss compression algorithm
 * @note
 *  - This module requires approximately 4.2KB of RAM.
 *  - If it is an embedded system, do not enable compression.
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */
#ifndef __JOTUPIX_LZSS_H__
#define __JOTUPIX_LZSS_H__
 
#ifdef __cplusplus
 extern "C" {
#endif

#include "jotupix_typedef.h"
#include "jotupix_define.h"

#define LZSS_OUT_BUFF_SIZE      1024

/*
    Do not modify the following parameters.
*/

#define LZSS_N           512       
#define LZSS_F           18        
#define LZSS_THRESHOLD   2  
#define LZSS_NIL         LZSS_N

typedef struct _jotupix_lzss_ctx_s
{
    uint8 m_au8WorkBuffer[LZSS_N + LZSS_F - 1];
    uint32 m_u32MatchPos;
    uint32 m_u32MatchLength;
    uint16 m_au16Lson[LZSS_N + 1];
    uint16 m_au16Rson[LZSS_N + 257];
    uint16 m_au16Dad[LZSS_N + 1];

    uint8 m_au8CodeBuffer[17]; // 
    uint8 m_u8Mask;
    uint8 m_u8CodeBufferPtr;
    uint8 m_u8CodeBufferIndex;
    uint32 m_u32R;
    uint32 m_u32S;
    uint32 m_u32Len;

    uint8 m_u8Status;

    jotupix_stream_s *m_psStream;
}jotupix_lzss_ctx_s;

/**
 * @brief Starts the compression process.
 *
 * Initializes and prepares the LZSS compression context for processing.
 *
 * @param psCtx     Pointer to the LZSS compression context.
 * @param psStream  Pointer to the input data stream structure.
 *
 * @return 
 *  0  : Success  
 * <0  : Error occurred
 */
int jotupix_lzss_start(jotupix_lzss_ctx_s *psCtx, jotupix_stream_s *psStream);

/**
 * @brief Compresses a portion of data. This function should be called repeatedly 
 *        until all data has been fully compressed.
 *
 * Each call compresses one block of data and outputs the compressed result.
 *
 * @param psCtx          Pointer to the LZSS compression context.
 * @param pu8OutBuff     Pointer to the output buffer for compressed data.
 * @param u32OutBuffLen  Length of the output buffer.
 *
 * @return 
 *  - < 0 : Error occurred  
 *  - = 0 : Compression complete (all data processed)  
 *  - > 0 : Length of the compressed data produced in this call
 */
int jotupix_lzss_next(jotupix_lzss_ctx_s *psCtx, uint8 *pu8OutBuff, uint32 u32OutBuffLen);

/**
 * @brief Checks whether the LZSS compression process is complete.
 *
 * Determines if all input data has been fully processed and 
 * the compression operation has finished.
 *
 * @param psCtx  Pointer to the LZSS compression context.
 *
 * @return 
 *  TRUE  : Compression is complete  
 *  FALSE : Compression is still in progress
 */
BOOL jotupix_lzss_complete(jotupix_lzss_ctx_s *psCtx);

#ifdef __cplusplus
}
#endif

#endif // __JOTUPIX_LZSS_H__