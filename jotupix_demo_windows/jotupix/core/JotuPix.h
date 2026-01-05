#pragma once

#include <vector>
#include <memory>
#include <string>
#include <thread>
#include <atomic>
#include <chrono>
#include "JDefine.h"
#include "JProtocol.h"
#include "JLzss.h"
#include "JPacket.h"
#include "JTick.h"
#include "JInfo.h"

/**
 * @brief Callback interface for sending program transmission status.
 *
 * This interface is implemented by the caller to receive progress updates,
 * completion notifications, or failure results during program transmission.
 */
class IJSendProgramCallback
{
public:
	enum class SendStatus
	{
		COMPLETED = 0,  ///< Transmission completed successfully.
		PROGRESS,  ///< Transmission is in progress.
		FAIL,   ///< Transmission failed.
	};

	/**
	 * @brief Called when program transmission status changes.
	 *
	 * @param eStatus  Current transmission status.
	 * @param u8Percent Transmission progress percentage (0¨C100).
	 */
	virtual	void onEvent(SendStatus eStatus, uint8_t u8Percent) = 0;
};

class JProgramGroupBase
{
public:
	enum class GroupType
	{
		NOR = 0,
	};

	virtual ~JProgramGroupBase() {}

	GroupType GetGroupType() 
	{
		return m_eGroupType;
	}

protected:
	GroupType m_eGroupType;
};

/**
 * @brief Callback interface for receiving device information.
 */
class IJGetDevInfoCallback
{
public:
	/**
	 * @brief Called when device information is retrieved.
	 *
	 * @param devInfo Device information object.
	 */
	virtual	void onEvent(JInfo devInfo) = 0;
};

class JProgramGroupNor : public JProgramGroupBase
{
public:
	enum class PlayType
	{
		PlAY_TYPE_CNT = 0, // Play by number of times
		PlAY_TYPE_DUR,  // Play by duration
		PlAY_TYPE_TIME, // Play by time
	};

	JProgramGroupNor()
	{
		m_eGroupType = GroupType::NOR;
	}

	PlayType m_ePlayType = PlayType::PlAY_TYPE_CNT;
	uint32_t m_u32PlayParam = 1;
};

/**
 * @brief Main class providing communication, protocol handling,
 *        program transmission, and device interaction.
 *
 * The JotuPix class encapsulates protocol parsing, LZSS compression,
 * packet sending, program sending state machine, and device commands.
 * It also manages callbacks for program transmission and device info retrieval.
 */
class JotuPix : public IJParseCallback
{
public:
	//enum class Status
	//{
	//	OFF = 0,
	//	ON = 1,
	//};

	// Commands executable
	enum class Action
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

		E_ACTION_MCU_TO_BLE_STARTUP_OPR = 0x32,             // start up operation (32)
	};

	enum class CompressFlag
	{
		COMPRESS_FLAG_DO = 0, // Compress
		COMPRESS_FLAG_UNDO  // No compression is required.
	};

	struct ProgramInfo
	{
		uint8_t m_u8ProIndex;
		uint8_t m_u8ProAllNum;
		CompressFlag m_eCompress;  // Compress or not?
		std::shared_ptr<JProgramGroupBase> m_proGroupParam;
	};

	struct ProgramSender
	{
		bool m_bSendStatus;
		IJSendProgramCallback *m_pfnCallback;
		uint16_t m_u16PktId;
		uint32_t m_u32TimeoutTick;
		uint8_t m_u8RetryCnt;
		uint16_t m_u16CurrPktPayloadLen;

		ProgramInfo m_sProgramInfo;
		
		uint32_t m_u32CurrSendLen;
		std::vector<uint8_t> m_au8ProData;

		uint32_t m_u32ProOrigCrc;
		uint32_t m_u32ProOrigLen;
	};

	~JotuPix();

	/**
	 * @brief Initialize the JotuPix module.
	 *
	 * @param pfnSender Interface used to send raw data through
	 *                  the underlying communication layer.
	 */
	void Init(IJSend *pfnSender);

	/**
	 * @brief Periodic tick function that must be called from the UI thread.
	 *
	 * Handles program sending timeouts, retries, and internal state updates.
	 *
	 * @param u32CurrTick Current system tick in milliseconds.
	 */
	void Tick(uint32_t u32CurrTick);

	/**
	 * @brief Provide received raw data to the protocol parser.
	 *
	 * @param pu8Data Pointer to received data.
	 * @param u32Len Length of received data.
	 */
	void ParseRecvData(const uint8_t* pu8Data, uint32_t u32Len);

	/**
	 * @brief Send a command to the device.
	 *
	 * @param pu8Data Command payload buffer.
	 * @param u32Len Length of payload.
	 * @return 0 on success, negative on error.
	 */
	int SendCommand(const uint8_t* pu8Data, uint32_t u32Len);

	/**
	 * @brief Start sending a program to the device.
	 *
	 * @param psProInfo Program settings and metadata.
	 * @param pu8ProData Raw program binary data.
	 * @param u32ProDataLen Length of the binary program data.
	 * @param pfnCallback Callback to receive send progress updates.
	 * @return 0 on success, negative on error.
	 */
	int SendProgram(ProgramInfo* psProInfo, const uint8_t* pu8ProData, uint32_t u32ProDataLen, IJSendProgramCallback* pfnCallback);
	
	/**
	 * @brief Cancel an ongoing program transmission.
	 *
	 * @return 0 on success, negative on error.
	 */
	int CancelSendProgram();

	/// Send simple commands
	int SendSwitchStatus(uint8_t u8Status);
	int SendBrightness(uint8_t u8Bn);
	int SendScreenFlip(uint8_t u8Flip);
	int SendReset();

	/**
	 * @brief Request device information.
	 *
	 * @param pfnCallback Callback receiving the device info.
	 * @return 0 on success, negative on error.
	 */
	int GetDevInfo(IJGetDevInfoCallback *pfnCallback);

	/**
	 * @brief Send the startup screen command (required in serial mode).
	 */
	int SendStartupScreen();

	void onParseComplete(const uint8_t* pu8Data, uint32_t u32Len) override;

private:
	JLzss m_sLzss;
	JProtocol m_sProtocol;
	JPacket m_sPacket;

	ProgramSender m_sProSender;
	uint32_t m_u32CurrMsTick = 0;
	IJGetDevInfoCallback* m_pfnGetDevInfoCallback = nullptr;

	int SendProgramStart();
	int iSendProgramNextPacket();
	void iRetrySendProgram();
	void PrintLogArrToHex(const char* pu8Title, const uint8_t* pu8Data, uint32_t u32Len);
};

