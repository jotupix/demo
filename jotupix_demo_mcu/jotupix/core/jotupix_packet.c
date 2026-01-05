/**
 * @file    jotupix_packet.c
 * @brief   Data packet sending
 * @note
 *
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */

#include "jotupix_packet.h"

#define TAG     "jotupix_packet"

int jotupix_packet_start(jotupix_packet_ctx_s *psCtx)
{
    uint8 *pu8TxBuffer = NULL;

    if (psCtx == NULL)
    {
        JTLogE(TAG, "Start fail\r\n");
        return FAIL;
    }

    memset(psCtx, 0, sizeof(jotupix_packet_ctx_s));
    psCtx->m_u16WriteLen = 10; // The first part is the package parameters.

    return SUCCESS;
}

int jotupix_packet_putc(jotupix_packet_ctx_s *psCtx, uint8 u8Byte)
{
    // The last byte is the check byte
    if (psCtx == NULL || psCtx->m_u16WriteLen >= PACKET_BUFF_SIZE-1)
    {
        JTLogE(TAG, "Putc fail\r\n");
        return FAIL;
    }

    psCtx->m_au8Buff[psCtx->m_u16WriteLen] = u8Byte;
    psCtx->m_u16WriteLen++;

    return SUCCESS;
}

int jotupix_packet_write(jotupix_packet_ctx_s *psCtx, const uint8 *pu8Data, uint32 u32Len)
{
    if (psCtx == NULL || pu8Data == NULL || u32Len == 0)
    {
        JTLogE(TAG, "Write fail\r\n");
        return FAIL;
    }

    // The last byte is the check byte
    if (psCtx->m_u16WriteLen + u32Len > PACKET_BUFF_SIZE-1)
    {
        JTLogE(TAG, "Buffer full\r\n");
        return FAIL;
    }

    memcpy(psCtx->m_au8Buff + psCtx->m_u16WriteLen, pu8Data, u32Len);
    psCtx->m_u16WriteLen += u32Len;

    return u32Len;
}


int jotupix_packet_end(jotupix_packet_ctx_s *psCtx, jotupix_packet_param_s *psParam, jotupix_protocol_tx_s *psProtocolCtx)
{
    uint8 u8CheckSum = 0;
    uint16 i = 0;
    uint8 *pu8Buff = NULL;
    uint16 u16PayloadLen = 0;

    // If the current packet has no data, the sending fails.
    if (psCtx == NULL || psCtx->m_u16WriteLen < 11 || psProtocolCtx == NULL || psParam == NULL)
    {
        JTLogE(TAG, "Send fail\r\n");
        return FAIL;
    }

    pu8Buff = psCtx->m_au8Buff;

    // The first byte is the message type
    pu8Buff[0] = psParam->m_u8MsgType;

    // packet type, default 0
    if (psParam->m_u8PacketTransType == E_PACKET_TRANS_TYPE_FLAG)
    {
        pu8Buff[1] |= 0x01;
    }

    if (psParam->m_u8TransCompleteFlag > 0)
    {
        pu8Buff[1] |= (0x01 << 1);
    }

    // all data len
    pu8Buff[2] = (psParam->m_u32AllDataLen >> 24) & 0xFF;
    pu8Buff[3] = (psParam->m_u32AllDataLen >> 16) & 0xFF;
    pu8Buff[4] = (psParam->m_u32AllDataLen >> 8) & 0xFF;
    pu8Buff[5] = (psParam->m_u32AllDataLen >> 0) & 0xFF;

    // packet id
    pu8Buff[6] = (psParam->m_u16PacketId >> 8) & 0xFF;
    pu8Buff[7] = (psParam->m_u16PacketId >> 0) & 0xFF;

    // Fill the current packet data length
    u16PayloadLen = psCtx->m_u16WriteLen - 10;
    pu8Buff[8] = (u16PayloadLen >> 8)& 0xFF;
    pu8Buff[9] = (u16PayloadLen >> 0)& 0xFF;

    // Get the check byte, excluding the message type
    for (i=1; i<psCtx->m_u16WriteLen; i++)
    {
        u8CheckSum ^= pu8Buff[i];
    }

    psCtx->m_au8Buff[psCtx->m_u16WriteLen] = u8CheckSum;
    psCtx->m_u16WriteLen++;

    return jotupix_protocol_send(psProtocolCtx, psCtx->m_au8Buff, psCtx->m_u16WriteLen);
}

int jotupix_packet_send(jotupix_packet_ctx_s *psCtx, jotupix_packet_data_s *psPktData, jotupix_protocol_tx_s *psProtocolCtx)
{
    uint8 *pu8TxBuffer = NULL;
    uint8 u8CheckSum = 0;
    uint16 i = 0;
    jotupix_packet_param_s *psPktParam = NULL;

    if (psCtx == NULL || psPktData == NULL || psProtocolCtx == NULL)
    {
        JTLogE(TAG, "send fail\r\n");
        return FAIL;
    }

    memset(psCtx, 0, sizeof(jotupix_packet_ctx_s));

    psPktParam = &psPktData->m_sPktParam;
    pu8TxBuffer = psCtx->m_au8Buff;

    // The first byte is the message type
    pu8TxBuffer[0] = psPktParam->m_u8MsgType;

    // packet type, default 0
    if (psPktParam->m_u8PacketTransType == E_PACKET_TRANS_TYPE_FLAG)
    {
        pu8TxBuffer[1] |= 0x01;
    }

    if (psPktParam->m_u8TransCompleteFlag > 0)
    {
        pu8TxBuffer[1] |= (0x01 << 1);
    }

    // all data len
    pu8TxBuffer[2] = (psPktParam->m_u32AllDataLen >> 24) & 0xFF;
    pu8TxBuffer[3] = (psPktParam->m_u32AllDataLen >> 16) & 0xFF;
    pu8TxBuffer[4] = (psPktParam->m_u32AllDataLen >> 8) & 0xFF;
    pu8TxBuffer[5] = (psPktParam->m_u32AllDataLen >> 0) & 0xFF;

    // packet id
    pu8TxBuffer[6] = (psPktParam->m_u16PacketId >> 8) & 0xFF;
    pu8TxBuffer[7] = (psPktParam->m_u16PacketId >> 0) & 0xFF;

    // Current packet data length, 2 bytes, subsequent padding
    pu8TxBuffer[8] = (psPktData->m_u16PaylaodLen >> 8) & 0xFF;
    pu8TxBuffer[9] = (psPktData->m_u16PaylaodLen >> 0) & 0xFF;
    
    psCtx->m_u16WriteLen = 10;

    memcpy(pu8TxBuffer+psCtx->m_u16WriteLen, psPktData->m_pu8Payload, psPktData->m_u16PaylaodLen);
    psCtx->m_u16WriteLen += psPktData->m_u16PaylaodLen;

    // Get the check byte, excluding the message type
    for (i=1; i<psCtx->m_u16WriteLen; i++)
    {
        u8CheckSum ^= pu8TxBuffer[i];
    }

    pu8TxBuffer[psCtx->m_u16WriteLen] = u8CheckSum;
    psCtx->m_u16WriteLen++;

    return jotupix_protocol_send(psProtocolCtx, psCtx->m_au8Buff, psCtx->m_u16WriteLen);
}
