/**
 * @file    main.c
 * @brief   
 * @note
 *
 * @copyright
 *  Copyright (c) 2025 JotuPix Technology.
 *  All rights reserved.
 */
#include "stm32f10x.h"                 
#include "jotupix.h"
#include "jotupix_typedef.h"
#include "jotupix_uart_dma.h"
#include "jotupix_demo.h"
#include "jotupix_tick.h"

#define JOTUPIX_UART_DMA_BAUD 	400000 	// Serial port DMA baud rate
#define UART_DMA_RX_BUFFER_LEN 	256 	// Serial port DMA data receiving buffer size
#define UART_DMA_TX_BUFFER_LEN 	1024 	// Serial port DMA data send buffer size，This value cannot be less than PROTOCOL_TX_BUFF_SIZE.
#define JOTUPIX_MAX_PRO_NUM 	6

typedef struct _jotupix_program_param_s
{
    const uint8 *m_pu8ProData;
    uint32 m_u32DataLen;
}jotupix_program_param_s;

static jotupix_ctx_s s_sCtx = {0};
static jotupix_program_info_s s_sProInfo = {0}; 
static jotupix_stream_s s_sStream = {0};

static uint8 s_au8UartRecBuff[UART_DMA_RX_BUFFER_LEN] = {0}; 
static uint8 s_au8UartSendBuff[UART_DMA_TX_BUFFER_LEN] = {0}; 

static uint32 s_u32Systick = 0;  // System counter, the elapsed time since the system was powered on, in milliseconds

static uint32 s_u32NormalTick = 0;
static uint8 s_u8ProIndex = 0;

static const jotupix_program_param_s s_au8ProBuff[JOTUPIX_MAX_PRO_NUM] = {
{GRAFFITI, sizeof(GRAFFITI)},
{ANIMATION, sizeof(ANIMATION)},
{BORDER, sizeof(BORDER)},
{FULLCOLOR, sizeof(FULLCOLOR)},
{DIYCOLOR, sizeof(DIYCOLOR)},
{GIF, sizeof(GIF)},
};
	
static void iUartDmaRecByteCallback(const uint8 *pu8Data, uint32 u32Len);
static void iSendProgramCallback(JOTUPIX_SEND_STATUS_E eStatus, uint8 u8Percent, void *pvUserData);
static int iStreamRead(jotupix_data_s *psData, uint8 *pu8Buff, uint32 u32Len);
static int iStreamSeek(jotupix_data_s *psData, uint32 u32Pos);
static int iUartSendBuff(const uint8* pu8Data, uint32 u32Len, void *pvUserData);
static void iSystick_Init(void);
static void iSendDemoPro(const uint8 *pu8Data, uint32 u32Len);

/**
 * @brief main function
 *
 */
int main(void)
{
	iSystick_Init();  // sys_tick init
	jotupix_uart_dma_init(s_au8UartRecBuff, sizeof(s_au8UartRecBuff), s_au8UartSendBuff, sizeof(s_au8UartSendBuff), iUartDmaRecByteCallback);
	jotupix_uart_dma_setbaudrate(JOTUPIX_UART_DMA_BAUD); 
	jotupix_init(&s_sCtx, iUartSendBuff, NULL);
	
	// Instruction example：
	//jotupix_send_switch_status(&s_sCtx, ON);	// Turn on or off the display screen
	//jotupix_send_screen_flip(&s_sCtx, 0);		// The display screen sends the display effect command: 0: No inversion, 1: XY inversion, 2: X inversion, 3: Y inversion
	//jotupix_send_brightness(&s_sCtx, 255);	// Set the display screen brightness, from 0 to 25
	//jotupix_send_reset(&s_sCtx); 				// Send the reset command

	/*When the display screen shows the ID number and logo, no program can be sent. 
	If the display screen and the controller are powered on simultaneously,
	the program can only be sent after a 5-second delay.*/	
	
	s_u32NormalTick = time_get_next_tick(s_u32Systick, 5000); // Set for 5 seconds

	while (1)
	{
		jotupix_uart_dma_mainloop();
		jotupix_thread(&s_sCtx, s_u32Systick);
		
		if (time_after_eq(s_u32Systick, s_u32NormalTick))
		{
			iSendDemoPro(s_au8ProBuff[s_u8ProIndex].m_pu8ProData, s_au8ProBuff[s_u8ProIndex].m_u32DataLen);

			s_u8ProIndex++;
			if (s_u8ProIndex >= JOTUPIX_MAX_PRO_NUM)
			{
				s_u8ProIndex = 0;
			}
			
			s_u32NormalTick = time_get_next_tick(s_u32Systick, 5000); // Set for 5 seconds
		}
	}
}

/**
 * @brief Send an example program
 *
 */
static void iSendDemoPro(const uint8 *pu8Data, uint32 u32Len)
{
	s_sStream.m_sData.m_pu8Data = pu8Data;
	s_sStream.m_sData.m_u32Size = u32Len;
	s_sStream.m_sData.m_u32Pos = 0;
	s_sStream.m_pfnRead = iStreamRead;
	s_sStream.m_pfnSeek = iStreamSeek;

	jotupix_crc_reset();
	s_sProInfo.m_u32ProCrc = jotupix_crc_calculate(pu8Data, u32Len);
	s_sProInfo.m_u32ProLen = u32Len;
	s_sProInfo.m_u8ProIndex = 0; 		// The current position of the program in the list of programs to be sent this time, with the value ranging from 0 to n-1.
	s_sProInfo.m_u8ProAllNum = 1; 		// How many programs will be sent in total? Range of values: 1 to n
	
	s_sProInfo.m_u8Compress = E_COMPRESS_FLAG_UNDO; // Compression flag: 0 - This program has been compressed 1 - This program has not been compressed
	s_sProInfo.m_u8ProGroupType = E_PROGRAM_GROUP_TYPE_NOR; 	// The program group category currently only supports general programs, and is fixed at 0.
	s_sProInfo.m_unProGoupParam.m_sProGroupNor.m_u8PlayType = E_PROGRAM_PlAY_TYPE_CNT; // Playback mode: 0x00: Play by number 0x01: Play by duration 0x02: Play by time
	s_sProInfo.m_unProGoupParam.m_sProGroupNor.m_u32PlayParam = 1; // When the playback mode is 0x00, it indicates the number of times. When the playback mode is 0x01, it represents the playback duration, measured in seconds. When the playback mode is 0x02, it indicates the playback time.
	
	jotupix_send_program(&s_sCtx, &s_sProInfo, &s_sStream, iSendProgramCallback, NULL);
}

/**
 * @brief Send program callback
 *
 * @param[in] eStatus JOTUPIX_SEND_STATUS_E
 * @param[in] u8Percent Sending progress percentage 0~100%
 *
 * @return
 */
static void iSendProgramCallback(JOTUPIX_SEND_STATUS_E eStatus, uint8 u8Percent, void *pvUserData)
{

}

/**
 * @brief The actual data transmission function 
 *
 * @param[in] pu8Data data buffer
 * @param[in] u32Len  data length
 *
 * @return 0 on success, other on failure.
 */
static int iUartSendBuff(const uint8* pu8Data, uint32 u32Len, void *pvUserData)
{
	jotupix_uart_dma_sendbuff(pu8Data, u32Len);
	return 0;
}

/**
 * @brief Data reading interface
 *
 * @param[in] psData data object
 * @param[in,out] pu8Buff read data buffer
 * @param[in] u32Len  read length
 *
 * @return read data length
 */
static int iStreamRead(jotupix_data_s *psData, uint8 *pu8Buff, uint32 u32Len)
{
    if (psData == NULL || pu8Buff == NULL)
    {
        return 0;
    }

    uint32 u32Remain = psData->m_u32Size - psData->m_u32Pos;
    uint32 u32ReadLen = (u32Len < u32Remain) ? u32Len : u32Remain;

    if (u32ReadLen > 0)
    {
        memcpy(pu8Buff, psData->m_pu8Data + psData->m_u32Pos, u32ReadLen);
        psData->m_u32Pos += u32ReadLen;
    }

    return u32ReadLen;
}

/**
 * @brief Set the read position
 *
 * @param[in] psData data object
 * @param[in] u32Pos  position
 *
 * @return seek postion
 */
static int iStreamSeek(jotupix_data_s *psData, uint32 u32Pos)
{
    if (psData == NULL)
    {
        return -1;
    }

    if (u32Pos > psData->m_u32Size)
    {
        u32Pos = psData->m_u32Size;
    }

    psData->m_u32Pos = u32Pos;

    return (int)psData->m_u32Pos;
}

/**
 * @brief UART DMA receive data callback function
 *
 * @param[in] pu8Data receive data buffer
 * @param[in] u32Len  Received data byte length
 *
 * @return
 */
static void iUartDmaRecByteCallback(const uint8 *pu8Data, uint32 u32Len)
{
	jotupix_parse_recv_data(&s_sCtx, pu8Data, u32Len);
}


/**
 * @brief Init systick
 *
 */
void iSystick_Init(void)
{ 
    SystemCoreClockUpdate();
    SysTick_Config(SystemCoreClock / 1000);	// Interrupt every 1 millisecond
    s_u32Systick = 0;
}

/**
 * @brief System interruption function
 *
 */
void SysTick_Handler(void)
{
    s_u32Systick++;
}

