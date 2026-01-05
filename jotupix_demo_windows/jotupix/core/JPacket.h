#pragma once

#include "JDefine.h"
#include "JProtocol.h"

class JPacket
{
public:
    enum class TransType
    {
        LEN = 0, // Determining whether the transmission is complete is based on the length.
        FLAG = 1, // Use flags to determine whether the transmission is complete.
    };

    /**
     * @brief Packet description used for sending data through JProtocol.
     *
     * This structure contains all metadata required to construct and send a
     * protocol packet, including message type, packet ID, transmission method,
     * and payload information.
     */
    struct Data
    {
        uint8_t m_u8MsgType;  ///< Application-level message type.
        uint32_t m_u32AllDataLen; ///< Total data length (for LEN mode).
        uint16_t m_u16PacketId; ///< Packet sequence ID.
        TransType m_ePacketTransType; ///< Transmission completion method.
        uint8_t m_u8TransCompleteFlag; ///< Completion flag (for FLAG mode).

        const uint8_t* m_pu8Payload; ///< Pointer to payload data buffer.
        uint16_t m_u16PaylaodLen; ///< Payload length in bytes.
    };

    /**
     * @brief Build and send a packet using the provided protocol object.
     *
     * @param psPktData Pointer to the packet data structure describing the packet.
     * @param pProtocol Communication protocol used to send the packet.
     *
     * @return 0 on success,
     *         negative value on error (e.g., invalid parameters or send failure).
     */
	int Send(JPacket::Data *psPktData, JProtocol *pProtocol);

private:
};

