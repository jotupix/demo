//#include "pch.h"
#include "JProtocol.h"
#include "JLog.h"

#define TAG     "JProtocol"

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
        m_au8TxBuffer[u16Index++] = PROTOCOL_ESC_CHAR; \
        m_au8TxBuffer[u16Index++] = ((V) | PROTOCOL_ESC_XOR_CHAR); \
    } \
    else \
    { \
        m_au8TxBuffer[u16Index++] = (V); \
    }

void JProtocol::Init(IJSend* pfnSender, IJParseCallback *pfnCallback)
{
    m_pfnSender = pfnSender;
    m_pfnCallback = pfnCallback;
}

int JProtocol::Send(const uint8_t* pu8Data, uint32_t u32Len)
{
    uint32_t i = 0;
    uint16_t u16Index = 0;
    uint8_t u8Tmp = 0;
    int nRet = -1;

    if (m_pfnSender == NULL)
    {
        JLogE(TAG, "Send data fail, send is NULL\r\n");
        return -1;
    }

    if (pu8Data == NULL || u32Len == 0 || u32Len > PROTOCOL_SEND_MAX_LEN)
    {
        JLogE(TAG, "Send data fail, input error\r\n");
        return -1;
    }

    m_au8TxBuffer[u16Index++] = PROTOCOL_START_CHAR;

    // send data length
    u8Tmp = (u32Len >> 8) & 0xFF;
    iXor(u8Tmp);

    u8Tmp = u32Len & 0xFF;
    iXor(u8Tmp);

    for (i = 0; i < u32Len; i++)
    {
        iXor(pu8Data[i]);

        if (u16Index > JPROTOCOL_TX_BUFF_SIZE - 2)
        {
            nRet = m_pfnSender->Send(m_au8TxBuffer, u16Index);
            if (nRet != 0)
            {
                return -1;
            }

            u16Index = 0;
        }
    }

    m_au8TxBuffer[u16Index++] = (PROTOCOL_END_CHAR);

    return m_pfnSender->Send(m_au8TxBuffer, u16Index);
}

void JProtocol::Parse(const uint8_t* pu8Data, uint32_t u32Len)
{
    uint32_t i = 0;
    uint8_t u8Byte = 0;

    if (pu8Data == NULL || u32Len == 0)
    {
        JLogE(TAG, "Parse data fail, input error\r\n");
        return;
    }

    for (i = 0; i < u32Len; i++)
    {
        u8Byte = pu8Data[i];

        switch (u8Byte)
        {
        case PROTOCOL_START_CHAR:      // Start Marker
            m_u16ParseDataLen = 0;
            m_bInEsc = false;
            m_u8RecvStatus = E_PROTOCOL_RECV_STATUS_DATALEN_H;  // Waiting to receive data length
            break;
        case PROTOCOL_ESC_CHAR:     //0x02
            m_bInEsc = true;
            break;
        case PROTOCOL_END_CHAR:    // End Marker
            if (m_u8RecvStatus == E_PROTOCOL_RECV_STATUS_DATA
                && m_u16ParseDataLen <= JPROTOCOL_RX_PARSE_BUFF_SIZE
                && m_u16ParseDataLen > 0)
            {
                if (m_pfnCallback != NULL)
                {
                    m_pfnCallback->onParseComplete(m_au8ParseBuffer, m_u16ParseDataLen);
                }
            }

            m_u8RecvStatus = E_PROTOCOL_RECV_STATUS_START; // Waiting for receiving start flag
            break;
        default:
            if (m_bInEsc)   //0x02
            {
                u8Byte ^= PROTOCOL_ESC_XOR_CHAR;   // XOR operation
                m_bInEsc = false;
            }

            switch (m_u8RecvStatus)
            {
            case E_PROTOCOL_RECV_STATUS_DATA:
            {
                if (m_u16RecvIndex < m_u16ParseDataLen)
                {
                    m_au8ParseBuffer[m_u16RecvIndex] = u8Byte;
                    m_u16RecvIndex++;
                }
            }
            break;
            case E_PROTOCOL_RECV_STATUS_DATALEN_H:
            {
                m_u16ParseDataLen = u8Byte << 8;
                m_u8RecvStatus = E_PROTOCOL_RECV_STATUS_DATALEN_L;
            }
            break;
            case E_PROTOCOL_RECV_STATUS_DATALEN_L:
            {
                m_u16ParseDataLen |= u8Byte;

                if (m_u16ParseDataLen > JPROTOCOL_RX_PARSE_BUFF_SIZE || m_u16ParseDataLen == 0)
                {
                    m_u8RecvStatus = E_PROTOCOL_RECV_STATUS_START;
                    m_u16ParseDataLen = 0;
                }
                else
                {
                    m_u8RecvStatus = E_PROTOCOL_RECV_STATUS_DATA;
                    m_u16RecvIndex = 0;
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