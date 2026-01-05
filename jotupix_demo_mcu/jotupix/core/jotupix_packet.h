/**
 * @file    jotupix_packet.h
 * @brief   Data packet sending
 * @note
 *
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */
#ifndef __JOTUPIX_PACKET_H__
#define __JOTUPIX_PACKET_H__

#ifdef __cplusplus
extern "C" {
#endif

#include "jotupix_typedef.h"
#include "jotupix_define.h"
#include "jotupix_protocol.h"

#ifndef PACKET_PAYLOAD_SIZE
#define PACKET_PAYLOAD_SIZE       1024                  // Maximum length of a package. If RAM is tight, adjust this value to a smaller value.
#endif
#define PACKET_BUFF_SIZE      (PACKET_PAYLOAD_SIZE + 11)  // Packet buffer length

typedef enum _PACKET_TRANS_TYPE_E
{
    E_PACKET_TRANS_TYPE_LEN = 0, // 
    E_PACKET_TRANS_TYPE_FLAG = 1, //
}PACKET_TRANS_TYPE_E;

typedef struct _jotupix_packet_ctx_s
{
    uint8 m_au8Buff[PACKET_BUFF_SIZE];
    uint16 m_u16WriteLen;
}jotupix_packet_ctx_s;

typedef struct _jotupix_packet_param_s
{
    uint8 m_u8MsgType;
    uint32 m_u32AllDataLen;
    uint16 m_u16PacketId;
    uint8 m_u8PacketTransType;
    uint8 m_u8TransCompleteFlag;
}jotupix_packet_param_s;

typedef struct _jotupix_packet_data_s
{
    jotupix_packet_param_s m_sPktParam;
    
    const uint8 *m_pu8Payload;
    uint16 m_u16PaylaodLen;
}jotupix_packet_data_s;

/**
 * @brief Begins constructing a packet data stream.
 *
 * Initializes the packet context and prepares for writing data
 * into the packet buffer.
 *
 * @param psCtx Pointer to the packet context structure.
 * @return 0 on success, or a negative value on error.
 */
int jotupix_packet_start(jotupix_packet_ctx_s *psCtx);

/**
 * @brief Writes a single byte into the packet data stream.
 *
 * Appends one byte to the packet buffer.
 *
 * @param psCtx  Pointer to the packet context structure.
 * @param u8Byte The byte value to be written.
 * @return 0 on success, or a negative value on error.
 */
int jotupix_packet_putc(jotupix_packet_ctx_s *psCtx, uint8 u8Byte);

/**
 * @brief Writes multiple bytes into the packet data stream.
 *
 * Appends a block of data to the packet buffer.
 *
 * @param psCtx    Pointer to the packet context structure.
 * @param pu8Data  Pointer to the data buffer to be written.
 * @param u32Len   Number of bytes to write.
 * @return 0 on success, or a negative value on error.
 */
int jotupix_packet_write(jotupix_packet_ctx_s *psCtx, const uint8 *pu8Data, uint32 u32Len);

/**
 * @brief Finishes constructing the packet and triggers transmission.
 *
 * Finalizes the packet, applies any protocol-specific framing,
 * and initiates the sending process.
 *
 * @param psCtx         Pointer to the packet context structure.
 * @param psParam       Pointer to the packet parameter structure.
 * @param psProtocolCtx Pointer to the protocol transmission context.
 * @return 0 on success, or a negative value on error.
 */
int jotupix_packet_end(jotupix_packet_ctx_s *psCtx, jotupix_packet_param_s *psParam, jotupix_protocol_tx_s *psProtocolCtx);

/**
 * @brief Sends an already constructed packet.
 *
 * Transmits the given packet data using the specified protocol context.
 *
 * @param psCtx         Pointer to the packet context structure.
 * @param psPktData     Pointer to the packet data structure.
 * @param psProtocolCtx Pointer to the protocol transmission context.
 * @return 0 on success, or a negative value on error.
 */
int jotupix_packet_send(jotupix_packet_ctx_s *psCtx, jotupix_packet_data_s *psPktData, jotupix_protocol_tx_s *psProtocolCtx);

#ifdef __cplusplus
}
#endif

#endif // __JOTUPIX_PACKET_H__
