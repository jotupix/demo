// JPacket.m
#import "JPacket.h"
#import "JByteWriter.h"
#import "JLog.h"

#define TAG "JPacket"

@implementation JPacket

/// Build and send a packet through JProtocol.
- (int)send:(JPacketData *)pkt protocol:(JProtocol *)protocol
{
    // Validate parameters
    if (!pkt || !protocol) {
        JLogE(TAG, "Send fail\r\n");
        return -1;
    }

    uint8_t checksum = 0;   // Checksum accumulator
    uint16_t i = 0;

    JByteWriter *writer = [[JByteWriter alloc] init];

    // 1. Write message type
    [writer put_u8:pkt->msgType];

    // 2. Encode transmission flags (packet type bits)
    uint8_t type = 0;
    
    // FLAG transmission method
    if (pkt->transType == JPacketTransType_FLAG)
        type |= 0x01;

    // Completed-flag bit
    if (pkt->transCompleteFlag > 0)
        type |= (0x01 << 1);

    [writer put_u8:type];

    // 3. Total data length (used in LEN mode)
    [writer put_u32:pkt->allDataLen];

    // 4. Packet sequence ID
    [writer put_u16:pkt->packetId];

    // 5. Current packet payload length
    [writer put_u16:pkt->payloadLen];

    // 6. Write payload bytes
    if (pkt->payload && pkt->payloadLen > 0) {
        [writer put_bytes:pkt->payload length:pkt->payloadLen];
    }

    // 7. Compute checksum (XOR of all bytes except the first one)
    for (i = 1; i < writer.size; i++) {
        checksum ^= writer.buffer[i];
    }

    [writer put_u8:checksum];

    // 8. Send constructed packet via protocol 
    return [protocol send:writer.buffer length:writer.size];
}

@end
