package com.jtkj.jotupix.core;

import android.util.Log;

import com.jtkj.jotupix.utils.JByteWriter;


public class JPacket {
    private static final String TAG = JPacket.class.getSimpleName();

    public static class TransType {
        public static int LEN = 0;   // Determining whether the transmission is complete is based on the length.
        public static int FLAG = 1;   // Use flags to determine whether the transmission is complete.
    }

    /**
     * @brief Packet description used for sending data through JProtocol.
     * <p>
     * This structure contains all metadata required to construct and send a
     * protocol packet, including message type, packet ID, transmission method,
     * and payload information.
     */
    public static class Data {
        public int msgType;                 //Application-level message type.
        public int allDataLen;              //Total data length (for LEN mode).
        public int packetId;                // Packet sequence ID.
        public int packetTransType;   // Transmission completion method.
        public int transCompleteFlag;       //  Completion flag (for FLAG mode).
        public byte[] payload;        //payload data .
        public int payloadLen;              //Payload length in bytes.

        public Data() {
            this.packetTransType = TransType.LEN;
            this.transCompleteFlag = 0;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "msgType=" + msgType +
                    ", allDataLen=" + allDataLen +
                    ", packetId=" + packetId +
                    ", packetTransType=" + packetTransType +
                    ", transCompleteFlag=" + transCompleteFlag +
                    ", payload=" + payload +
                    ", payloadLen=" + payloadLen +
                    '}';
        }
    }

    /**
     * @param packetData Pointer to the packet data structure describing the packet.
     * @param protocol   Communication protocol used to send the packet.
     * @return 0 on success,
     * negative value on error (e.g., invalid parameters or send failure).
     * @brief Build and send a packet using the provided protocol object.
     */
    public int send(Data packetData, JProtocol protocol) {

        if (packetData == null || protocol == null) {
            Log.e(TAG, "[ERROR] JPacket: send fail - null parameter");
            return -1;
        }

        byte u8CheckSum = 0;
        JByteWriter sendData = new JByteWriter();
        sendData.putByteOne((byte) packetData.msgType);

        byte u8Type = 0;
        if (packetData.packetTransType == TransType.FLAG) {
            u8Type |= 0x01;
        }

        if (packetData.transCompleteFlag > 0) {
            u8Type |= (0x01 << 1);
        }

        sendData.putByteOne(u8Type);

        // all data len
        sendData.putBytesFour(packetData.allDataLen);

        // packet id
        sendData.putBytesTwo(packetData.packetId);

        // Current packet data length, 2 bytes, subsequent padding
        sendData.putBytesTwo(packetData.payloadLen);

        sendData.putBytes(packetData.payload);

        byte[] buffer = sendData.toByteArray();
        for (int i = 1; i < sendData.size(); i++) {
            u8CheckSum ^= buffer[i];
        }

        // Get the check byte, excluding the message type
        sendData.putByteOne(u8CheckSum);
        byte[] data = sendData.toByteArray();

        return protocol.send(data, data.length);
    }
}