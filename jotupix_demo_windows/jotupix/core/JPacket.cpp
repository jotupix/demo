//#include "pch.h"
#include <string.h>
#include "JPacket.h"
#include "JByteWriter.h"

#define TAG  "JPacket"

int JPacket::Send(JPacket::Data* psPktData, JProtocol* pProtocol)
{
    uint8_t u8CheckSum = 0;
    uint16_t i = 0;
    JByteWriter sendData;

    if (psPktData == NULL || pProtocol == NULL)
    {
        JLogE(TAG, "Send fail\r\n");
        return -1;
    }

    // The first byte is the message type
    sendData.put_u8(psPktData->m_u8MsgType);

    // packet type, default 0
    uint8_t u8Type = 0;
    if (psPktData->m_ePacketTransType == TransType::FLAG)
    {
        u8Type |= 0x01;
    }

    if (psPktData->m_u8TransCompleteFlag > 0)
    {
        u8Type |= (0x01 << 1);
    }

    sendData.put_u8(u8Type);

    // all data len
    sendData.put_u32(psPktData->m_u32AllDataLen);

    // packet id
    sendData.put_u16(psPktData->m_u16PacketId);

    // Current packet data length, 2 bytes, subsequent padding
    sendData.put_u16(psPktData->m_u16PaylaodLen);

    sendData.put_bytes(psPktData->m_pu8Payload, psPktData->m_u16PaylaodLen);

    // Get the check byte, excluding the message type
    for (i = 1; i < sendData.size(); i++)
    {
        u8CheckSum ^= sendData.buffer[i];
    }

    sendData.put_u8(u8CheckSum);

    return pProtocol->Send(sendData.buffer.data(), sendData.size());
}