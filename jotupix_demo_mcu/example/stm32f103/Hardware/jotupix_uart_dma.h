/**
 * @file    jotupix_uart_dma.h
 * @brief   
 * @note
 *    1. TX：PA9；RX：PA10
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */
#ifndef __JOTUPIX_UART_DMA_H__
#define __JOTUPIX_UART_DMA_H__
#include "jotupix_typedef.h"

#ifndef JOTUPIX_UART_DMA_BAUD
#define JOTUPIX_UART_DMA_BAUD     400000 // The default serial port baud rate
#endif

#define JOTUPIX_UART_DMA_MAX_BAUDRATE  7500000   // The maximum baud rate supported by the serial port
#define JOTUPIX_UART_DMA_MIN_BAUDRATE  1000      // The minimum baud rate supported by the serial port

/**
 * @brief Serial port data reception callback function.
 *
 */
typedef void (*jotupix_uart_dma_recbytecallback_f)(const uint8 *pu8Data, uint32 u32Len);

/**
 * @brief Uart DMA init.
 *
 */
void jotupix_uart_dma_init(uint8 *pu8RxDataBuff, uint32 u32RxLen, uint8 *pu8TxDataBuff, uint32 u32TxLen, jotupix_uart_dma_recbytecallback_f pfnCallback);

/**
 * @brief Uart DMA Reverse initialization.
 *
 */
void jotupix_uart_dma_reinit(void);

/**
 * @brief Send a byte via the serial port
 *
 * @param[in] u8Byte The bytes that need to be sent
 *
 * @return
 */
void jotupix_uart_dma_sendbyte(uint8 u8Byte);

/**
 * @brief Send a group of data via the serial port
 *
 * @param[in] pu8Data The data buffer that needs to be sent
 * @param[in] u32Len  data length
 *
 * @return
 */
void jotupix_uart_dma_sendbuff(const uint8* pu8Data, uint32 u32Len);

/**
 * @brief The main loop of the serial port module must be repeatedly called within the main function.
 * 
 * @return
 */
void jotupix_uart_dma_mainloop(void);

/**
 * @brief To modify the serial port baud rate, it is necessary to call this operation 
 *        after the serial port initialization is completed in order for the change to take effect.
 *
 * @param[in] u32Baudrate The baud rate that needs to be set for the serial port
 *
 * @return
 */
void jotupix_uart_dma_setbaudrate(uint32 u32Baudrate);
#endif

