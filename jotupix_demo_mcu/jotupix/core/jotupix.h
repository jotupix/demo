/**
 * @file    jotupix.h
 * @brief   Here is the SDK entrance
 * @note
 *  - All functions are NOT thread-safe; synchronization is required in multi-threaded environments.
 *  - The SDK requires 4.5KB of memory.
 *  - If you need to support multiple devices, please refer to this document and modify it accordingly.
 *
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */

#ifndef __JOTUPIX_H__
#define __JOTUPIX_H__

#ifdef __cplusplus
extern "C" {
#endif

#include "jotupix_typedef.h"
#include "jotupix_define.h"
#include "jotupix_crc.h"
#include "jotupix_lzss.h"
#include "jotupix_packet.h"
#include "jotupix_protocol.h"

#define JOTUPIX_MAX_NAME_LEN        20
#define PRO_BUFFER_SIZE             256

typedef enum _JOTUPIX_PROGRAM_GROUP_TYPE_E
{
    E_PROGRAM_GROUP_TYPE_NOR = 0, // Normal Program
}JOTUPIX_PROGRAM_GROUP_TYPE_E;

typedef enum _JOTUPIX_PROGRAM_PLAY_TYPE_E
{
    E_PROGRAM_PlAY_TYPE_CNT = 0, // Play by number of times
    E_PROGRAM_PlAY_TYPE_DUR,  // Play by duration
    E_PROGRAM_PlAY_TYPE_TIME, // Play by time
}JOTUPIX_PROGRAM_PLAY_TYPE_E;

typedef enum _JOTUPIX_SEND_STATUS_E
{
    E_SEND_STATUS_COMPLETED = 0,
    E_SEND_STATUS_PROGRESS, 
    E_SEND_STATUS_FAIL,
}JOTUPIX_SEND_STATUS_E;

typedef enum _JOTUPIX_COMPRESS_FLAG_E
{
    E_COMPRESS_FLAG_DO = 0, // Compress
    E_COMPRESS_FLAG_UNDO,  // No compression is required.
}JOTUPIX_COMPRESS_FLAG_E;

typedef struct _jotupix_dev_info_s
{
    uint8 m_u8SwitchStatus;
    uint8 m_u8Bn;
    uint8 m_u8Flip;
    uint8 m_u8SupportLocalMicFlag;
    uint8 m_u8LocalMicStatus;
    uint8 m_u8LocalMicMode;
    uint8 m_u8EnableShowId;
    uint8 m_u8ProMaxNum;
    uint8 m_u8EnableRemote;
    uint8 m_u8TimerMaxNum;
    uint8 m_u8DevType;
    uint32 m_u32ProjectCode;
    uint16 m_u16Version;
    uint8 m_u8DevleoperFlag;
    uint16 m_u16PktMaxSize; // Maximum supported packet size, default 1024
    uint16 m_u16DevId;
    uint16 m_u16DevWidth;
    uint16 m_u16DevHeight;
    uint8 m_au8DevName[JOTUPIX_MAX_NAME_LEN];
}jotupix_dev_info_s;

typedef struct _jotupix_program_group_nor_s
{
    uint8 m_u8PlayType;
    uint32 m_u32PlayParam;
}jotupix_program_group_nor_s;

typedef struct _jotupix_program_info_s
{
    uint32 m_u32ProCrc;
    uint32 m_u32ProLen; // This length is the length of the data before compression.
    uint8 m_u8ProIndex;
    uint8 m_u8ProAllNum;
    uint8 m_u8Compress;  // Compress or not?
    uint8 m_u8ProGroupType;
    union {
        jotupix_program_group_nor_s m_sProGroupNor;
    } m_unProGoupParam;
}jotupix_program_info_s;

// Get Device Information Callback
typedef void (*jotupix_get_dev_info_f)(jotupix_dev_info_s *psDevInfo);

/**
 * @brief Send program callback
 *
 * @param[in] eStatus JOTUPIX_SEND_STATUS_E
 * @param[in] u8Percent Sending progress percentage 0~100%
 *
 * @return
 */
typedef void (*jotupix_send_program_callback_f)(JOTUPIX_SEND_STATUS_E eStatus, uint8 u8Percent, void *pvUserData);

typedef struct _SEND_PRO_S
{
    BOOL m_bSendStatus;
    jotupix_lzss_ctx_s m_sLzssCtx;
    jotupix_packet_ctx_s m_sPktCtx;
    jotupix_stream_s * m_psStream;
    uint8 m_au8Buffer[PRO_BUFFER_SIZE];
    uint16 m_u16BuffLen;
    jotupix_program_info_s m_sProInfo;
    jotupix_send_program_callback_f m_pfnCallback;
    uint16 m_u16PktId;
    uint32 m_u32TimeoutTick;
    uint8 m_u8RetryCnt;
    void *m_pvUserData;
}SEND_PRO_S;

typedef struct _jotupix_ctx_s
{
    SEND_PRO_S m_sSendPro;
    jotupix_protocol_rx_s m_sRecvCtx;
    jotupix_protocol_tx_s m_sSendCtx;
    jotupix_get_dev_info_f m_pfnGetDevInfoCallback;
    jotupix_dev_info_s m_sDevInfo;
    BOOL m_bGetDevInfo;
    uint32 m_u32CurrMsTick;
}jotupix_ctx_s;

// Log output callback
typedef void (*jotupix_printlog_f)(const char* fmt, ...);

/**
 * @brief Initialize.
 *
 * @param[in] pfnPrintLog printf function
 *
 * @return
 */
void jotupix_init(jotupix_ctx_s *psCtx, jotupix_send_f pfnSend, void *pvUserData);

/**
 * @brief This function is used to register a print function that needs to output logs to a specified window.
 *
 * @param[in] pfnPrintLog printf function
 *
 * @return
 */
void jotupix_register_printlog(jotupix_printlog_f pfnPrintLog);

/**
 * @brief Parse the received data
 *
 * @param[in] pu8Data received data buffer
 * @param[in] u32Len  received data length
 *
 * @return
 */
void jotupix_parse_recv_data(jotupix_ctx_s *psCtx, const uint8 *pu8Data, uint32 u32Len);

/**
 * @brief Sending a command will automatically perform data forced conversion.
 *
 * @param[in] pu8Data send data
 * @param[in] u32Len send length
 *
 * @return 0 on success, other on failure.
 *
 * @note
 *  - Whether the sending is successful or not, you need to wait for the response from the device.
 *  - This response is asynchronous.
 *  - If some commands are not encapsulated by Jotupix, you can assemble the commands yourself and then send them through this interface.
 *
 * @attention
 * 
 */
int jotupix_send_command(jotupix_ctx_s *psCtx, const uint8 *pu8Data, uint32 u32Len);

/**
 * @brief Periodic thread handler for protocol or system tasks.
 *
 * This function should be called periodically (e.g., every system tick)
 * to handle internal timing, state updates, or background operations.
 *
 * @param u32CurrMs Current system time in milliseconds.
 */
void jotupix_thread(jotupix_ctx_s *psCtx, uint32 u32CurrMs);

// Basic function interface

int jotupix_send_switch_status(jotupix_ctx_s *psCtx, uint8 u8Status);

int jotupix_send_screen_flip(jotupix_ctx_s *psCtx, uint8 u8Flip);

int jotupix_send_brightness(jotupix_ctx_s *psCtx, uint8 u8Bn);

// Some products require this command to activate the screen.
int jotupix_send_startup_screen(jotupix_ctx_s *psCtx);

// Retrieving device information is asynchronous; the information is returned in callback.
int jotupix_get_dev_info(jotupix_ctx_s *psCtx, jotupix_get_dev_info_f pfnCallback);

/**
 * @brief Start sending a program to the device.
 *
 * @param psCtx jotupix_ctx_s handle
 * @param psProInfo Program settings and metadata.
 * @param psDataStream program data.
 * @param pfnCallback Callback to receive send progress updates.
 * @param pvUserData 
 * @return 0 on success, negative on error.
 */
int jotupix_send_program(jotupix_ctx_s *psCtx, jotupix_program_info_s *psProInfo, jotupix_stream_s *psDataStream, jotupix_send_program_callback_f pfnCallback, void *pvUserData);

/**
 * @brief Cancel an ongoing program transmission.
 *
 * @return 0 on success, negative on error.
 */
int jotupix_cancel_send_program(jotupix_ctx_s *psCtx);

int jotupix_send_reset(jotupix_ctx_s *psCtx);

//int jotupix_send_program_

#ifdef __cplusplus
}
#endif

#endif // __JOTUPIX_H__
