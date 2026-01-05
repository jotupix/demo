package com.jtkj.jotupix.core;

import android.util.Log;

import com.jtkj.jotupix.utils.JByteWriter;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * @brief Communication protocol handler for sending and parsing framed data.
 * <p>
 * The JProtocol class packages outgoing data and parses incoming data.
 * Users must provide a send interface (JSend) and a callback interface
 * (JParseCallBack) to receive parsed frames.
 */
public class JProtocol {
    private static final String TAG = JProtocol.class.getSimpleName();
    // buff length of parsed data
    private static final int JPROTOCOL_RX_PARSE_BUFF_SIZE = 256;

    // Start mark
    private static final byte PROTOCOL_START_CHAR = 0x01;
    // Protocol Flags
    private static final byte PROTOCOL_ESC_CHAR = 0x02;
    // Protocol XOR value
    private static final byte PROTOCOL_ESC_XOR_CHAR = 0x04;
    // End Marker
    private static final byte PROTOCOL_END_CHAR = 0x03;

    private static final int PROTOCOL_SEND_MAX_LEN = 65535;

    // Receive state machine status
    private enum PROTOCOL_RECV_STATUS_E {
        E_PROTOCOL_RECV_STATUS_DATA, // Receiving Data
        E_PROTOCOL_RECV_STATUS_START, // Receive start signal
        E_PROTOCOL_RECV_STATUS_DATALEN_H, // Receive data length high bit
        E_PROTOCOL_RECV_STATUS_DATALEN_L // Receive data length low bit
    }

    // Interface for receiving parse-complete callback events.
    public interface JParseCallBack {
        /**
         * @param data   Pointer to the parsed data buffer.
         *               The buffer is valid only during the callback scope unless
         *               explicitly documented otherwise.
         * @param length Length of the parsed data buffer in bytes.
         * @brief Called when the parsing process is completed.
         * @note Implementers should avoid performing time-consuming operations
         * inside this callback to prevent blocking the caller thread.
         */
        void onParseComplete(byte[] data, int length);
    }

    // Interface for sending raw data.
    public interface JSend {
        /**
         * @param data   Pointer to the data buffer to send.
         * @param length Length of the data in bytes.
         * @return int Returns 0 on success, negative values on error.
         * @brief send a block of data.
         */
        int send(byte[] data, int length);
    }

    // tx
    // Handler for sending data.
    private JSend sender = null;

    // Callback for parsed frames.
    private JParseCallBack callBack = null;
    // Parsing buffer.
    private byte[] parseBuffer = new byte[JPROTOCOL_RX_PARSE_BUFF_SIZE];
    // Receive state machine status.
    private PROTOCOL_RECV_STATUS_E recvStatus = PROTOCOL_RECV_STATUS_E.E_PROTOCOL_RECV_STATUS_START;
    private int recvIndex = 0;
    private int parseDataLen = 0;
    private boolean inEsc = false;

    /**
     * @param sender   Pointer to an object that implements JSend for data transmission.
     * @param callback Pointer to an object that receives parsed data frames.
     * @brief Initialize protocol with send handler and parse callback.
     */
    public void init(JSend sender, JParseCallBack callback) {
        this.sender = sender;
        this.callBack = callback;
    }


    /**
     * @param data   Pointer to raw data to be packed and sent.
     * @param length Length of the data in bytes.
     * @return int Returns 0 on success, negative values on error.
     * @brief send a data frame through the protocol.
     * <p>
     * This method will wrap the raw data into protocol format before sending.
     */
    public int send(byte[] data, int length) {

        if (sender == null) {
            Log.e(TAG, "JProtocol: send data fail, send is NULL");
            return -1;
        }

        if (data == null || length == 0 || length > PROTOCOL_SEND_MAX_LEN) {
            Log.e(TAG, "JProtocol: send data fail, input error");
            return -1;
        }


        byte[] buffer = processDataByFormat(data);
        return sender.send(buffer, buffer.length);
    }

    /**
     * @param data   Pointer to received data.
     * @param length Number of received bytes.
     * @brief Parse incoming raw data.
     * <p>
     * This function should be called whenever new bytes are received.
     * It processes the protocol state machine and triggers the callback
     * when a full frame is successfully parsed.
     */
    public void parse(byte[] data, int length) {
        int i = 0;
        int u8Byte = 0;

        if (data == null || length == 0) {
            Log.e(TAG, "JProtocol: Parse data fail, input error");
            return;
        }

        for (i = 0; i < length; i++) {
            u8Byte = data[i] & 0xFF;

            switch (u8Byte) {
                case PROTOCOL_START_CHAR:      // Start Marker
                    parseDataLen = 0;
                    inEsc = false;
                    recvStatus = PROTOCOL_RECV_STATUS_E.E_PROTOCOL_RECV_STATUS_DATALEN_H;  // Waiting to receive data length
                    break;
                case PROTOCOL_ESC_CHAR:     //0x02
                    inEsc = true;
                    break;
                case PROTOCOL_END_CHAR:    // End Marker
                    if (recvStatus == PROTOCOL_RECV_STATUS_E.E_PROTOCOL_RECV_STATUS_DATA
                            && parseDataLen <= JPROTOCOL_RX_PARSE_BUFF_SIZE
                            && parseDataLen > 0) {
                        if (callBack != null) {
                            byte[] parsedData = Arrays.copyOf(parseBuffer, parseDataLen);
                            callBack.onParseComplete(parsedData, parseDataLen);
                        }
                    }

                    recvStatus = PROTOCOL_RECV_STATUS_E.E_PROTOCOL_RECV_STATUS_START; // Waiting for receiving start flag
                    break;
                default:
                    if (inEsc)   //0x02
                    {
                        u8Byte ^= PROTOCOL_ESC_XOR_CHAR;   // XOR operation
                        inEsc = false;
                    }

                    switch (recvStatus) {
                        case E_PROTOCOL_RECV_STATUS_DATA: {
                            if (recvIndex < parseDataLen) {
                                parseBuffer[recvIndex] = (byte) u8Byte;
                                recvIndex++;
                            }
                        }
                        break;
                        case E_PROTOCOL_RECV_STATUS_DATALEN_H: {
                            parseDataLen = u8Byte << 8;
                            recvStatus = PROTOCOL_RECV_STATUS_E.E_PROTOCOL_RECV_STATUS_DATALEN_L;
                        }
                        break;
                        case E_PROTOCOL_RECV_STATUS_DATALEN_L: {
                            parseDataLen |= u8Byte;

                            if (parseDataLen > JPROTOCOL_RX_PARSE_BUFF_SIZE || parseDataLen == 0) {
                                recvStatus = PROTOCOL_RECV_STATUS_E.E_PROTOCOL_RECV_STATUS_START;
                                parseDataLen = 0;
                            } else {
                                recvStatus = PROTOCOL_RECV_STATUS_E.E_PROTOCOL_RECV_STATUS_DATA;
                                recvIndex = 0;
                            }
                        }
                        break;
                        default:
                            break;
                    }
                    break;
            }
        }
    }

    /***
     *  process the data before send by the data format
     * @param data
     * @return
     */
    private static byte[] processDataByFormat(byte[] data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(PROTOCOL_START_CHAR);

        int dataLen = data.length;

        JByteWriter byteWriter = new JByteWriter();
        byteWriter.putBytesTwo(dataLen);
        byte[] lengthBytes = byteWriter.toByteArray();

        byte[] escapedLength = convertDataBytes(lengthBytes);
        baos.write(escapedLength, 0, escapedLength.length);

        byte[] escapedData = convertDataBytes(data);
        baos.write(escapedData, 0, escapedData.length);

        baos.write(PROTOCOL_END_CHAR);

        return baos.toByteArray();
    }

    private static byte[] convertDataBytes(byte[] data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for (byte b : data) {
            int value = b & 0xFF;

            if (value > 0 && value < PROTOCOL_ESC_XOR_CHAR) {
                baos.write(PROTOCOL_ESC_CHAR);
                baos.write(value ^ PROTOCOL_ESC_XOR_CHAR);
            } else {
                baos.write(value);
            }
        }

        return baos.toByteArray();
    }

}







