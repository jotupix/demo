/**
 * @file    jotupix_protocol.h
 * @brief   Convert the sent data according to the protocol
 * @note
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */

#ifndef __JOTUPIX_PROTOCOL_H__
#define __JOTUPIX_PROTOCOL_H__

#ifdef __cplusplus
extern "C" {
#endif

#include "jotupix_typedef.h"
#include "jotupix_define.h"

#define PROTOCOL_TX_BUFF_SIZE          512        // Send buffer size
#define PROTOCOL_PARSE_BUFF_SIZE       256       // buff length of parsed data

typedef struct _jotupix_protocol_tx_s
{
    uint8 m_au8TxBuffer[PROTOCOL_TX_BUFF_SIZE];
    jotupix_send_f m_pfnSend;
    void *m_pvUserData;
}jotupix_protocol_tx_s;

typedef struct _jotupix_protocol_rx_s
{
    uint8 m_au8ParseBuffer[PROTOCOL_PARSE_BUFF_SIZE];
    uint8 m_u8RecvStatus;
    uint16 m_u16RecvIndex;
    uint16 m_u16ParseDataLen;
    BOOL m_bInEsc;

    jotupix_parse_complete_f m_pfnCallback;
    void *m_pvUserData;
}jotupix_protocol_rx_s;

/**
 * @brief To send data
 *
 * @param[in] psProtocol Structure handle
 * @param[in] pu8Data data buffer
 * @param[in] u32Len  data length, max 65535
 *
 * @return 0 on success, other on failure.
 */
int jotupix_protocol_send(jotupix_protocol_tx_s *psProtocol, const uint8 *pu8Data, uint32 u32Len);

/**
 * @brief parse the received data, After successful parsing, the parsed data will be returned to the application layer through callback
 *
 * @param[in] psProtocol Structure handle
 * @param[in] pu8Data data buffer
 * @param[in] u32Len  data length
 *
 * @return 0 on success, other on failure.
 */
void jotupix_protocol_parse(jotupix_protocol_rx_s *psProtocol, const uint8 *pu8Data, uint32 u32Len);

#ifdef __cplusplus
}
#endif

#endif // __JOTUPIX_PROTOCOL_H__

