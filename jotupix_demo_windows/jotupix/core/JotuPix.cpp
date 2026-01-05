//#include "pch.h"
#include <string.h>
#include "JotuPix.h"
#include "JLog.h"
#include "JByteWriter.h"
#include "JCrc.h"

#define TAG  "JotuPix"

#define SEND_PRO_TIMEOUT_TICK       (5*1000)  // ms
#define DEFAULT_PKT_MAX_SIZE        1024
#define RETRY_MAX_CNT               3  // Maximum number of retries
#define MAX_NAME_LEN        20

JotuPix::~JotuPix()
{
}

void JotuPix::Init(IJSend* pfnSender)
{
	m_sProtocol.Init(pfnSender, this);
}

void JotuPix::ParseRecvData(const uint8_t* pu8Data, uint32_t u32Len)
{
	m_sProtocol.Parse(pu8Data, u32Len);
}

void JotuPix::onParseComplete(const uint8_t* pu8Data, uint32_t u32Len)
{
    Action u8MsgType = Action::E_ACTION_UNKOWN;

    PrintLogArrToHex("parse: ", pu8Data, u32Len);

    u8MsgType =(Action) pu8Data[0];

    switch (u8MsgType)
    {
    case Action::E_ACTION_APP_START_SET_PRO:
        if (m_sProSender.m_bSendStatus)
        {
            if (pu8Data[1] == 0)
            {
                iSendProgramNextPacket();
            }
            else if (pu8Data[1] == 1) // The screen has a copy of the program, so there's no need to send it repeatedly.
            {
                m_sProSender.m_bSendStatus = false;
                JLogD(TAG, "Send program success, The screen has a copy of the program, so there's no need to send it repeatedly.\r\n");

                if (m_sProSender.m_pfnCallback != NULL)
                {
                    m_sProSender.m_pfnCallback->onEvent(IJSendProgramCallback::SendStatus::COMPLETED, 100);
                }
            }
            else
            {
                m_sProSender.m_bSendStatus = false;
                JLogE(TAG, "Send program start fail.\r\n");

                if (m_sProSender.m_pfnCallback != NULL)
                {
                    m_sProSender.m_pfnCallback->onEvent(IJSendProgramCallback::SendStatus::FAIL, 0);
                }
            }
        }
        break;
    case Action::E_ACTION_APP_SET_PRO:
        if (m_sProSender.m_bSendStatus)  // The previous packet failed to be sent.
        {
            uint8_t u8RetCode = pu8Data[4];

            if (u8RetCode != 0)
            {
                m_sProSender.m_bSendStatus = false;
                JLogE(TAG, "Send program packet fail. packet id=%d, retCode=%d\r\n", m_sProSender.m_u16PktId, u8RetCode);

                m_sProSender.m_u8RetryCnt++;
                if (m_sProSender.m_u8RetryCnt > RETRY_MAX_CNT)
                {
                    m_sProSender.m_u8RetryCnt = 0;
                    if (m_sProSender.m_pfnCallback != NULL)
                    {
                        m_sProSender.m_pfnCallback->onEvent(IJSendProgramCallback::SendStatus::FAIL, 0);
                    }
                    break;
                }

                iRetrySendProgram();
            }
            else
            {
                m_sProSender.m_u16PktId++;
                iSendProgramNextPacket();
            }
        }
        break;
    case Action::E_ACTION_APP_SET_SWITCH_STATUS:
        JLogD(TAG, "Set or notify switch status success, current status=%d\r\n", pu8Data[1]);
        break;
    case Action::E_ACTION_APP_SET_FLIP:
        JLogD(TAG, "Set flip success, current flip=%d\r\n", pu8Data[1]);
        break;
    case Action::E_ACTION_APP_SET_BN:
        JLogD(TAG, "Set brightness success, current bn=%d\r\n", pu8Data[1]);
        break;
    case Action::E_ACTION_APP_GET_DEV_INFO:
    {
        JInfo info;

        info.m_u8SwitchStatus = pu8Data[1];
        info.m_u8Bn = pu8Data[2];
        info.m_u8Flip = pu8Data[3];
        info.m_u8SupportLocalMic = pu8Data[4];
        info.m_u8LocalMicStatus = pu8Data[5];
        info.m_u8LocalMicMode = pu8Data[6];
        info.m_u8EnableShowId = pu8Data[7];
        info.m_u8ProMaxNum = pu8Data[8];
        info.m_u8EnableRemote = pu8Data[9];
        info.m_u8TimerMaxNum = pu8Data[10];
        info.m_u8DevType = pu8Data[11];

        info.m_u32ProjectCode = ((uint32_t)pu8Data[12] << 24);
        info.m_u32ProjectCode |= ((uint32_t)pu8Data[13] << 16);
        info.m_u32ProjectCode |= ((uint32_t)pu8Data[14] << 8);
        info.m_u32ProjectCode |= ((uint32_t)pu8Data[15] << 0);

        info.m_u16Version = ((uint16_t)pu8Data[16] << 8);
        info.m_u16Version |= ((uint16_t)pu8Data[17] << 0);

        //if (u32Len > 18)
        //{
        //    info.m_u8DevleoperFlag = pu8Data[18];
        //}
        //else
        //{
        //    info.m_u8DevleoperFlag = 0;
        //}

        if (u32Len > 20)
        {
            info.m_u16PktMaxSize = ((uint16_t)pu8Data[19] << 8);
            info.m_u16PktMaxSize |= ((uint16_t)pu8Data[20] << 0);
        }
        else
        {
            info.m_u16PktMaxSize = DEFAULT_PKT_MAX_SIZE;  // default;
        }

        // Compatibility handling ensures the new version is compatible with subsequent devices.
        if (u32Len > 22)
        {
            info.m_u16DevId = ((uint16_t)pu8Data[21] << 0);
            info.m_u16DevId |= ((uint16_t)pu8Data[22] << 8);
        }

        if (u32Len > 24)
        {
            info.m_u16DevWidth = ((uint16_t)pu8Data[23] << 8);
            info.m_u16DevWidth |= ((uint16_t)pu8Data[24] << 0);
        }

        if (u32Len > 26)
        {
            info.m_u16DevHeight = ((uint16_t)pu8Data[25] << 8);
            info.m_u16DevHeight |= ((uint16_t)pu8Data[26] << 0);
        }

        if (u32Len > 27)
        {
            uint8_t u8NameLen = pu8Data[27];
            uint8_t au8Name[MAX_NAME_LEN] = {0};

            if (u8NameLen > MAX_NAME_LEN)
            {
                u8NameLen = MAX_NAME_LEN;
            }

            memset(au8Name, 0, MAX_NAME_LEN);
            memcpy(au8Name, pu8Data + 28, u8NameLen);

            info.m_strDevName = std::string(reinterpret_cast<char*>(au8Name), u8NameLen);
        }

        JPrint("Device info: \r\n");
        JPrint("-name: %s\r\n", info.m_strDevName.c_str());
        JPrint("-id: %04X\r\n", info.m_u16DevId);
        JPrint("-height,width: %d,%d\r\n", info.m_u16DevHeight, info.m_u16DevWidth);
        JPrint("-SwitchStatus: %d\r\n", info.m_u8SwitchStatus);
        JPrint("-Brightness: %d\r\n", info.m_u8Bn);
        JPrint("-ScreenFlip: %d\r\n", info.m_u8Flip);
        JPrint("-DevType: %d\r\n", info.m_u8DevType);
        JPrint("-Version: %d\r\n", info.m_u16Version);
        JPrint("-PktMaxSize: %d\r\n", info.m_u16PktMaxSize);

        if (m_pfnGetDevInfoCallback != NULL)
        {
            m_pfnGetDevInfoCallback->onEvent(info);
        }
    }
    break;
    case Action::E_ACTION_MCU_TO_BLE_STARTUP_OPR: // Some products require this command to be parsed before the screen will activate.
    {
        if (pu8Data[1] == 0x01)
        {
            JLogD(TAG, "The screen has started up and will automatically obtain device information.\r\n");
            GetDevInfo(m_pfnGetDevInfoCallback);
        }
        else if (pu8Data[1] == 0x04)  // Power-on message on screen
        {
            JLogD(TAG, "Received power-on message from the screen\r\n");

            JLogD(TAG, "Startup screen\r\n");
            SendStartupScreen();
        }
        break;
    }
    default:
        // Other messages, please ignore.
        break;
    }
}

void JotuPix::Tick(uint32_t u32CurrTick)
{
    m_u32CurrMsTick = u32CurrTick;

    if (m_sProSender.m_bSendStatus)
    {
        if (time_after_eq(m_u32CurrMsTick, m_sProSender.m_u32TimeoutTick))
        {
            m_sProSender.m_bSendStatus = false;
            JLogE(TAG, "Program transmission timed out, ended.\r\n");
            if (m_sProSender.m_pfnCallback != NULL)
            {
                m_sProSender.m_pfnCallback->onEvent(IJSendProgramCallback::SendStatus::FAIL, 0);
            }
        }
    }
}

int JotuPix::SendProgram(ProgramInfo* psProInfo, const uint8_t* pu8ProData, uint32_t u32ProDataLen, IJSendProgramCallback* pfnCallback)
{
	if (psProInfo == nullptr || pu8ProData == nullptr || u32ProDataLen == 0)
	{
		JLogE(TAG, "SendProgram fail, input param error!\r\n");
		return -1;
	}

    m_sProSender.m_bSendStatus = false;
    m_sProSender.m_pfnCallback = nullptr;
    m_sProSender.m_u16PktId = 0;
    m_sProSender.m_u32TimeoutTick = 0;
    m_sProSender.m_u8RetryCnt = 0;
    m_sProSender.m_u16CurrPktPayloadLen = 0;
    m_sProSender.m_u32CurrSendLen = 0;
    m_sProSender.m_au8ProData.clear();

	
	m_sProSender.m_sProgramInfo.m_u8ProIndex = psProInfo->m_u8ProIndex;
	m_sProSender.m_sProgramInfo.m_u8ProAllNum = psProInfo->m_u8ProAllNum;
	m_sProSender.m_sProgramInfo.m_eCompress = psProInfo->m_eCompress;
	m_sProSender.m_sProgramInfo.m_proGroupParam = psProInfo->m_proGroupParam;

	m_sProSender.m_u32ProOrigLen = u32ProDataLen;

	JCrc crc;
	crc.Reset();
	m_sProSender.m_u32ProOrigCrc = crc.Calculate(pu8ProData, u32ProDataLen);

	if (psProInfo->m_eCompress == CompressFlag::COMPRESS_FLAG_DO)
	{
		m_sProSender.m_au8ProData = m_sLzss.Encode(pu8ProData, u32ProDataLen);
	}
	else
	{
		m_sProSender.m_au8ProData.insert(m_sProSender.m_au8ProData.end(), pu8ProData, pu8ProData + u32ProDataLen);
	}

	m_sProSender.m_pfnCallback = pfnCallback;
	m_sProSender.m_u32TimeoutTick = time_get_next_tick(m_u32CurrMsTick, SEND_PRO_TIMEOUT_TICK);
	m_sProSender.m_bSendStatus = true;

    JLogD(TAG, "Program info:\r\n");
    JPrint(" -index: %d\r\n", psProInfo->m_u8ProIndex);
    JPrint(" -all num: %d\r\n", psProInfo->m_u8ProAllNum);
    JPrint(" -compress flag: %d\r\n", psProInfo->m_eCompress);
    JPrint(" -pro size: %d\r\n", u32ProDataLen);
    JPrint(" -pro crc: 0x%x\r\n", m_sProSender.m_u32ProOrigCrc);

	// Send command to start setting program content
	int nRet = SendProgramStart();
	if (nRet != 0)
	{
		JLogE(TAG, "Send program, start fail\r\n");
		CancelSendProgram();
		return -1;
	}

	if (m_sProSender.m_pfnCallback != NULL)
	{
		m_sProSender.m_pfnCallback->onEvent(IJSendProgramCallback::SendStatus::PROGRESS, 0);
	}

	return 0;
}

/**
    <0 fail
    =0 complete
    >0 send length
*/
int JotuPix::iSendProgramNextPacket()
{
    int nRet = -1;

    if (m_sProSender.m_pfnCallback != NULL)
    {
        if (m_sProSender.m_u32CurrSendLen == m_sProSender.m_au8ProData.size())
        {
            m_sProSender.m_bSendStatus = false;
            JLogD(TAG, "Send program success.\r\n");

            m_sProSender.m_pfnCallback->onEvent(IJSendProgramCallback::SendStatus::COMPLETED, 100);

            return 0;
        }
        else
        {
            // Calculate the percentage that has been sent
            uint8_t u8Per = m_sProSender.m_u32CurrSendLen * 100 / m_sProSender.m_au8ProData.size();
            m_sProSender.m_pfnCallback->onEvent(IJSendProgramCallback::SendStatus::PROGRESS, u8Per);
        }
    }

    if (m_sProSender.m_au8ProData.size() - m_sProSender.m_u32CurrSendLen > DEFAULT_PKT_MAX_SIZE)
    {
        m_sProSender.m_u16CurrPktPayloadLen = DEFAULT_PKT_MAX_SIZE;
    }
    else
    {
        m_sProSender.m_u16CurrPktPayloadLen = m_sProSender.m_au8ProData.size() - m_sProSender.m_u32CurrSendLen;
    }

    JLogD(TAG, "Send a packet data, id=%d, len=%d.\r\n", m_sProSender.m_u16PktId, m_sProSender.m_u16CurrPktPayloadLen);

    JPacket::Data packetData;

    packetData.m_u8MsgType = (uint8_t)Action::E_ACTION_APP_SET_PRO;
    packetData.m_u16PacketId = m_sProSender.m_u16PktId;
    packetData.m_u32AllDataLen = m_sProSender.m_au8ProData.size();
    packetData.m_ePacketTransType = JPacket::TransType::LEN;

    if (m_sProSender.m_au8ProData.size() > m_sProSender.m_u32CurrSendLen)
    {
        packetData.m_u8TransCompleteFlag = 0;
    }
    else
    {
        packetData.m_u8TransCompleteFlag = 1;
    }
    packetData.m_pu8Payload = m_sProSender.m_au8ProData.data() + m_sProSender.m_u32CurrSendLen;
    packetData.m_u16PaylaodLen = m_sProSender.m_u16CurrPktPayloadLen;

    nRet = m_sPacket.Send(&packetData, &m_sProtocol);
    if (nRet < 0)
    {
        return -1;
    }

    m_sProSender.m_u32CurrSendLen += m_sProSender.m_u16CurrPktPayloadLen;

    m_sProSender.m_u32TimeoutTick = time_get_next_tick(m_u32CurrMsTick, SEND_PRO_TIMEOUT_TICK);

    return m_sProSender.m_u16CurrPktPayloadLen;
}

void JotuPix::iRetrySendProgram()
{
    int nRet = -1;

    JLogD(TAG, "Retry send program, cnt=%d\r\n", m_sProSender.m_u8RetryCnt);

    m_sProSender.m_u16PktId = 0;
    m_sProSender.m_u32CurrSendLen = 0;

    iSendProgramNextPacket();
}

int JotuPix::CancelSendProgram()
{
	m_sProSender.m_bSendStatus = false;
	m_sProSender.m_au8ProData.clear();
	m_sProSender.m_sProgramInfo.m_proGroupParam = nullptr;

	return 0;
}

int JotuPix::SendProgramStart()
{
	JByteWriter startBuffer;
	JProgramGroupBase::GroupType groupType;

	ProgramInfo* psProInfo = &m_sProSender.m_sProgramInfo;

	startBuffer.put_u8((uint8_t)Action::E_ACTION_APP_START_SET_PRO);

	startBuffer.put_u32(m_sProSender.m_u32ProOrigCrc);
	startBuffer.put_u32(m_sProSender.m_u32ProOrigLen);
	startBuffer.put_u8(psProInfo->m_u8ProIndex);
	startBuffer.put_u8(psProInfo->m_u8ProAllNum);
	startBuffer.put_u8(1); // The default value is 1.

	startBuffer.put_repeat(0, 7); // Reserved bytes,default 0
	startBuffer.put_u8((uint8_t)m_sProSender.m_sProgramInfo.m_eCompress);

	groupType = psProInfo->m_proGroupParam->GetGroupType();

	startBuffer.put_u8((uint8_t)groupType);

	switch (groupType)
	{
	case JProgramGroupBase::GroupType::NOR:
	{
		std::shared_ptr<JProgramGroupNor> groupNor = std::dynamic_pointer_cast<JProgramGroupNor>(psProInfo->m_proGroupParam);
		if (groupNor != nullptr)
		{
			startBuffer.put_u8((uint8_t)groupNor->m_ePlayType);
			startBuffer.put_u32(groupNor->m_u32PlayParam);
		}
		break;
	}
	default:
		JLogE(TAG, "Unsupported types\r\n");
		return -1;
	}

	JLogD(TAG, "Send Start Program Setup Command.\r\n");

	return m_sProtocol.Send(startBuffer.buffer.data(), startBuffer.size());
}

int JotuPix::SendCommand(const uint8_t* pu8Data, uint32_t u32Len)
{
	return m_sProtocol.Send(pu8Data, u32Len);
}

int JotuPix::SendSwitchStatus(uint8_t u8Status)
{
	uint8_t au8Data[2] = { 0 };

	au8Data[0] = (uint8_t)Action::E_ACTION_APP_SET_SWITCH_STATUS;
	au8Data[1] = u8Status;

	return m_sProtocol.Send(au8Data, sizeof(au8Data));
}

int JotuPix::SendBrightness(uint8_t u8Bn)
{
	uint8_t au8Data[2] = { 0 };

	au8Data[0] = (uint8_t)Action::E_ACTION_APP_SET_BN;
	au8Data[1] = u8Bn;

	return m_sProtocol.Send(au8Data, sizeof(au8Data));
}

int JotuPix::SendScreenFlip(uint8_t u8Flip)
{
	uint8_t au8Data[2] = { 0 };

	au8Data[0] = (uint8_t)Action::E_ACTION_APP_SET_FLIP;
	au8Data[1] = u8Flip;

	return m_sProtocol.Send(au8Data, sizeof(au8Data));
}

int JotuPix::SendReset()
{
    // Non - public interface,

    return -1;
}

int JotuPix::GetDevInfo(IJGetDevInfoCallback* pfnCallback)
{
	if (pfnCallback == nullptr)
	{
		JLogE(TAG, "GetDevInfo fail!, callback is null\r\n");
		return -1;
	}

	uint8_t au8Data[1] = { 0 };

	au8Data[0] = (uint8_t)Action::E_ACTION_APP_GET_DEV_INFO;
	m_pfnGetDevInfoCallback = pfnCallback;

	return m_sProtocol.Send(au8Data, sizeof(au8Data));
}

int JotuPix::SendStartupScreen()
{
    uint8_t au8Data[3] = { 0 };

    au8Data[0] = 0x23;
    au8Data[1] = 0x01;
    au8Data[2] = 0x00;

    return m_sProtocol.Send(au8Data, sizeof(au8Data));
}

void JotuPix::PrintLogArrToHex(const char* pu8Title, const uint8_t* pu8Data, uint32_t u32Len)
{
    uint32_t i = 0;

    if (pu8Title != NULL)
    {
        JPrint("%s", pu8Title);
    }

    for (i = 0; i < u32Len; i++)
    {
        JPrint("%02x ", pu8Data[i]);
    }

    JPrint("\r\n");
}