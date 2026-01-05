/**
 * @file    jotupix_protocol.c
 * @brief   Convert the sent data according to the protocol
 * @note
 *
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */

#include "jotupix_protocol.h"

#define TAG     "jotupix_protocol"

#define PROTOCOL_START_CHAR           0x01      // Start mark
#define PROTOCOL_ESC_CHAR             0x02      // Protocol Flags
#define PROTOCOL_ESC_XOR_CHAR         0x04      // Protocol XOR value
#define PROTOCOL_END_CHAR             0x03      // End Marker

#define PROTOCOL_SEND_MAX_LEN         65535

typedef enum _PROTOCOL_RECV_STATUS_E
{
    E_PROTOCOL_RECV_STATUS_DATA = 0, // Receiving Data
    E_PROTOCOL_RECV_STATUS_START,    // Receive start signal
    E_PROTOCOL_RECV_STATUS_DATALEN_H, // Receive data length high bit
    E_PROTOCOL_RECV_STATUS_DATALEN_L, // Receive data length low bit
} PROTOCOL_RECV_STATUS_E;

#define iXor(V) \
    if ((V) > 0 && (V) < PROTOCOL_ESC_XOR_CHAR) \
    { \
        psProtocol->m_au8TxBuffer[u16Index++] = PROTOCOL_ESC_CHAR; \
        psProtocol->m_au8TxBuffer[u16Index++] = ((V) | PROTOCOL_ESC_XOR_CHAR); \
    } \
    else \
    { \
        psProtocol->m_au8TxBuffer[u16Index++] = (V); \
    }

int jotupix_protocol_send(jotupix_protocol_tx_s *psProtocol, const uint8 *pu8Data, uint32 u32Len)
{
    uint32 i = 0;
    uint16 u16Index = 0;
    uint8 u8Tmp = 0;
    int nRet = -1;

    if (psProtocol == NULL || psProtocol->m_pfnSend == NULL)
    {
        JTLogE(TAG, "Send data fail, send is NULL\r\n");
        return -1;
    }

    if (pu8Data == NULL || u32Len == 0 || u32Len > PROTOCOL_SEND_MAX_LEN)
    {
        JTLogE(TAG, "Send data fail, input error\r\n");
        return -1;
    }

    psProtocol->m_au8TxBuffer[u16Index++] = PROTOCOL_START_CHAR;
    
    // send data length
    u8Tmp = (u32Len>>8) & 0xFF;
    iXor(u8Tmp);

    u8Tmp = u32Len & 0xFF;
    iXor(u8Tmp);

    for (i=0; i<u32Len; i++)
    {
        iXor(pu8Data[i]);

        if (u16Index > PROTOCOL_TX_BUFF_SIZE-2)
        {
            nRet = psProtocol->m_pfnSend(psProtocol->m_au8TxBuffer, u16Index, psProtocol->m_pvUserData);
            if (nRet != 0)
            {
                return -1;
            }

            u16Index = 0;
        }
    }

    psProtocol->m_au8TxBuffer[u16Index++] = (PROTOCOL_END_CHAR);

    return psProtocol->m_pfnSend(psProtocol->m_au8TxBuffer, u16Index, psProtocol->m_pvUserData);
}

void jotupix_protocol_parse(jotupix_protocol_rx_s *psProtocol, const uint8 *pu8Data, uint32 u32Len)
{
    uint32 i = 0;
    uint8 u8Byte = 0;

    if (psProtocol == NULL || pu8Data == NULL || u32Len == 0)
    {
        JTLogE(TAG, "parse data fail, input error\r\n");
        return;
    }

    for (i=0; i<u32Len; i++)
    {
        u8Byte = pu8Data[i];
    
        switch (u8Byte)
        {
            case PROTOCOL_START_CHAR:      // Start Marker
                psProtocol->m_u16ParseDataLen = 0;
                psProtocol->m_bInEsc = FALSE;
                psProtocol->m_u8RecvStatus = E_PROTOCOL_RECV_STATUS_DATALEN_H;  // Waiting to receive data length
                break;
            case PROTOCOL_ESC_CHAR:     //0x02
                psProtocol->m_bInEsc = TRUE;
                break;
            case PROTOCOL_END_CHAR:    // End Marker
                if (psProtocol->m_u8RecvStatus == E_PROTOCOL_RECV_STATUS_DATA
                    && psProtocol->m_u16ParseDataLen <= PROTOCOL_PARSE_BUFF_SIZE
                    && psProtocol->m_u16ParseDataLen > 0)
                {
                    if (psProtocol->m_pfnCallback != NULL)
                    {
                        psProtocol->m_pfnCallback(psProtocol->m_au8ParseBuffer, psProtocol->m_u16ParseDataLen, psProtocol->m_pvUserData);                    
                    }
                }

                psProtocol->m_u8RecvStatus = E_PROTOCOL_RECV_STATUS_START; // Waiting for receiving start flag
                break;
            default:
                if(psProtocol->m_bInEsc)   //0x02
                {
                    u8Byte ^= PROTOCOL_ESC_XOR_CHAR;   // XOR operation
                    psProtocol->m_bInEsc = FALSE;
                }

                switch(psProtocol->m_u8RecvStatus)
                {
                    case E_PROTOCOL_RECV_STATUS_DATA:
                    {
                        if (psProtocol->m_u16RecvIndex < psProtocol->m_u16ParseDataLen)
                        {
                            psProtocol->m_au8ParseBuffer[psProtocol->m_u16RecvIndex] = u8Byte;
                            psProtocol->m_u16RecvIndex++;
                        }
                    }
                    break;
                    case E_PROTOCOL_RECV_STATUS_DATALEN_H:
                    {
                        psProtocol->m_u16ParseDataLen = u8Byte << 8;
                        psProtocol->m_u8RecvStatus = E_PROTOCOL_RECV_STATUS_DATALEN_L;
                    }
                    break;
                    case E_PROTOCOL_RECV_STATUS_DATALEN_L:
                    {
                        psProtocol->m_u16ParseDataLen |= u8Byte;

                        if (psProtocol->m_u16ParseDataLen > PROTOCOL_PARSE_BUFF_SIZE || psProtocol->m_u16ParseDataLen == 0)
                        {
                            psProtocol->m_u8RecvStatus = E_PROTOCOL_RECV_STATUS_START;
                            psProtocol->m_u16ParseDataLen = 0;
                        }
                        else
                        {
                            psProtocol->m_u8RecvStatus = E_PROTOCOL_RECV_STATUS_DATA;
                            psProtocol->m_u16RecvIndex = 0;
                        }
                    }
                    break;
                    default:
                        break;
                }
                break;
        }
    }
}

