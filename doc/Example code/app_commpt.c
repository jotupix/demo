/*
* Copyright (c) 2024, Shenzhen Juntong Technology Co., Ltd.
* All rights reserved.
*
* File Name: app_commpt.c
* File Identifier:
* Summary:
*   Parse or send messages according to the communication protocol
* Current Version: V1.0
* Author: yangshun
* Completion Date: October 23, 2024
*/
#include "app_commpt.h"

#define APP_COMMPT_START_CHAR           0x01  // Start flag
#define APP_COMMPT_ESC_CHAR             0x02  // Escape flag
#define APP_COMMPT_ESC_XOR_CHAR         0x04  // Escape XOR value
#define APP_COMMPT_END_CHAR             0x03  // End flag
#define APP_COMMPT_MAX_LEN              (APS_PACKAGE_DATA_MAX_LEN+16)  // Maximum received data length
#define APP_COMMPT_PARSE_BUFF_LEN       APP_COMMPT_MAX_LEN            // Buffer length for parsing data
#define APP_COMMPT_TRANS_TX_BUFF_SIZE   200    // Size of the transmission buffer

typedef enum _APP_COMMPT_REC_STATUS_E
{
    E_APP_COMMPT_REC_STATUS_DATA = 0,     // Receiving data
    E_APP_COMMPT_REC_STATUS_START,        // Receiving start signal
    E_APP_COMMPT_REC_STATUS_DATALEN_H,    // Receiving data length (high byte)
    E_APP_COMMPT_REC_STATUS_DATALEN_L,    // Receiving data length (low byte)
} APP_COMMPT_REC_STATUS_E;

static APP_COMMPT_REC_STATUS_E s_eRecStatus = E_APP_COMMPT_REC_STATUS_START;
static uint16 s_u16RecIndex = 0;
static uint16 s_u16ParseDataLen = 0;
static uint8 s_au8ParseBuff[APP_COMMPT_PARSE_BUFF_LEN] = {0};
static BOOL s_bInEsc = FALSE;

static uint8 s_au8TransportTxBuffer[APP_COMMPT_TRANS_TX_BUFF_SIZE] = {0};  // Send buffer after being packaged according to the protocol

static APP_CommptParseCompleteCallback s_pfnParseCallback = NULL;
static APP_CommptTransportTx s_pfnTransportTx = NULL;

void APP_CommptInit(APP_CommptParseCompleteCallback pfnParseCallback, APP_CommptTransportTx pfnTransportTx)
{
    s_eRecStatus = E_APP_COMMPT_REC_STATUS_START;
    s_u16RecIndex = 0;
    s_u16ParseDataLen = 0;

    s_pfnParseCallback = pfnParseCallback;
    s_pfnTransportTx = pfnTransportTx;
}

void APP_CommptRecByte(uint8 u8Byte)
{
    switch (u8Byte)
    {
        case APP_COMMPT_START_CHAR:
            s_u16ParseDataLen = 0;
            s_bInEsc = FALSE;
            s_eRecStatus = E_APP_COMMPT_REC_STATUS_DATALEN_H;  // Length of data to be received
            break;
        case APP_COMMPT_ESC_CHAR:     //0x02
            s_bInEsc = TRUE;
            break;
        case APP_COMMPT_END_CHAR:    // End marker
            if (s_eRecStatus == E_APP_COMMPT_REC_STATUS_DATA
                && s_u16ParseDataLen <= APP_COMMPT_MAX_LEN
                && s_u16ParseDataLen > 0)
            {
                if (s_pfnParseCallback != NULL)
                {
                    s_pfnParseCallback(s_au8ParseBuff, s_u16ParseDataLen);                    
                }
            }

            s_eRecStatus = E_APP_COMMPT_REC_STATUS_START; // Waiting for start signal
            break;
        default:
            if(s_bInEsc)   //0x02
            {
                u8Byte ^= APP_COMMPT_ESC_XOR_CHAR; 
                s_bInEsc = FALSE;
            }

            switch(s_eRecStatus)
            {
                case E_APP_COMMPT_REC_STATUS_DATA:
                {
                    if (s_u16RecIndex < s_u16ParseDataLen)
                    {
                        s_au8ParseBuff[s_u16RecIndex] = u8Byte;
                        s_u16RecIndex++;
                    }
                }
                break;
                case E_APP_COMMPT_REC_STATUS_DATALEN_H:
                {
                    s_u16ParseDataLen = u8Byte << 8;
                    s_eRecStatus = E_APP_COMMPT_REC_STATUS_DATALEN_L;
                }
                break;
                case E_APP_COMMPT_REC_STATUS_DATALEN_L:
                {
                    s_u16ParseDataLen |= u8Byte;

                    if (s_u16ParseDataLen > APP_COMMPT_MAX_LEN || s_u16ParseDataLen == 0)
                    {
                        s_eRecStatus = E_APP_COMMPT_REC_STATUS_START;
                        s_u16ParseDataLen = 0;
                    }
                    else
                    {
                        s_eRecStatus = E_APP_COMMPT_REC_STATUS_DATA;
                        s_u16RecIndex = 0;
                    }
                }
                break;
                default:
                    break;
            }
            break;
    }
}

void APP_CommptSend(const uint8 *pu8Data, uint8 u8Len)
{
    uint8 i = 0;
    uint8 u8Index = 0;

    s_au8TransportTxBuffer[u8Index++] = (APP_COMMPT_START_CHAR);

    // Length of data sent
    s_au8TransportTxBuffer[u8Index++] = (0x00);
    if (u8Len < APP_COMMPT_ESC_XOR_CHAR)
    {
        s_au8TransportTxBuffer[u8Index++] = (APP_COMMPT_ESC_CHAR);
        s_au8TransportTxBuffer[u8Index++] = (APP_COMMPT_ESC_XOR_CHAR|u8Len);
    }
    else
    {
        s_au8TransportTxBuffer[u8Index++] = (u8Len);
    }

    // Send data
    for (i=0; i<u8Len; i++)
    {
        if (pu8Data[i] < APP_COMMPT_ESC_XOR_CHAR && pu8Data[i] != 0)
        {
            s_au8TransportTxBuffer[u8Index++] = (APP_COMMPT_ESC_CHAR);
            s_au8TransportTxBuffer[u8Index++] = (APP_COMMPT_ESC_XOR_CHAR|pu8Data[i]);
        }
        else
        {
            s_au8TransportTxBuffer[u8Index++] = (pu8Data[i]);
        }

        if (u8Index >= APP_COMMPT_TRANS_TX_BUFF_SIZE)
        {
            JTDebug(ERROR_LEVEL, "APP_CommptSend fail, data too long\n");
            return;
        }
    }

    s_au8TransportTxBuffer[u8Index++] = (APP_COMMPT_END_CHAR);

    if (s_pfnTransportTx != NULL)
    {
        s_pfnTransportTx(s_au8TransportTxBuffer, u8Index);
    }
}

