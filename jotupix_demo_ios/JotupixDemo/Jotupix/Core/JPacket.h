// JPacket.h
#pragma once

#import <Foundation/Foundation.h>
#import <stdint.h>
#import "JProtocol.h"   // Communication protocol interface
#import "JDefine.h"

@interface JPacket : NSObject

/// Transmission type:
/// LEN  - completion determined by total data length.
/// FLAG - completion determined by complete flag.
typedef NS_ENUM(uint8_t, JPacketTransType) {
    JPacketTransType_LEN  = 0,
    JPacketTransType_FLAG = 1
};

/// Packet description used for sending data through JProtocol.
typedef struct {
    uint8_t msgType;            // Application-level message type (m_u8MsgType)
    uint32_t allDataLen;        // Total data length for LEN mode (m_u32AllDataLen)
    uint16_t packetId;          // Packet sequence ID (m_u16PacketId)
    JPacketTransType transType; // Transmission completion method (m_ePacketTransType)
    uint8_t transCompleteFlag;  // Completion flag for FLAG mode (m_u8TransCompleteFlag)

    const uint8_t *payload;     // Pointer to payload data buffer (m_pu8Payload)
    uint16_t payloadLen;        // Payload length in bytes (m_u16PaylaodLen)
} JPacketData;

/// Build and send a packet using the given protocol object.
- (int)send:(JPacketData *)data protocol:(JProtocol *)protocol;

@end
