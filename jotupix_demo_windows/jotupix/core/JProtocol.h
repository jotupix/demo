#pragma once
#include "JDefine.h"

#define JPROTOCOL_RX_PARSE_BUFF_SIZE       256       // buff length of parsed data
#define JPROTOCOL_TX_BUFF_SIZE          512        // Send buffer size

/**
 * @brief Interface for receiving parse-complete callback events.
 *
 * Classes that need to handle parsing results should implement this interface.
 * When the parsing operation is finished, the parser will invoke the
 * `onParseComplete` method to deliver the parsed data.
 */
class IJParseCallback
{
public:
	/**
	 * @brief Called when the parsing process is completed.
	 *
	 * @param pu8Data Pointer to the parsed data buffer.
	 *               The buffer is valid only during the callback scope unless
	 *               explicitly documented otherwise.
	 *
	 * @param u32Len  Length of the parsed data buffer in bytes.
	 *
	 * @note Implementers should avoid performing time-consuming operations
	 *       inside this callback to prevent blocking the caller thread.
	 */
	virtual void onParseComplete(const uint8_t* pu8Data, uint32_t u32Len) = 0;
};

/**
 * @brief Interface for sending raw data.
 *
 * This interface should be implemented by any class that provides
 * a mechanism to transmit data (e.g., UART, TCP, BLE).
 */
class IJSend
{
public:
	/**
	 * @brief Send a block of data.
	 *
	 * @param pu8Data Pointer to the data buffer to send.
	 * @param u32Len Length of the data in bytes.
	 * @return int Returns 0 on success, negative values on error.
	 */
	virtual int Send(const uint8_t* pu8Data, uint32_t u32Len) = 0;
};

/**
 * @brief Communication protocol handler for sending and parsing framed data.
 *
 * The JProtocol class packages outgoing data and parses incoming data.
 * Users must provide a send interface (`IJSend`) and a callback interface
 * (`IJParseCallback`) to receive parsed frames.
 */
class JProtocol
{
public:
	/**
	 * @brief Initialize protocol with send handler and parse callback.
	 *
	 * @param pfnSender Pointer to an object that implements IJSend for data transmission.
	 * @param pfnCallback Pointer to an object that receives parsed data frames.
	 */
	void Init(IJSend *pfnSender, IJParseCallback *pfnCallback);

	/**
	 * @brief Send a data frame through the protocol.
	 *
	 * This method will wrap the raw data into protocol format before sending.
	 *
	 * @param pu8Data Pointer to raw data to be packed and sent.
	 * @param u32Len Length of the data in bytes.
	 * @return int Returns 0 on success, negative values on error.
	 */
	int Send(const uint8_t* pu8Data, uint32_t u32Len);

	/**
	 * @brief Parse incoming raw data.
	 *
	 * This function should be called whenever new bytes are received.
	 * It processes the protocol state machine and triggers the callback
	 * when a full frame is successfully parsed.
	 *
	 * @param pu8Data Pointer to received data.
	 * @param u32Len Number of received bytes.
	 */
	void Parse(const uint8_t* pu8Data, uint32_t u32Len);

private: // tx
	IJSend* m_pfnSender = nullptr;  ///< Handler for sending data.
	uint8_t m_au8TxBuffer[JPROTOCOL_TX_BUFF_SIZE]; ///< Transmission buffer.
	
private:  // rx
	IJParseCallback* m_pfnCallback = nullptr;  ///< Callback for parsed frames.
	uint8_t m_au8ParseBuffer[JPROTOCOL_RX_PARSE_BUFF_SIZE];  ///< Parsing buffer.
	uint8_t m_u8RecvStatus; ///< Receive state machine status.
	uint16_t m_u16RecvIndex;
	uint16_t m_u16ParseDataLen;
	bool m_bInEsc;
};

