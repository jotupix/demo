#pragma once
#include <string>

class JInfo
{
public:
	uint8_t  m_u8SwitchStatus;
	uint8_t  m_u8Bn;
	uint8_t  m_u8Flip;
	uint8_t  m_u8SupportLocalMic;
	uint8_t  m_u8LocalMicStatus;
	uint8_t  m_u8LocalMicMode;
	uint8_t  m_u8EnableShowId;
	uint8_t  m_u8ProMaxNum;
	uint8_t  m_u8EnableRemote;
	uint8_t  m_u8TimerMaxNum;
	uint8_t  m_u8DevType;
	uint32_t m_u32ProjectCode;
	uint16_t m_u16Version;
	uint8_t  m_u8DeveloperFlag;
	uint16_t m_u16PktMaxSize;
	uint16_t m_u16DevId;
	uint16_t m_u16DevWidth;
	uint16_t m_u16DevHeight;
	std::string m_strDevName;
};

