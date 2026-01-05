/**
 * @file    jotupix_lzss.c
 * @brief   Implementation of lzss compression algorithm
 * @note
 *
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */

#include "jotupix_lzss.h"

#define TAG     "jotupix_lzss"

typedef enum _LZSS_STATUS_E
{
    E_LZSS_STATUS_COMPLETE = 0, 
    E_LZSS_STATUS_WORK,
}LZSS_STATUS_E;

static void InitTree(uint16 *pu16Rson, uint16 *pu16Dad)
{
    int i = 0;

    for (i = LZSS_N + 1; i <= LZSS_N + 256; i++)
    {
        pu16Rson[i] = LZSS_NIL;
    }

    for (i = 0; i < LZSS_N; i++)
    {
        pu16Dad[i] = LZSS_NIL;
    }
}

static void InsertNode(jotupix_lzss_ctx_s *psCtx, uint32 r)
{
    uint32  i, p;
    int cmp;

    uint8 *endeBuffer = psCtx->m_au8WorkBuffer;
    uint16 *rson = psCtx->m_au16Rson;
    uint16 *lson = psCtx->m_au16Lson;
    uint16 *dad = psCtx->m_au16Dad;

    cmp = 1;
    p = LZSS_N + 1 + endeBuffer[r];
    rson[r] = lson[r] = LZSS_NIL;
    
    psCtx->m_u32MatchLength = 0;

    for (; ; )
    {
        if (cmp >= 0)
        {
            if (rson[p] != LZSS_NIL)
            {
                p = rson[p];
            }
            else
            {
                rson[p] = r;
                dad[r] = p;
                
                return;
            }
        }
        else
        {
            if (lson[p] != LZSS_NIL)
            {
                p = lson[p];
            }
            else
            {
                lson[p] = r;
                dad[r] = p;
                
                return;
            }
        }

        for (i = 1; i < LZSS_F; i++)
        {
            cmp = endeBuffer[r+i] - endeBuffer[p + i];
        
            if (cmp != 0)
            {
                break;
            }
        }

        if (i > psCtx->m_u32MatchLength)
        {
            psCtx->m_u32MatchPos = p;
            psCtx->m_u32MatchLength = i;
            
            if (i >= LZSS_F)
            {
                break;
            }
        }
    }

    dad[r] = dad[p];
    lson[r] = lson[p];
    rson[r] = rson[p];
    dad[lson[p]] = r;
    dad[rson[p]] = r;

    if (rson[dad[p]] == p)
    {
        rson[dad[p]] = r;
    }
    else
    {
        lson[dad[p]] = r;
    }

    dad[p] = LZSS_NIL;
}

static void DeleteNode(jotupix_lzss_ctx_s *psCtx, uint32 p)
{
    uint32  q = 0;

    uint16 *rson = psCtx->m_au16Rson;
    uint16 *lson = psCtx->m_au16Lson;
    uint16 *dad = psCtx->m_au16Dad;

    if (dad[p] == LZSS_NIL)
    {
        return;
    }

    if (rson[p] == LZSS_NIL)
    {
        q = lson[p];
    }
    else if (lson[p] == LZSS_NIL)
    {
        q = rson[p];
    }
    else
    {
        q = lson[p];

        if (rson[q] != LZSS_NIL)
        {
            do
            {
                q = rson[q];
            } while (rson[q] != LZSS_NIL);

            rson[dad[q]] = lson[q];
            dad[lson[q]] = dad[q];
            lson[q] = lson[p];
            dad[lson[p]] = q;
        }

        rson[q] = rson[p];
        dad[rson[p]] = q;
    }

    dad[q] = dad[p];

    if (rson[dad[p]] == p)
    {
        rson[dad[p]] = q;
    }
    else
    {
        lson[dad[p]] = q;
    }

    dad[p] = LZSS_NIL;
}

static int iInitEncode(jotupix_lzss_ctx_s *psCtx)
{
    int i = 0;
    jotupix_stream_s *psStream = psCtx->m_psStream;

    if (psStream->m_sData.m_u32Size <= psStream->m_sData.m_u32Pos)
    {
        return -1;
    }

    psCtx->m_u32MatchLength = 0;
    psCtx->m_u32MatchPos = 0;

    psCtx->m_au8CodeBuffer[0] = 0;
    psCtx->m_u8CodeBufferPtr = 1;
    psCtx->m_u8CodeBufferIndex = 0;
    psCtx->m_u8Mask = 1;

    psCtx->m_u32S = 0;
    psCtx->m_u32R = LZSS_N - LZSS_F;

    psCtx->m_u32Len = psStream->m_sData.m_u32Size - psStream->m_sData.m_u32Pos;
    if (psCtx->m_u32Len > LZSS_F)
    {
        psCtx->m_u32Len = LZSS_F;
    }

    psStream->m_pfnSeek(&psStream->m_sData, 0);

    i = psStream->m_pfnRead(&psStream->m_sData, psCtx->m_au8WorkBuffer + psCtx->m_u32R, psCtx->m_u32Len);
    if (i != psCtx->m_u32Len)
    {
        return -1;
    }

    InitTree(psCtx->m_au16Rson, psCtx->m_au16Dad);
    
    for (i = psCtx->m_u32S; i < psCtx->m_u32R; i++)
    {
        psCtx->m_au8WorkBuffer[i] = 0;
    }


    for (i = 1; i <= LZSS_F; i++)
    {
        InsertNode(psCtx, psCtx->m_u32R - i);
    }

    InsertNode(psCtx, psCtx->m_u32R);

    psCtx->m_u8Status = E_LZSS_STATUS_WORK;

    return 0;
}

static int iEncode(jotupix_lzss_ctx_s *psCtx, uint8 *pu8OutBuff, uint32 u32OutBuffLen)
{
    uint32 i = 0;
    uint32 u32CurrOutLen = 0;
    uint32 u32CurrCopyLen = 0;
	uint32 last_match_length = 0;
	uint8 c = 0;
	uint8 au8ReadBuffer[LZSS_F] = { 0 };
	uint32 u32ReadLen = 0;
	int nRet = -1;

    if (psCtx->m_u8Status != E_LZSS_STATUS_WORK)
    {
        JTLogE(TAG, "Encode fail, not init\r\n");
        return -1;
    }

    while (psCtx->m_u32Len > 0)
    {
		if ((psCtx->m_u8Mask & 0xFF) == 0)
		{
			goto ENCODE_OUT;
		}

		if (psCtx->m_u32MatchLength > psCtx->m_u32Len)
		{
			psCtx->m_u32MatchLength = psCtx->m_u32Len;
		}

		if (psCtx->m_u32MatchLength <= LZSS_THRESHOLD)
		{
			psCtx->m_u32MatchLength = 1;
			psCtx->m_au8CodeBuffer[0] |= psCtx->m_u8Mask;
			psCtx->m_au8CodeBuffer[psCtx->m_u8CodeBufferPtr++] = psCtx->m_au8WorkBuffer[psCtx->m_u32R];
		}
		else
		{
			psCtx->m_au8CodeBuffer[psCtx->m_u8CodeBufferPtr++] = (unsigned char)psCtx->m_u32MatchPos;
			psCtx->m_au8CodeBuffer[psCtx->m_u8CodeBufferPtr++] = (unsigned char)
				(((psCtx->m_u32MatchPos >> 4) & 0xf0)
					| (psCtx->m_u32MatchLength - (LZSS_THRESHOLD + 1)));
		}

		psCtx->m_u8Mask <<= 1;

		// The status flag is only one byte, 8 bits
		if ((psCtx->m_u8Mask & 0xFF) == 0)
		{
	ENCODE_OUT:
			// If the output length is greater than the current available buffer length, then another output is needed.
			if (psCtx->m_u8CodeBufferPtr > u32OutBuffLen - u32CurrOutLen)
			{
				u32CurrCopyLen = u32OutBuffLen - u32CurrOutLen;
				memcpy(pu8OutBuff + u32CurrOutLen, psCtx->m_au8CodeBuffer+psCtx->m_u8CodeBufferIndex, u32CurrCopyLen);
				psCtx->m_u8CodeBufferPtr -= u32CurrCopyLen;
                psCtx->m_u8CodeBufferIndex += u32CurrCopyLen;
				u32CurrOutLen += u32CurrCopyLen;

				return u32CurrOutLen;
			}
			else
			{
				u32CurrCopyLen = psCtx->m_u8CodeBufferPtr;
				memcpy(pu8OutBuff + u32CurrOutLen, psCtx->m_au8CodeBuffer+psCtx->m_u8CodeBufferIndex, u32CurrCopyLen);
				u32CurrOutLen += u32CurrCopyLen;
			}

			psCtx->m_au8CodeBuffer[0] = 0;
			psCtx->m_u8CodeBufferPtr = psCtx->m_u8Mask = 1;
            psCtx->m_u8CodeBufferIndex = 0;
		}
		
		last_match_length = psCtx->m_u32MatchLength;

		u32ReadLen = psCtx->m_psStream->m_sData.m_u32Size - psCtx->m_psStream->m_sData.m_u32Pos;
		if (u32ReadLen > last_match_length)
		{
			u32ReadLen = last_match_length;
		}

		nRet = psCtx->m_psStream->m_pfnRead(&psCtx->m_psStream->m_sData, au8ReadBuffer, u32ReadLen);
		if (nRet != u32ReadLen)
		{
			psCtx->m_u8Status = E_LZSS_STATUS_COMPLETE;
			return -1;
		}

		for (i = 0; i < u32ReadLen; i++)
		{
			DeleteNode(psCtx, psCtx->m_u32S);

			c = au8ReadBuffer[i];

			psCtx->m_au8WorkBuffer[psCtx->m_u32S] = c;

			if (psCtx->m_u32S < LZSS_F - 1)
			{
				psCtx->m_au8WorkBuffer[psCtx->m_u32S + LZSS_N] = c;
			}

			psCtx->m_u32S = (psCtx->m_u32S + 1) & (LZSS_N - 1);
			psCtx->m_u32R = (psCtx->m_u32R + 1) & (LZSS_N - 1);

			InsertNode(psCtx, psCtx->m_u32R);
		}

		// The length of readable data is less than last_match_length
		while (i++ < last_match_length)
		{
			DeleteNode(psCtx, psCtx->m_u32S);

			psCtx->m_u32S = (psCtx->m_u32S + 1) & (LZSS_N - 1);
			psCtx->m_u32R = (psCtx->m_u32R + 1) & (LZSS_N - 1);

			if (--psCtx->m_u32Len)
			{
				InsertNode(psCtx, psCtx->m_u32R);
			}
		}
    }

    // All data has been read
    while (psCtx->m_u8CodeBufferPtr > 0)
    {
        if (psCtx->m_u8CodeBufferPtr > u32OutBuffLen - u32CurrOutLen)
        {
            u32CurrCopyLen = u32OutBuffLen - u32CurrOutLen;
        }
        else
        {
            u32CurrCopyLen = psCtx->m_u8CodeBufferPtr;
        }

        memcpy(pu8OutBuff + u32CurrOutLen, psCtx->m_au8CodeBuffer+psCtx->m_u8CodeBufferIndex, u32CurrCopyLen);
        psCtx->m_u8CodeBufferPtr -= u32CurrCopyLen;
        psCtx->m_u8CodeBufferIndex += u32CurrCopyLen;
        u32CurrOutLen += u32CurrCopyLen;

        if (psCtx->m_u8CodeBufferPtr == 0)
        {
            psCtx->m_u8Status = E_LZSS_STATUS_COMPLETE;
        }

        return u32CurrOutLen;
    }

    psCtx->m_u8Status = E_LZSS_STATUS_COMPLETE;

    return 0;
}

int jotupix_lzss_start(jotupix_lzss_ctx_s *psCtx, jotupix_stream_s *psStream)
{
    if (psCtx == NULL || psStream == NULL)
    {
        JTLogE(TAG, "start fail\r\n");
        return -1;
    }

    memset(psCtx, 0, sizeof(jotupix_lzss_ctx_s));

    psCtx->m_psStream = psStream;

    return iInitEncode(psCtx);
}

int jotupix_lzss_next(jotupix_lzss_ctx_s *psCtx, uint8 *pu8OutBuff, uint32 u32OutBuffLen)
{
    if (psCtx == NULL)
    {
        JTLogE(TAG, "next fail\r\n");
        return -1;
    }

    return iEncode(psCtx, pu8OutBuff, u32OutBuffLen);
}

BOOL jotupix_lzss_complete(jotupix_lzss_ctx_s *psCtx)
{
    if (psCtx == NULL)
    {
        JTLogE(TAG, "Complete fail\r\n");
        return FALSE;
    }

    if (psCtx->m_u8Status == E_LZSS_STATUS_WORK)
    {
        return FALSE;
    }
    else
    {
        return TRUE;
    }
}
