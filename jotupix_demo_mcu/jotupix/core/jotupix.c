/**
 * @file    jotupix.c
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
#include "jotupix.h"
#include "jotupix_protocol.h"
#include "jotupix_packet.h"
#include "jotupix_lzss.h"
#include "jotupix_tick.h"

#define TAG     "jotupix"

#define SEND_PRO_TIMEOUT_TICK       (5*1000)  // ms
#define DEFAULT_PKT_MAX_SIZE        1024
#define RETRY_MAX_CNT               3  // Maximum number of retries

// Commands executable
typedef enum _ACTION_E
{
    E_ACTION_UNKOWN = 0,

    E_ACTION_APP_MUSIC = 0x01,                  // Music rhythm mode 1
    E_ACTION_APP_START_SET_PRO,                 // Start setting program content
    E_ACTION_APP_SET_PRO,                       // Set program content
    E_ACTION_APP_SET_BN,                        // Set display brightness
    E_ACTION_APP_SET_SWITCH_STATUS,             // Set switch status (6)
    E_ACTION_APP_SET_LOCAL_MUSIC,               // Local microphone mode
    E_ACTION_APP_PLAY_PROLIST_BY_INDEX,         // Play program by index
    E_ACTION_APP_DEL_PROLIST_BY_INDEX,          // Delete program by index
    E_ACTION_APP_UPDATE_TIME,                   // Update time
    E_ACTION_APP_SET_TIMERS,                    // Set timers
    E_ACTION_APP_GET_TIMERS,                    // Get timers
    E_ACTION_APP_SET_FLIP,                      // Set flip status

    E_ACTION_APP_CHECK_PASSWORD,                // Check password (6 digits)
    E_ACTION_APP_SET_PASSWORD,                  // Set password (6 digits)
    E_ACTION_APP_OPR_COUNTDOWN,                 // Countdown operation 0x0F
    E_ACTION_APP_OPR_STOPWATCH,                 // Stopwatch operation 0x10
    E_ACTION_APP_OPR_SCOREBOARD,                // Scoreboard operation 0x11
    E_ACTION_APP_SET_GRAPHICS,                  // Set graphic drawing info 0x12
    E_ACTION_APP_OPR_LIGHT,                     // Light operation: set color, dynamic modes, etc. 0x13
    E_ACTION_APP_SET_DEV_INFO = 0x1E,          // Set device configuration info
    E_ACTION_APP_GET_DEV_INFO = 0x1F,          // Get device information

    // Messages from BLE to MCU
    E_ACTION_BLE_TO_MCU_RESET = 0x20,
    E_ACTION_BLE_TO_MCU_STARTUP = 0x23,

    E_ACTION_MCU_TO_BLE_SET_SCREEN_PARAM = 0x30,  // Set screen parameters
    E_ACTION_MCU_TO_BLE_SET_NAME,                // Set device name (31)
    E_ACTION_MCU_TO_BLE_STARTUP_OPR,             // start up operation (32)
} ACTION_E;

//static jotupix_protocol_rx_s s_sRecvCtx;
//static jotupix_protocol_tx_s s_sSendCtx;

//static SEND_PRO_S s_sSendPro;

//static jotupix_get_dev_info_f s_pfnGetDevInfoCallback = NULL;

//static uint32 s_u32CurrMsTick = 0;
//static jotupix_dev_info_s s_sDevInfo;
//static BOOL s_bGetDevInfo = FALSE;

static int iInitRecv(jotupix_protocol_rx_s *psCtx, jotupix_parse_complete_f pfnCallback, void *pvUserData);
static int iInitSend(jotupix_protocol_tx_s *psCtx, jotupix_send_f pfnSend, void *pvUserData);
static void iParseComplete(const uint8 *pu8Data, uint32 u32Len, void *pvUserData);
static void iPrintLogArrToHex(const uint8 *pu8Title, const uint8 *pu8Data, uint32 u32Len);
static int jotupix_send_program_start(jotupix_ctx_s *psCtx, jotupix_program_info_s *psProInfo);

void jotupix_init(jotupix_ctx_s *psCtx, jotupix_send_f pfnSend, void *pvUserData)
{
    // Since multiple objects share a single iParseComplete, it's necessary to distinguish which object's callback it is.
    iInitRecv(&psCtx->m_sRecvCtx, iParseComplete, psCtx); 
	iInitSend(&psCtx->m_sSendCtx, pfnSend, pvUserData);
    psCtx->m_bGetDevInfo = FALSE;
}

void jotupix_register_printlog(jotupix_printlog_f pfnPrintLog)
{
    jotupix_log_register_print((jotupix_log_print_f)pfnPrintLog);
}

static int iInitRecv(jotupix_protocol_rx_s *psCtx, jotupix_parse_complete_f pfnCallback, void *pvUserData)
{
    if (psCtx == NULL || pfnCallback == NULL)
    {
        JTLogE(TAG, "init recv fail\r\n");
        return FAIL;
    }

    memset(psCtx, 0, sizeof(jotupix_protocol_rx_s));
    psCtx->m_pfnCallback = pfnCallback;
    psCtx->m_pvUserData = pvUserData;

    return SUCCESS;
}

static int iInitSend(jotupix_protocol_tx_s *psCtx, jotupix_send_f pfnSend, void *pvUserData)
{
    if (psCtx == NULL || pfnSend == NULL)
    {
        JTLogE(TAG, "init send fail\r\n");
        return FAIL;
    }

    memset(psCtx, 0, sizeof(jotupix_protocol_tx_s));
    psCtx->m_pfnSend = pfnSend;
    psCtx->m_pvUserData = pvUserData;

    return SUCCESS;
}

int jotupix_send_command(jotupix_ctx_s *psCtx, const uint8 *pu8Data, uint32 u32Len)
{
    int nRet = FAIL;

    if (pu8Data == NULL || u32Len == 0)
    {
        JTLogE(TAG, "send command fail\r\n");
        return nRet;
    }

    // Responses need to be processed asynchronously

    nRet = jotupix_protocol_send(&psCtx->m_sSendCtx, pu8Data, u32Len);
    
    return nRet;
}

void jotupix_parse_recv_data(jotupix_ctx_s *psCtx, const uint8 *pu8Data, uint32 u32Len)
{
    if (pu8Data == NULL || u32Len == 0)
    {
        JTLogE(TAG, "recv command fail\r\n");
        return;
    }

    jotupix_protocol_parse(&psCtx->m_sRecvCtx, pu8Data, u32Len);
}

void jotupix_thread(jotupix_ctx_s *psCtx, uint32 u32CurrMs)
{
    psCtx->m_u32CurrMsTick = u32CurrMs;

    if (psCtx->m_sSendPro.m_bSendStatus)
    {
        if (time_after_eq(u32CurrMs, psCtx->m_sSendPro.m_u32TimeoutTick))
        {
            psCtx->m_sSendPro.m_bSendStatus = FALSE;
            JTLogE(TAG, "Program transmission timed out, ended.\r\n");
            if (psCtx->m_sSendPro.m_pfnCallback != NULL)
            {
                psCtx->m_sSendPro.m_pfnCallback(E_SEND_STATUS_FAIL, 0, psCtx->m_sSendPro.m_pvUserData);
            }
        }
    }
}

int jotupix_send_switch_status(jotupix_ctx_s *psCtx, uint8 u8Status)
{
    uint8 au8Data[2] = {0};

    au8Data[0] = E_ACTION_APP_SET_SWITCH_STATUS;
    au8Data[1] = u8Status;

    return jotupix_protocol_send(&psCtx->m_sSendCtx, au8Data, sizeof(au8Data));
}

int jotupix_send_screen_flip(jotupix_ctx_s *psCtx, uint8 u8Flip)
{
    uint8 au8Data[2] = {0};
    
    au8Data[0] = E_ACTION_APP_SET_FLIP;
    au8Data[1] = u8Flip;

    return jotupix_protocol_send(&psCtx->m_sSendCtx, au8Data, sizeof(au8Data));
}

int jotupix_send_brightness(jotupix_ctx_s *psCtx, uint8 u8Bn)
{
    uint8 au8Data[2] = {0};
    
    au8Data[0] = E_ACTION_APP_SET_BN;
    au8Data[1] = u8Bn;

    return jotupix_protocol_send(&psCtx->m_sSendCtx, au8Data, sizeof(au8Data));
}

int jotupix_send_startup_screen(jotupix_ctx_s *psCtx)
{
    uint8 au8Data[2] = {0};
    
    au8Data[0] = E_ACTION_BLE_TO_MCU_STARTUP;
    au8Data[1] = ON;

    return jotupix_protocol_send(&psCtx->m_sSendCtx, au8Data, sizeof(au8Data));
}

int jotupix_get_dev_info(jotupix_ctx_s *psCtx, jotupix_get_dev_info_f pfnCallback)
{
    uint8 au8Data[1] = {0};

    if (psCtx->m_bGetDevInfo)
    {
        if (pfnCallback != NULL)
        {
            pfnCallback(&psCtx->m_sDevInfo);
        }
        
        return SUCCESS;
    }
    
    au8Data[0] = E_ACTION_APP_GET_DEV_INFO;
    psCtx->m_pfnGetDevInfoCallback = pfnCallback;
    
    return jotupix_protocol_send(&psCtx->m_sSendCtx, au8Data, sizeof(au8Data)); 
}

int jotupix_send_reset(jotupix_ctx_s *psCtx)
{
    uint8 au8Data[2] = {0};
    
    au8Data[0] = E_ACTION_BLE_TO_MCU_RESET;
    au8Data[1] = 0x05;

    return jotupix_protocol_send(&psCtx->m_sSendCtx, au8Data, sizeof(au8Data));
}

int jotupix_send_program(jotupix_ctx_s *psCtx, jotupix_program_info_s *psProInfo, jotupix_stream_s *psDataStream, jotupix_send_program_callback_f pfnCallback, void *pvUserData)
{
    int nRet = FAIL;

    if (psCtx->m_sSendPro.m_bSendStatus)
    {
        JTLogE(TAG, "Waiting for the last program to finish sending\r\n");
        return FAIL;
    }

    memset(&psCtx->m_sSendPro, 0, sizeof(SEND_PRO_S));

#if SUPPORT_COMPRESS
    if (psProInfo->m_u8Compress == E_COMPRESS_FLAG_DO)
    {
        nRet = jotupix_lzss_start(&psCtx->m_sSendPro.m_sLzssCtx, psDataStream);
        if (nRet != SUCCESS)
        {
            JTLogE(TAG, "Send program, lzss start fail\r\n");
            return FAIL;
        }
    }
#endif

    memcpy(&psCtx->m_sSendPro.m_sProInfo, psProInfo, sizeof(jotupix_program_info_s));
    psCtx->m_sSendPro.m_psStream = psDataStream;
    psCtx->m_sSendPro.m_pfnCallback = pfnCallback;
    psCtx->m_sSendPro.m_pvUserData = pvUserData;
    psCtx->m_sSendPro.m_u32TimeoutTick = time_get_next_tick(psCtx->m_u32CurrMsTick, SEND_PRO_TIMEOUT_TICK);
    psCtx->m_sSendPro.m_bSendStatus = TRUE;

    // Send command to start setting program content
    nRet = jotupix_send_program_start(psCtx, psProInfo);
    if (nRet != 0)
    {
        JTLogE(TAG, "Send program, start fail\r\n");
        memset(&psCtx->m_sSendPro, 0, sizeof(SEND_PRO_S));
        return FAIL;
    }

    if (psCtx->m_sSendPro.m_pfnCallback != NULL)
    {
        psCtx->m_sSendPro.m_pfnCallback(E_SEND_STATUS_PROGRESS, 0, psCtx->m_sSendPro.m_pvUserData);
    }

    return SUCCESS;
}

int jotupix_cancel_send_program(jotupix_ctx_s *psCtx)
{
    JTLogD(TAG, "Cancel send program!\r\n");
    memset(&psCtx->m_sSendPro, 0, sizeof(SEND_PRO_S));  

    return SUCCESS;
}

static int jotupix_send_program_start(jotupix_ctx_s *psCtx, jotupix_program_info_s *psProInfo)
{
    uint8 au8Buffer[32] = {0};
    uint8 u8Index = 0;

    au8Buffer[u8Index++] = E_ACTION_APP_START_SET_PRO;
    
    au8Buffer[u8Index++] = (psProInfo->m_u32ProCrc >> 24) & 0xFF;
    au8Buffer[u8Index++] = (psProInfo->m_u32ProCrc >> 16) & 0xFF;
    au8Buffer[u8Index++] = (psProInfo->m_u32ProCrc >> 8) & 0xFF;
    au8Buffer[u8Index++] = (psProInfo->m_u32ProCrc >> 0) & 0xFF;

    au8Buffer[u8Index++] = (psProInfo->m_u32ProLen >> 24) & 0xFF;
    au8Buffer[u8Index++] = (psProInfo->m_u32ProLen >> 16) & 0xFF;
    au8Buffer[u8Index++] = (psProInfo->m_u32ProLen >> 8) & 0xFF;
    au8Buffer[u8Index++] = (psProInfo->m_u32ProLen >> 0) & 0xFF;

    au8Buffer[u8Index++] = psProInfo->m_u8ProIndex;
    au8Buffer[u8Index++] = psProInfo->m_u8ProAllNum;
    au8Buffer[u8Index++] = 0;

    memset(au8Buffer+u8Index, 0, 7); // Reserved bytes
    u8Index += 7;

#if SUPPORT_COMPRESS
    au8Buffer[u8Index++] = psProInfo->m_u8Compress;
#else
    au8Buffer[u8Index++] = E_COMPRESS_FLAG_UNDO;
#endif
    au8Buffer[u8Index++] = psProInfo->m_u8ProGroupType;

    switch (psProInfo->m_u8ProGroupType)
    {
    case E_PROGRAM_GROUP_TYPE_NOR:
        jotupix_program_group_nor_s *psGroupNor = (jotupix_program_group_nor_s *)&psProInfo->m_unProGoupParam.m_sProGroupNor;
    
        au8Buffer[u8Index++] = psGroupNor->m_u8PlayType;
    
        au8Buffer[u8Index++] = (psGroupNor->m_u32PlayParam >> 24) & 0xFF;
        au8Buffer[u8Index++] = (psGroupNor->m_u32PlayParam >> 16) & 0xFF;
        au8Buffer[u8Index++] = (psGroupNor->m_u32PlayParam >> 8) & 0xFF;
        au8Buffer[u8Index++] = (psGroupNor->m_u32PlayParam >> 0) & 0xFF;
        break;
    default:
        break;
    }

    JTLogD(TAG, "Send Start Program Setup Command.\r\n");

    return jotupix_protocol_send(&psCtx->m_sSendCtx, au8Buffer, u8Index);
}

static int iSendProgramNextPacketByCompress(jotupix_ctx_s *psCtx)
{
    int nRet = -1;
    uint16 u16CompressLen = 0, u16WriteLen = 0;
    jotupix_packet_param_s sPacket;

    if (jotupix_lzss_complete(&psCtx->m_sSendPro.m_sLzssCtx))
    {
        psCtx->m_sSendPro.m_bSendStatus = FALSE;
        JTLogD(TAG, "Send program success.\r\n");
        
        return 0;
    }

    jotupix_packet_start(&psCtx->m_sSendPro.m_sPktCtx);

    while (u16WriteLen < PACKET_PAYLOAD_SIZE)
    {  
        if (PRO_BUFFER_SIZE > PACKET_PAYLOAD_SIZE - u16WriteLen)
        {
            u16CompressLen = PACKET_PAYLOAD_SIZE - u16WriteLen;
        }
        else
        {
            u16CompressLen = PRO_BUFFER_SIZE;
        }
    
        nRet = jotupix_lzss_next(&psCtx->m_sSendPro.m_sLzssCtx, psCtx->m_sSendPro.m_au8Buffer, u16CompressLen);
        if (nRet < 0)
        {
            JTLogE(TAG, "Send Program fail, lzss fail\r\n");
            psCtx->m_sSendPro.m_bSendStatus = FALSE;
            return nRet;
        }

        u16CompressLen = nRet;

        jotupix_packet_write(&psCtx->m_sSendPro.m_sPktCtx, psCtx->m_sSendPro.m_au8Buffer, u16CompressLen);

        u16WriteLen += u16CompressLen;

        if (jotupix_lzss_complete(&psCtx->m_sSendPro.m_sLzssCtx))
        {
//            if (u16WriteLen == 0)
//            {
//                s_sSendPro.m_bSendStatus = FALSE;
//                JTLogD(TAG, "Send program success.\r\n");
//                return 0;
//            }
//            else
//            {
//                break;
//            }
            break;
        }
    }

    sPacket.m_u8MsgType = E_ACTION_APP_SET_PRO;
    sPacket.m_u16PacketId = psCtx->m_sSendPro.m_u16PktId;
    sPacket.m_u32AllDataLen = 0; // Sending data via a data stream does not require a total data length.
    sPacket.m_u8PacketTransType = E_PACKET_TRANS_TYPE_FLAG;

    if (jotupix_lzss_complete(&psCtx->m_sSendPro.m_sLzssCtx))
    {
        sPacket.m_u8TransCompleteFlag = 1;
    }
    else
    {
        sPacket.m_u8TransCompleteFlag = 0;
    }

    JTLogD(TAG, "Send a packet data by compress, id=%d, len=%d.\r\n", psCtx->m_sSendPro.m_u16PktId, u16WriteLen);

    jotupix_packet_end(&psCtx->m_sSendPro.m_sPktCtx, &sPacket, &psCtx->m_sSendCtx);

    psCtx->m_sSendPro.m_u32TimeoutTick = time_get_next_tick(psCtx->m_u32CurrMsTick, SEND_PRO_TIMEOUT_TICK);

    return u16WriteLen;
}

static int iSendProgramNextPacketByUncompress(jotupix_ctx_s *psCtx)
{
    uint16 u16ReadLen = 0, u16WriteLen = 0;
    int nRet = -1;
    jotupix_packet_param_s sPacket;

    if (psCtx->m_sSendPro.m_psStream->m_sData.m_u32Size == psCtx->m_sSendPro.m_psStream->m_sData.m_u32Pos)
    {
        psCtx->m_sSendPro.m_bSendStatus = FALSE;
        JTLogD(TAG, "Send program success.\r\n");
        
        return 0;
    }

    jotupix_packet_start(&psCtx->m_sSendPro.m_sPktCtx);

    while (u16WriteLen < PACKET_PAYLOAD_SIZE)
    {
        if (PRO_BUFFER_SIZE > PACKET_PAYLOAD_SIZE - u16WriteLen)
        {
            u16ReadLen = PACKET_PAYLOAD_SIZE - u16WriteLen;
        }
        else
        {
            u16ReadLen = PRO_BUFFER_SIZE;
        }
    
        if (u16ReadLen > psCtx->m_sSendPro.m_psStream->m_sData.m_u32Size - psCtx->m_sSendPro.m_psStream->m_sData.m_u32Pos)
        {
            u16ReadLen = psCtx->m_sSendPro.m_psStream->m_sData.m_u32Size - psCtx->m_sSendPro.m_psStream->m_sData.m_u32Pos;
        }

        if (u16ReadLen == 0)
        {
            break;
        }
        
        nRet = psCtx->m_sSendPro.m_psStream->m_pfnRead(&psCtx->m_sSendPro.m_psStream->m_sData, psCtx->m_sSendPro.m_au8Buffer, u16ReadLen);
        if (nRet != u16ReadLen)
        {
            JTLogE(TAG, "Send Program fail, read data fail\r\n");
            psCtx->m_sSendPro.m_bSendStatus = FALSE;
            return -1;
        }

        jotupix_packet_write(&psCtx->m_sSendPro.m_sPktCtx, psCtx->m_sSendPro.m_au8Buffer, u16ReadLen);

        u16WriteLen += u16ReadLen;
    }

    JTLogD(TAG, "Send a packet data by uncompress, id=%d, len=%d.\r\n", psCtx->m_sSendPro.m_u16PktId, u16WriteLen);

    sPacket.m_u8MsgType = E_ACTION_APP_SET_PRO;
    sPacket.m_u16PacketId = psCtx->m_sSendPro.m_u16PktId;
    sPacket.m_u32AllDataLen = 0; // Sending data via a data stream does not require a total data length.
    sPacket.m_u8PacketTransType = E_PACKET_TRANS_TYPE_FLAG;

    if (psCtx->m_sSendPro.m_psStream->m_sData.m_u32Size > psCtx->m_sSendPro.m_psStream->m_sData.m_u32Pos)
    {
        sPacket.m_u8TransCompleteFlag = 0;
    }
    else
    {
        sPacket.m_u8TransCompleteFlag = 1;
    }
    
    jotupix_packet_end(&psCtx->m_sSendPro.m_sPktCtx, &sPacket, &psCtx->m_sSendCtx);

    psCtx->m_sSendPro.m_u32TimeoutTick = time_get_next_tick(psCtx->m_u32CurrMsTick, SEND_PRO_TIMEOUT_TICK);

    return u16WriteLen;
}

/**
    <0 fail
    =0 complete
    >0 send length
*/
static int iSendProgramNextPacket(jotupix_ctx_s *psCtx)
{
    int nRet = -1;

    // Calculate the percentage that has been sent
    uint8 u8Per = psCtx->m_sSendPro.m_psStream->m_sData.m_u32Pos * 100 / psCtx->m_sSendPro.m_psStream->m_sData.m_u32Size;
    
    if (psCtx->m_sSendPro.m_pfnCallback != NULL)
    {
        psCtx->m_sSendPro.m_pfnCallback(E_SEND_STATUS_PROGRESS, u8Per, psCtx->m_sSendPro.m_pvUserData);
    }

#if SUPPORT_COMPRESS
    if (psCtx->m_sSendPro.m_sProInfo.m_u8Compress == E_COMPRESS_FLAG_DO)
    {
        nRet = iSendProgramNextPacketByCompress(psCtx);
    }
    else
    {
        nRet = iSendProgramNextPacketByUncompress(psCtx);
    }
#else
    nRet = iSendProgramNextPacketByUncompress(psCtx);
#endif

    if (psCtx->m_sSendPro.m_pfnCallback != NULL)
    {
        if (nRet == 0)
        {
            psCtx->m_sSendPro.m_pfnCallback(E_SEND_STATUS_COMPLETED, 100, psCtx->m_sSendPro.m_pvUserData);
        }
        else if (nRet < 0)
        {
            psCtx->m_sSendPro.m_pfnCallback(E_SEND_STATUS_FAIL, 0, psCtx->m_sSendPro.m_pvUserData);
        }
    }

    return nRet;
}

// Retransmit from the beginning
static void iRetrySendProgram(jotupix_ctx_s *psCtx)
{
    int nRet = FAIL;

    psCtx->m_sSendPro.m_u16PktId = 0;

    nRet = jotupix_lzss_start(&psCtx->m_sSendPro.m_sLzssCtx, psCtx->m_sSendPro.m_psStream);
    if (nRet != SUCCESS)
    {
        JTLogE(TAG, "Retry send program, lzss start fail\r\n");
        return;
    }

    psCtx->m_sSendPro.m_psStream->m_pfnSeek(&psCtx->m_sSendPro.m_psStream->m_sData, 0);
    
    iSendProgramNextPacket(psCtx);
}

static void iParseComplete(const uint8 *pu8Data, uint32 u32Len, void *pvUserData)
{
    uint8 u8MsgType = 0;
    jotupix_ctx_s *psCtx = (jotupix_ctx_s *)pvUserData;

    iPrintLogArrToHex("parse: ", pu8Data, u32Len);

    u8MsgType = pu8Data[0];

    switch (u8MsgType)
    {
    case E_ACTION_APP_START_SET_PRO:
        if (psCtx->m_sSendPro.m_bSendStatus)
        {
            if (pu8Data[1] == 0) 
            {
                iSendProgramNextPacket(psCtx);
            }
            else if (pu8Data[1] == 1) // The screen has a copy of the program, so there's no need to send it repeatedly.
            {
                psCtx->m_sSendPro.m_bSendStatus = FALSE;
                JTLogD(TAG, "Send program success, The screen has a copy of the program, so there's no need to send it repeatedly.\r\n");

                if (psCtx->m_sSendPro.m_pfnCallback != NULL)
                {
                    psCtx->m_sSendPro.m_pfnCallback(E_SEND_STATUS_COMPLETED, 100, psCtx->m_sSendPro.m_pvUserData);
                }
            }
            else
            {
                psCtx->m_sSendPro.m_bSendStatus = FALSE;
                JTLogE(TAG, "Send program start fail.\r\n");

                if (psCtx->m_sSendPro.m_pfnCallback != NULL)
                {
                    psCtx->m_sSendPro.m_pfnCallback(E_SEND_STATUS_FAIL, 0, psCtx->m_sSendPro.m_pvUserData);
                }
            }
        }
        break;
    case E_ACTION_APP_SET_PRO:
        if (psCtx->m_sSendPro.m_bSendStatus)  // The previous packet failed to be sent.
        {
            uint8 u8RetCode = pu8Data[4];
        
            if (u8RetCode != 0)
            {
                psCtx->m_sSendPro.m_bSendStatus = FALSE;
                JTLogE(TAG, "Send program packet fail. packet id=%d, retCode=%d\r\n", psCtx->m_sSendPro.m_u16PktId, u8RetCode);

                psCtx->m_sSendPro.m_u8RetryCnt++;
                if (psCtx->m_sSendPro.m_u8RetryCnt > RETRY_MAX_CNT)
                {
                    psCtx->m_sSendPro.m_u8RetryCnt = 0;
                    if (psCtx->m_sSendPro.m_pfnCallback != NULL)
                    {
                        psCtx->m_sSendPro.m_pfnCallback(E_SEND_STATUS_FAIL, 0, psCtx->m_sSendPro.m_pvUserData);
                    }
                    break;  
                }
                
                iRetrySendProgram(psCtx);
            }
            else
            {
                psCtx->m_sSendPro.m_u16PktId++;
                iSendProgramNextPacket(psCtx);
            }
        }
        break;
    case E_ACTION_APP_SET_SWITCH_STATUS:
        JTLogD(TAG, "Set switch status success, current status=%d\r\n", pu8Data[1]);
        break;
    case E_ACTION_APP_SET_FLIP:
        JTLogD(TAG, "Set flip success, current flip=%d\r\n", pu8Data[1]);
        break;
    case E_ACTION_APP_SET_BN:
        JTLogD(TAG, "Set brightness success, current bn=%d\r\n", pu8Data[1]);
        break;
    case E_ACTION_MCU_TO_BLE_SET_SCREEN_PARAM:
        {
            psCtx->m_sDevInfo.m_u16DevId = ((uint16)pu8Data[1] << 8);
            psCtx->m_sDevInfo.m_u16DevId |= ((uint16)pu8Data[2] << 0);

            // The screen height was represented by only one byte in the message.
            psCtx->m_sDevInfo.m_u16DevHeight = pu8Data[3];

            psCtx->m_sDevInfo.m_u16DevWidth = ((uint16)pu8Data[4] << 8);
            psCtx->m_sDevInfo.m_u16DevWidth |= ((uint16)pu8Data[5] << 0);
            psCtx->m_sDevInfo.m_u8DevType = pu8Data[6];

            // The version number in this message is represented by only one byte.
            psCtx->m_sDevInfo.m_u16Version = pu8Data[7];
        }
        break;
    case E_ACTION_MCU_TO_BLE_SET_NAME:
        {
            uint8 u8NameLen = u32Len - 1;

            if (u8NameLen > JOTUPIX_MAX_NAME_LEN)
            {
                u8NameLen = JOTUPIX_MAX_NAME_LEN;
            }

            memcpy(psCtx->m_sDevInfo.m_au8DevName, pu8Data+1, u8NameLen);
        }
        break;
    case E_ACTION_MCU_TO_BLE_STARTUP_OPR: // Some products require this command to be parsed before the screen will activate.
        if (pu8Data[1] == 0x01)
        {
            JTLogD(TAG, "The screen has started up and will automatically obtain device information.\r\n");
            jotupix_get_dev_info(psCtx, NULL);
        }
        else if (pu8Data[1] == 0x04)  // Power-on message on screen
        {
            JTLogD(TAG, "Received power-on message from the screen\r\n");

            JTLogD(TAG, "Startup screen\r\n");
            jotupix_send_startup_screen(psCtx);
        }
        break;
    case E_ACTION_APP_GET_DEV_INFO:
        {
            psCtx->m_bGetDevInfo = TRUE;

//            memset(&psCtx->m_sDevInfo, 0, sizeof(jotupix_dev_info_s));

            psCtx->m_sDevInfo.m_u8SwitchStatus = pu8Data[1];
            psCtx->m_sDevInfo.m_u8Bn = pu8Data[2];
            psCtx->m_sDevInfo.m_u8Flip = pu8Data[3];
            psCtx->m_sDevInfo.m_u8SupportLocalMicFlag = pu8Data[4];
            psCtx->m_sDevInfo.m_u8LocalMicStatus = pu8Data[5];
            psCtx->m_sDevInfo.m_u8LocalMicMode = pu8Data[6];
            psCtx->m_sDevInfo.m_u8EnableShowId = pu8Data[7];
            psCtx->m_sDevInfo.m_u8ProMaxNum = pu8Data[8];
            psCtx->m_sDevInfo.m_u8EnableRemote = pu8Data[9];
            psCtx->m_sDevInfo.m_u8TimerMaxNum = pu8Data[10];
            psCtx->m_sDevInfo.m_u8DevType = pu8Data[11];

            psCtx->m_sDevInfo.m_u32ProjectCode = ((uint32)pu8Data[12] << 24);
            psCtx->m_sDevInfo.m_u32ProjectCode |= ((uint32)pu8Data[13] << 16);
            psCtx->m_sDevInfo.m_u32ProjectCode |= ((uint32)pu8Data[14] << 8);
            psCtx->m_sDevInfo.m_u32ProjectCode |= ((uint32)pu8Data[15] << 0);

            psCtx->m_sDevInfo.m_u16Version = ((uint16)pu8Data[16] << 8);
            psCtx->m_sDevInfo.m_u16Version |= ((uint16)pu8Data[17] << 0);

            if (u32Len > 18)
            {
                psCtx->m_sDevInfo.m_u8DevleoperFlag = pu8Data[18];
            }
            else
            {
                psCtx->m_sDevInfo.m_u8DevleoperFlag = 0;
            }

            if (u32Len > 20)
            {
                psCtx->m_sDevInfo.m_u16PktMaxSize = ((uint16)pu8Data[19] << 8);
                psCtx->m_sDevInfo.m_u16PktMaxSize |= ((uint16)pu8Data[20] << 0);
            }
            else
            {
                psCtx->m_sDevInfo.m_u16PktMaxSize = DEFAULT_PKT_MAX_SIZE;  // default;
            }

            // Compatibility handling ensures the new version is compatible with subsequent devices.
            if (u32Len > 22)
            {
                psCtx->m_sDevInfo.m_u16DevId = ((uint16)pu8Data[21] << 8);
                psCtx->m_sDevInfo.m_u16DevId |= ((uint16)pu8Data[22] << 0);
            }

            if (u32Len > 24)
            {
                psCtx->m_sDevInfo.m_u16DevWidth = ((uint16)pu8Data[23] << 8);
                psCtx->m_sDevInfo.m_u16DevWidth |= ((uint16)pu8Data[24] << 0);
            }

            if (u32Len > 26)
            {
                psCtx->m_sDevInfo.m_u16DevHeight = ((uint16)pu8Data[25] << 8);
                psCtx->m_sDevInfo.m_u16DevHeight |= ((uint16)pu8Data[26] << 0);
            }

            if (u32Len > 27)
            {
                uint8 u8NameLen = pu8Data[27];

                if (u8NameLen > JOTUPIX_MAX_NAME_LEN)
                {
                    u8NameLen = JOTUPIX_MAX_NAME_LEN;
                }

                memset(psCtx->m_sDevInfo.m_au8DevName, 0, JOTUPIX_MAX_NAME_LEN);
                memcpy(psCtx->m_sDevInfo.m_au8DevName, pu8Data+28, u8NameLen);
            }

            JTPrint("Device info: \r\n");
            JTPrint("-name: %s\r\n", psCtx->m_sDevInfo.m_au8DevName);
            JTPrint("-id: %04X\r\n", psCtx->m_sDevInfo.m_u16DevId);
            JTPrint("-height,width: %d,%d\r\n", psCtx->m_sDevInfo.m_u16DevHeight, psCtx->m_sDevInfo.m_u16DevWidth);
            JTPrint("-SwitchStatus: %d\r\n", psCtx->m_sDevInfo.m_u8SwitchStatus);
            JTPrint("-Brightness: %d\r\n", psCtx->m_sDevInfo.m_u8Bn);
            JTPrint("-ScreenFlip: %d\r\n", psCtx->m_sDevInfo.m_u8Flip);
            JTPrint("-DevType: %d\r\n", psCtx->m_sDevInfo.m_u8DevType);
            JTPrint("-Version: %d\r\n", psCtx->m_sDevInfo.m_u16Version);
            JTPrint("-PktMaxSize: %d\r\n", psCtx->m_sDevInfo.m_u16PktMaxSize);
        
            if (psCtx->m_pfnGetDevInfoCallback != NULL)
            {
                psCtx->m_pfnGetDevInfoCallback(&psCtx->m_sDevInfo);
                psCtx->m_pfnGetDevInfoCallback = NULL;
            }
        }
        break;
    default:
        // Other messages, please ignore.
        break;
    }
}


static void iPrintLogEmpty(const char* fmt, ...)
{
    //
}

static void iPrintLogArrToHex(const uint8 *pu8Title, const uint8 *pu8Data, uint32 u32Len)
{
    uint32 i = 0;

    if (pu8Title != NULL)
    {
        JTPrint(pu8Title);;
    }

    for (i=0; i<u32Len; i++)
    {
        JTPrint("%02x ", pu8Data[i]);
    }

    JTPrint("\r\n");
}

