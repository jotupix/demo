/*
* Copyright (c) 2024, Shenzhen Juntong Technology Co., Ltd.
* All rights reserved.
*
* File Name: app_commpt.h
* File Identifier:
* Summary:
*   Parse or send messages according to the communication protocol
* Current Version: V1.0
* Author: yangshun
* Completion Date: October 23, 2024
*/
#ifndef __APP_COMMPT_H__
#define __APP_COMMPT_H__

#include "jt_typedef.h"
#include "app_config.h"

// Parsing completion callback
typedef void (*APP_CommptParseCompleteCallback)(const uint8 *pu8Data, uint16 u16Len);

// Raw data sending function
typedef void (*APP_CommptTransportTx)(const uint8 *pu8Data, uint8 u8Len);

/**
    @Function: Initialization
    @Param:
        pfnParseCallback[in]: Callback invoked when data parsing is completed
        pfnTransportTx[in]: Data sending function, registered by the application layer
    @Return:
    @Remark:
*/
void APP_CommptInit(APP_CommptParseCompleteCallback pfnParseCallback, APP_CommptTransportTx pfnTransportTx);

/**
    @Function: Receive one byte according to the protocol
    @Param:
        u8Byte[in]:
    @Return:
    @Remark: After a frame of data is fully parsed, the parse-complete callback will be invoked
*/
void APP_CommptRecByte(uint8 u8Byte);

/**
    @Function: Send the specified data according to the protocol
    @Param:
        pu8Data[in]: Data to be sent
        u8Len[in]: Length of the data to send
    @Return:
    @Remark:
*/
void APP_CommptSend(const uint8 *pu8Data, uint8 u8Len);


#endif // __APP_COMMPT_H__

