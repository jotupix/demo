/**
 * @file    jotupix_uart_dma.h
 * @brief   
 * @note
 *    1. TX：PA9；RX：PA10
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */
#include "jotupix_uart_dma.h"
#include "stm32f10x.h"                  // Device header

typedef struct _jotupix_rx_seg_s
{
    uint8 *m_pu8Buf;
    uint16 m_u16Len;
} jotupix_rx_seg_s;

typedef struct _jotupix_rx_batch_s
{
    jotupix_rx_seg_s m_ausSeg[2];
    uint8 m_u8SegCnt;
} jotupix_rx_batch_s;

static uint16 s_u16Wp = 0;
static uint16 s_u16Rp = 0;
static jotupix_rx_batch_s s_sRxBatch;
static uint8 s_bRxReady = FALSE;
static BOOL s_bSendFlag = FALSE;   
static jotupix_uart_dma_recbytecallback_f s_pfnRecCallback = NULL;
static uint8 *s_pu8RxDataBuffer = NULL;
static uint8 *s_pu8TxDataBuffer = NULL;
static uint32 s_u32RxBufferLen = 0;
static uint32 s_u32TxBufferLen = 0;
static uint16 s_u16Len = 0;
static const uint8 *s_pu8Buf = NULL;
static jotupix_rx_batch_s s_sBatch; 

static void iInitUart(void)
{
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_USART1, ENABLE);

    USART_InitTypeDef USART_InitStructure;
    USART_StructInit(&USART_InitStructure);
    USART_InitStructure.USART_BaudRate = JOTUPIX_UART_DMA_BAUD;
    USART_InitStructure.USART_WordLength = USART_WordLength_8b;
    USART_InitStructure.USART_StopBits = USART_StopBits_1;
    USART_InitStructure.USART_Parity = USART_Parity_No;
    USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;
    USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx ;
    USART_Init(USART1, &USART_InitStructure);

    /* Enable USART1 DMA Rx request */
    USART_DMACmd(USART1, USART_DMAReq_Rx, ENABLE); 
    USART_ITConfig(USART1, USART_IT_IDLE, ENABLE);  

    /* Enable the USART1 */
    USART_Cmd(USART1, ENABLE);
}

static void iInitGpio(void)
{
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA, ENABLE);

    GPIO_InitTypeDef GPIO_InitStructure;
		
    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_9;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_Init(GPIOA, &GPIO_InitStructure);

    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_10;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN_FLOATING;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_Init(GPIOA, &GPIO_InitStructure);
}

static void iInitDma(void)
{
    RCC_AHBPeriphClockCmd(RCC_AHBPeriph_DMA1,ENABLE);

    DMA_InitTypeDef DMA_InitStructure;

    /* USART1 TX DMA1 Channel (triggered by USART1 Tx event) Config */
    DMA_DeInit(DMA1_Channel4);
    DMA_StructInit(&DMA_InitStructure);
    DMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t)&USART1->DR;
    DMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t)s_pu8TxDataBuffer;
    DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralDST;
    DMA_InitStructure.DMA_BufferSize = s_u32TxBufferLen;
    DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;
    DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;
    DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_Byte;
    DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_Byte;
    DMA_InitStructure.DMA_Mode = DMA_Mode_Normal;
    DMA_InitStructure.DMA_Priority = DMA_Priority_High;
    DMA_InitStructure.DMA_M2M = DMA_M2M_Disable;
    DMA_Init(DMA1_Channel4, &DMA_InitStructure);
    DMA_ClearFlag(DMA1_FLAG_TC4);

    /* Enable USART1 TX DMA1 Channel */
    DMA_Cmd(DMA1_Channel4, ENABLE);

    /* USART1 RX DMA1 Channel (triggered by USART1 Rx event) Config */
    DMA_DeInit(DMA1_Channel5);
    DMA_StructInit(&DMA_InitStructure);
    DMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t)&USART1->DR;
    DMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t)s_pu8RxDataBuffer;
    DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralSRC;
    DMA_InitStructure.DMA_BufferSize = s_u32RxBufferLen;
    DMA_InitStructure.DMA_PeripheralInc=DMA_PeripheralInc_Disable;
    DMA_InitStructure.DMA_MemoryInc=DMA_MemoryInc_Enable;
    DMA_InitStructure.DMA_PeripheralDataSize=DMA_PeripheralDataSize_Byte;
    DMA_InitStructure.DMA_MemoryDataSize=DMA_MemoryDataSize_Byte;
    DMA_InitStructure.DMA_Mode=DMA_Mode_Circular; // Configured in loop mode
    DMA_InitStructure.DMA_Priority=DMA_Priority_VeryHigh;
    DMA_InitStructure.DMA_M2M=DMA_M2M_Disable;
    DMA_Init(DMA1_Channel5, &DMA_InitStructure);

    /* Enable USART1 RX DMA1 Channel */
    DMA_Cmd(DMA1_Channel5, ENABLE);
}

static void iInitNvic(void)
{
    NVIC_InitTypeDef NVIC_InitStructure;
    NVIC_PriorityGroupConfig(NVIC_PriorityGroup_1);

    NVIC_InitStructure.NVIC_IRQChannel = USART1_IRQn;
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
    NVIC_Init(&NVIC_InitStructure);
}

void jotupix_uart_dma_init(uint8 *pu8RxDataBuff, uint32 u32RxLen, uint8 *pu8TxDataBuff, uint32 u32TxLen, jotupix_uart_dma_recbytecallback_f pfnCallback)
{
    s_bSendFlag = FALSE;
	s_pu8RxDataBuffer = pu8RxDataBuff;
	s_u32RxBufferLen = u32RxLen;
	s_pu8TxDataBuffer = pu8TxDataBuff;
	s_u32TxBufferLen = u32TxLen;
	s_pfnRecCallback = pfnCallback;
	
	iInitDma();
	iInitUart();
	iInitNvic();
	iInitGpio(); 
}

void jotupix_uart_dma_reinit(void)
{
}

void jotupix_uart_dma_sendbyte(uint8 u8Byte)
{
    USART1->DR = (((uint16)u8Byte) & ((uint16)0x01FF));
    while(!(USART1->SR & 0x80));
}

void jotupix_uart_dma_sendbuff(const uint8* pu8Data, uint32 u32Len)
{
//    uint8 u8Cnt = 200;  // Timeout counter for waiting

    if (pu8Data == NULL || u32Len == 0 || u32Len > s_u32TxBufferLen)
    {
        //JTDebug(ERROR_LEVEL, "jotupix_uart_dma_sendbuff error!\n");
        return;
    }

    // Waiting for the completion of the last data transmission
    while (s_bSendFlag && DMA_GetFlagStatus(DMA1_FLAG_TC4) == RESET)
    {
//        u8Cnt--;
//        if (u8Cnt == 0)
//        {
//            break;
//        }
    }

    memcpy(s_pu8TxDataBuffer, pu8Data, u32Len);

    DMA_Cmd(DMA1_Channel4, DISABLE);
    DMA_ClearFlag(DMA1_FLAG_TC4);
    DMA_SetCurrDataCounter(DMA1_Channel4, u32Len);
    DMA_Cmd(DMA1_Channel4, ENABLE);
    USART_DMACmd(USART1, USART_DMAReq_Tx, ENABLE); 
    s_bSendFlag = TRUE;
}

void jotupix_uart_dma_mainloop(void)
{
	if (s_bRxReady)
    {
        uint8 i = 0;

        __disable_irq();
        s_sBatch = s_sRxBatch;
        s_bRxReady = FALSE;
        __enable_irq();

        for (i = 0; i < s_sBatch.m_u8SegCnt; i++)
        {
            s_pfnRecCallback(s_sBatch.m_ausSeg[i].m_pu8Buf, s_sBatch.m_ausSeg[i].m_u16Len );
        }
    }
}

void USART1_IRQHandler()
{
    if(USART_GetITStatus(USART1, USART_IT_IDLE) != RESET) // When the serial port is idle, read the data to prevent the system from crashing.
    {  
        uint32 tmp = 0;

        // The idle flag can only be cleared by reading.
        tmp = USART1->SR;  
        tmp = USART1->DR; //Clear the USART_IT_IDLE flag

        s_u16Wp = s_u32RxBufferLen - DMA_GetCurrDataCounter(DMA1_Channel5);
		/* No new data*/
        if (s_u16Wp == s_u16Rp)
        {
            return;
        }

        s_sRxBatch.m_u8SegCnt = 0;

        /* Situation 1: Not looping back */
        if (s_u16Wp > s_u16Rp)
        {
            s_sRxBatch.m_ausSeg[0].m_pu8Buf = &s_pu8RxDataBuffer[s_u16Rp];
            s_sRxBatch.m_ausSeg[0].m_u16Len = s_u16Wp - s_u16Rp;
            s_sRxBatch.m_u8SegCnt = 1;
        }
        /* Situation 2: Occurrence of looping */
        else
        {
            /* first stage：[Rp ~ end) */
            s_sRxBatch.m_ausSeg[0].m_pu8Buf = &s_pu8RxDataBuffer[s_u16Rp];
            s_sRxBatch.m_ausSeg[0].m_u16Len = s_u32RxBufferLen - s_u16Rp;

            /* second stage：[0 ~ Wp) */
            if (s_u16Wp > 0)
            {
                s_sRxBatch.m_ausSeg[1].m_pu8Buf = &s_pu8RxDataBuffer[0];
                s_sRxBatch.m_ausSeg[1].m_u16Len = s_u16Wp;
                s_sRxBatch.m_u8SegCnt = 2;
            }
            else
            {
                s_sRxBatch.m_u8SegCnt = 1;
            }
        }

        s_u16Rp = s_u16Wp;
        s_bRxReady = TRUE; 
	}   
}

void jotupix_uart_dma_setbaudrate(uint32 u32Baudrate)
{
    if (u32Baudrate < JOTUPIX_UART_DMA_MIN_BAUDRATE || u32Baudrate > JOTUPIX_UART_DMA_MAX_BAUDRATE)
    {
        //JTDebug(ERROR_LEVEL, "jotupix_uart_dma_setbaudrate error!\n");
        return;
    }

    USART_InitTypeDef USART_InitStructure;
    USART_StructInit(&USART_InitStructure);
    USART_InitStructure.USART_BaudRate = u32Baudrate;
    USART_InitStructure.USART_WordLength = USART_WordLength_8b;
    USART_InitStructure.USART_StopBits = USART_StopBits_1;
    USART_InitStructure.USART_Parity = USART_Parity_No;
    USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None;
    USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx ;
    USART_Init(USART1, &USART_InitStructure);
}

