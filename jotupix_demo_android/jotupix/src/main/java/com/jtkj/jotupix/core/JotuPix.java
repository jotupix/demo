package com.jtkj.jotupix.core;

import android.util.Log;

import com.jtkj.jotupix.utils.JByteWriter;
import com.jtkj.jotupix.utils.Utils;

/**
 * @brief Main class providing communication, protocol handling,
 * program transmission, and device interaction.
 * <p>
 * The JotuPix class encapsulates protocol parsing, LZSS compression,
 * packet sending, program sending state machine, and device commands.
 * It also manages callbacks for program transmission and device info retrieval.
 */
public class JotuPix implements JProtocol.JParseCallBack {
    private static final String TAG = JotuPix.class.getSimpleName();

    /**
     * @brief Callback interface for sending program transmission status.
     * <p>
     * This interface is implemented by the caller to receive progress updates,
     * completion notifications, or failure results during program transmission.
     */
    public interface JSendProgramCallback {

        class SendStatus {
            public static final int COMPLETED = 0;//Transmission completed successfully.
            public static final int PROGRESS = 1;//Transmission is in progress.
            public static final int FAIL = 2;//Transmission failed.
        }

        /**
         * @param sendStatus Current transmission status.
         * @param percent    Transmission progress percentage (0�C100).
         * @brief Called when program transmission status changes.
         */
        void onEvent(int sendStatus, double percent);
    }


    /**
     * @brief Callback interface for receiving device information.
     */
    public interface JGetDevInfoCallback {
        /**
         * @param devInfo Device information object.
         * @brief Called when device information is retrieved.
         */
        void onEvent(JInfo devInfo);
    }


    public abstract static class GroupType {
        public static final int NOR = 0;
    }

    public abstract static class JProgramGroupBase {

        public abstract int getGroupType();
    }

    public static class PlayType {
        public static int PLAY_TYPE_CNT = 0;  // Play by number of times
        public static int PLAY_TYPE_DUR = 1;   // Play by duration
        public static int PLAY_TYPE_TIME = 2;  // Play by time
    }

    public static class JProgramGroupNor extends JProgramGroupBase {
        private int groupType = GroupType.NOR;
        public int playType = PlayType.PLAY_TYPE_CNT;
        public int playParam = 0;

        @Override
        public int getGroupType() {
            return groupType;
        }
    }


    public static class CompressFlag {
        public static int COMPRESS_FLAG_DO = 0;    // Compress
        public static int COMPRESS_FLAG_UNDO = 1;  // No compression is required.
    }

    public static class ProgramInfo {
        public int proIndex;
        public int proAllNum;
        public int compress; // Compress or not?
        public JProgramGroupBase groupParam;
    }

    public static class Action {
        public static final int E_ACTION_UNKNOWN = 0;
        public static final int E_ACTION_APP_MUSIC = 0x01;   // Music rhythm mode 1
        public static final int E_ACTION_APP_START_SET_PRO = 0x02;// Start setting program content
        public static final int E_ACTION_APP_SET_PRO = 0x03;  // Set program content
        public static final int E_ACTION_APP_SET_BN = 0x04;  // Set display brightness
        public static final int E_ACTION_APP_SET_SWITCH_STATUS = 0x05; // Set switch status
        public static final int E_ACTION_APP_SET_LOCAL_MUSIC = 0x06; // Local microphone mode
        public static final int E_ACTION_APP_PLAY_PROLIST_BY_INDEX = 0x07; // Play program by index
        public static final int E_ACTION_APP_DEL_PROLIST_BY_INDEX = 0x08; // Delete program by index
        public static final int E_ACTION_APP_UPDATE_TIME = 0x09; // Update time
        public static final int E_ACTION_APP_SET_TIMERS = 0x0A;// Set timers
        public static final int E_ACTION_APP_GET_TIMERS = 0x0B;  // Get timers
        public static final int E_ACTION_APP_SET_FLIP = 0x0C; // Set flip status
        public static final int E_ACTION_APP_CHECK_PASSWORD = 0x0D; // Check password (6 digits)
        public static final int E_ACTION_APP_SET_PASSWORD = 0x0E;  // Set password (6 digits)
        public static final int E_ACTION_APP_OPR_COUNTDOWN = 0x0F; // Countdown operation 0x0F
        public static final int E_ACTION_APP_OPR_STOPWATCH = 0x10; // Stopwatch operation 0x10
        public static final int E_ACTION_APP_OPR_SCOREBOARD = 0x11;// Scoreboard operation 0x11
        public static final int E_ACTION_APP_SET_GRAPHICS = 0x12;// Set graphic drawing info 0x12
        public static final int E_ACTION_APP_OPR_LIGHT = 0x13; // Light operation: set color, dynamic modes, etc. 0x13
        public static final int E_ACTION_APP_SET_DEV_INFO = 0x1E; // Set device configuration info
        public static final int E_ACTION_APP_GET_DEV_INFO = 0x1F; // Get device information
    }

    private static class ProgramSender {
        boolean sendStatus = false;
        JSendProgramCallback callback = null;
        int pktId = 0;
        long timeoutTick = 0;
        int retryCnt = 0;
        int currPktPayloadLen = 0;
        ProgramInfo programInfo = null;
        int currSendLen = 0;
        byte[] proData;
        int proOrigCrc;
        int proOrigLen = 0;
    }

    private final JLzss lzss = new JLzss();
    private final JProtocol protocol = new JProtocol();
    private final JPacket packet = new JPacket();
    private final ProgramSender proSender = new ProgramSender();
    private long currMsTick = 0;
    private JGetDevInfoCallback getDevInfoCallback = null;

    private static final long SEND_PRO_TIMEOUT_TICK = 5 * 1000;  // ms
    private static final int DEFAULT_PKT_MAX_SIZE = 1024;
    private static final int RETRY_MAX_CNT = 3;// Maximum number of retries
    private static final int MAX_NAME_LEN = 20;

    /**
     * @param sender Interface used to send raw data through
     *               the underlying communication layer.
     * @brief Initialize the JotuPix module.
     */
    public void init(JProtocol.JSend sender) {
        protocol.init(sender, this);
    }

    /**
     * @param data   Pointer to received data.
     * @param length Length of received data.
     * @brief Provide received raw data to the protocol parser.
     */
    public void parseRecvData(byte[] data, int length) {
        protocol.parse(data, length);
    }

    @Override
    public void onParseComplete(byte[] data, int length) {
        Log.i(TAG, "parse: >>>" + Utils.byte2hex(data) + "length>>>" + length);

        if (length == 0) return;

        int msgType = data[0] & 0xFF;
        Log.i(TAG, "msgType: >>>" + msgType);
        switch (msgType) {
            case Action.E_ACTION_APP_START_SET_PRO:
                handleStartSetProgram(data, length);
                break;

            case Action.E_ACTION_APP_SET_PRO:
                handleSetProgram(data, length);
                break;

            case Action.E_ACTION_APP_SET_SWITCH_STATUS:
                Log.i(TAG, "Set or notify switch status success, current status=" + (data[1] & 0xFF));
                break;

            case Action.E_ACTION_APP_SET_FLIP:
                Log.i(TAG, "Set flip success, current flip=" + (data[1] & 0xFF));
                break;

            case Action.E_ACTION_APP_SET_BN:
                Log.i(TAG, "Set brightness success, current bn=" + (data[1] & 0xFF));
                break;

            case Action.E_ACTION_APP_GET_DEV_INFO:
                handleGetDevInfo(data, length);
                break;

            default:
                break;
        }
    }

    private void handleStartSetProgram(byte[] data, int length) {
        if (proSender.sendStatus && length > 1) {
            int response = data[1] & 0xFF;
            if (response == 0) {
                Log.i(TAG, "send program success, screen not has the program need to send");
                sendProgramNextPacket();
            } else if (response == 1) {
                proSender.sendStatus = false;
                Log.i(TAG, "send program success, screen already has the program");
                if (proSender.callback != null) {
                    proSender.callback.onEvent(JSendProgramCallback.SendStatus.COMPLETED, 100);
                }
            } else {
                proSender.sendStatus = false;
                Log.i(TAG, "send program start fail");
                if (proSender.callback != null) {
                    proSender.callback.onEvent(JSendProgramCallback.SendStatus.FAIL, 0);
                }
            }
        }
    }

    private void handleSetProgram(byte[] data, int length) {
        if (proSender.sendStatus && length > 4) {
            int retCode = data[4] & 0xFF;
            if (retCode != 0) {
                proSender.sendStatus = false;
                Log.e(TAG, String.format("send program packet fail. packet id=%d, retCode=%d", proSender.pktId, retCode));

                proSender.retryCnt++;
                if (proSender.retryCnt > RETRY_MAX_CNT) {
                    proSender.retryCnt = 0;
                    if (proSender.callback != null) {
                        proSender.callback.onEvent(JSendProgramCallback.SendStatus.FAIL, 0);
                    }
                    return;
                }

                retrySendProgram();
            } else {
                proSender.pktId++;
                sendProgramNextPacket();
            }
        }
    }

    private void handleGetDevInfo(byte[] data, int length) {
        JInfo info = new JInfo();

        if (length > 1) info.switchStatus = data[1] & 0xFF;
        if (length > 2) info.brightness = data[2] & 0xFF;
        if (length > 3) info.flip = data[3] & 0xFF;
        if (length > 4) info.supportLocalMic = data[4] & 0xFF;
        if (length > 5) info.localMicStatus = data[5] & 0xFF;
        if (length > 6) info.localMicMode = data[6] & 0xFF;
        if (length > 7) info.enableShowId = data[7] & 0xFF;
        if (length > 8) info.proMaxNum = data[8] & 0xFF;
        if (length > 9) info.enableRemote = data[9] & 0xFF;
        if (length > 10) info.timerMaxNum = data[10] & 0xFF;
        if (length > 11) info.devType = data[11] & 0xFF;

        if (length > 15) {
            info.projectCode = ((data[12] & 0xFF) << 24) |
                    ((data[13] & 0xFF) << 16) |
                    ((data[14] & 0xFF) << 8) |
                    (data[15] & 0xFF);
        }

        // version
        if (length > 17) {
            info.version = ((data[16] & 0xFF) << 8) | (data[17] & 0xFF);
        }

        // max package size
        if (length > 20) {
            info.pktMaxSize = ((data[19] & 0xFF) << 8) | (data[20] & 0xFF);
        } else {
            info.pktMaxSize = DEFAULT_PKT_MAX_SIZE;
        }

        // device ID
        if (length > 22) {
            info.devId = ((data[21] & 0xFF) << 8) | (data[22] & 0xFF);
        }

        // device width
        if (length > 24) {
            info.devWidth = ((data[23] & 0xFF) << 8) | (data[24] & 0xFF);
        }

        // device height
        if (length > 26) {
            info.devHeight = ((data[25] & 0xFF) << 8) | (data[26] & 0xFF);
        }

        // device name
        if (length > 27) {
            int nameLen = data[27] & 0xFF;
            if (nameLen > MAX_NAME_LEN) {
                nameLen = MAX_NAME_LEN;
            }
            if (length > 27 + nameLen) {
                byte[] nameBytes = new byte[nameLen];
                System.arraycopy(data, 28, nameBytes, 0, nameLen);
                info.devName = new String(nameBytes);
            }
        }

        Log.i(TAG, "Device info:");
        Log.i(TAG, String.format("- name: %s", info.devName != null ? info.devName : ""));
        Log.i(TAG, String.format("- id: %04X", info.devId));
        Log.i(TAG, String.format("- height,width: %d,%d", info.devHeight, info.devWidth));
        Log.i(TAG, String.format("- SwitchStatus: %d", info.switchStatus));
        Log.i(TAG, String.format("- Brightness: %d", info.brightness));
        Log.i(TAG, String.format("- ScreenFlip: %d", info.flip));
        Log.i(TAG, String.format("- DevType: %d", info.devType));
        Log.i(TAG, String.format("- Version: %d", info.version));
        Log.i(TAG, String.format("- PktMaxSize: %d", info.pktMaxSize));

        if (getDevInfoCallback != null) {
            getDevInfoCallback.onEvent(info);
        }
    }

    /**
     * @param currTick Current system tick in milliseconds.
     * @brief Periodic tick function that must be called from the UI thread.
     * <p>
     * Handles program sending timeouts, retries, and internal state updates.
     */
    public void tick(long currTick) {
        currMsTick = currTick;

        if (proSender.sendStatus) {
            if (JTick.timeAfterEq(currMsTick, proSender.timeoutTick)) {
                proSender.sendStatus = false;
                Log.i(TAG, "Program transmission timed out, ended");
                if (proSender.callback != null) {
                    proSender.callback.onEvent(JSendProgramCallback.SendStatus.FAIL, 0);
                }
            }
        }
    }

    /**
     * @param proInfo    Program settings and metadata.
     * @param proData    Raw program binary data.
     * @param proDataLen Length of the binary program data.
     * @param callback   Callback to receive send progress updates.
     * @return 0 on success, negative on error.
     * @brief Start sending a program to the device.
     */
    public int sendProgram(ProgramInfo proInfo, byte[] proData, int proDataLen, JSendProgramCallback callback) {
        if (proInfo == null || proData == null || proDataLen == 0) {
            Log.e(TAG, "SendProgram fail, input param error!");
            return -1;
        }

        proSender.sendStatus = false;
        proSender.callback = null;
        proSender.pktId = 0;
        proSender.timeoutTick = 0;
        proSender.retryCnt = 0;
        proSender.currPktPayloadLen = 0;
        proSender.currSendLen = 0;
        proSender.proData = null;

        proSender.programInfo = proInfo;
        proSender.proOrigLen = proDataLen;

        JCrc crc = new JCrc();
        crc.reset();
        proSender.proOrigCrc = crc.calculate(proData, proDataLen);

        if (proInfo.compress == CompressFlag.COMPRESS_FLAG_DO) {
            byte[] compressed = lzss.encode(proData);
            proSender.proData = compressed;
        } else {
            proSender.proData = proData;
        }

        proSender.callback = callback;
        proSender.timeoutTick = JTick.getNextTick(currMsTick, SEND_PRO_TIMEOUT_TICK);
        proSender.sendStatus = true;

        Log.i(TAG, "Program info:");
        Log.i(TAG, String.format(" - index: %d", proInfo.proIndex));
        Log.i(TAG, String.format(" - all num: %d", proInfo.proAllNum));
        Log.i(TAG, String.format(" - compress flag: %d", proInfo.compress));
        Log.i(TAG, String.format(" - pro size: %d", proDataLen));
        Log.i(TAG, " - pro crc: " + proSender.proOrigCrc);

        // send command to start setting program content
        int ret = sendProgramStart();
        if (ret != 0) {
            Log.i(TAG, "send program, start fail");
            cancelSendProgram();
            return -1;
        } else {
            Log.i(TAG, "send program, start success");
        }

        if (proSender.callback != null) {
            proSender.callback.onEvent(JSendProgramCallback.SendStatus.PROGRESS, 0);
        }

        return 0;
    }

    /**
     * <0 fail
     * =0 complete
     * >0 send length
     */
    private int sendProgramNextPacket() {
        if (proSender.callback != null) {
            if (proSender.currSendLen == proSender.proData.length) {
                proSender.sendStatus = false;
                Log.i(TAG, "send program success");
                proSender.callback.onEvent(JSendProgramCallback.SendStatus.COMPLETED, 100);
                return 0;
            } else {
                Log.i(TAG, "send program not success");
                int percent = proSender.currSendLen * 100 / proSender.proData.length;
                proSender.callback.onEvent(JSendProgramCallback.SendStatus.PROGRESS, percent);
            }
        }
        int remaining = proSender.proData.length - proSender.currSendLen;
        if (remaining > DEFAULT_PKT_MAX_SIZE) {
            proSender.currPktPayloadLen = DEFAULT_PKT_MAX_SIZE;
        } else {
            proSender.currPktPayloadLen = remaining;
        }

        Log.i(TAG, String.format("send a packet data, id=%d, len=%d", proSender.pktId, proSender.currPktPayloadLen));

        JPacket.Data packetData = new JPacket.Data();
        packetData.msgType = Action.E_ACTION_APP_SET_PRO;
        packetData.packetId = proSender.pktId;
        packetData.allDataLen = proSender.proData.length;
        packetData.packetTransType = JPacket.TransType.LEN;
        packetData.transCompleteFlag = (proSender.currSendLen + proSender.currPktPayloadLen >= proSender.proData.length) ? 1 : 0;


        byte[] payload = new byte[proSender.currPktPayloadLen];
        for (int i = 0; i < proSender.currPktPayloadLen; i++) {
            byte value = proSender.proData[proSender.currSendLen + i];
            payload[i] = value;
        }
        packetData.payload = payload;
        packetData.payloadLen = proSender.currPktPayloadLen;
        int ret = packet.send(packetData, protocol);
        Log.i(TAG, "ret>" + ret);
        if (ret < 0) {
            return -1;
        }

        proSender.currSendLen += proSender.currPktPayloadLen;
        proSender.timeoutTick = JTick.getNextTick(currMsTick, SEND_PRO_TIMEOUT_TICK);

        return proSender.currPktPayloadLen;
    }

    private void retrySendProgram() {
        Log.i(TAG, String.format("Retry send program, cnt=%d", proSender.retryCnt));

        proSender.pktId = 0;
        proSender.currSendLen = 0;

        sendProgramNextPacket();
    }

    /**
     * @return 0 on success, negative on error.
     * @brief Cancel an ongoing program transmission.
     */
    public int cancelSendProgram() {
        proSender.sendStatus = false;
        proSender.proData = null;
        proSender.programInfo = null;
        return 0;
    }

    private int sendProgramStart() {
        if (proSender.programInfo == null || proSender.programInfo.groupParam == null) {
            return -1;
        }
        ProgramInfo proInfo = proSender.programInfo;
        JByteWriter startBuffer = new JByteWriter();

        // Put Action type
        startBuffer.putByteOne(Action.E_ACTION_APP_START_SET_PRO);

        // Put proOrigCrc
        startBuffer.putBytesFour(proSender.proOrigCrc);

        // Put proOrigLen
        startBuffer.putBytesFour(proSender.proOrigLen);

        // Put proIndex
        startBuffer.putByteOne(proInfo.proIndex);

        // Put proAllNum
        startBuffer.putByteOne(proInfo.proAllNum);

        // Put default  1 byte zero
        startBuffer.putByteOne(0);

        // Put 7 bytes of zeros
        startBuffer.putRepeatByteOne(0, 7); // Reserved bytes,default 0

        int compressFlag = 0;
        if (proInfo.compress == CompressFlag.COMPRESS_FLAG_DO) {
            compressFlag = 0;
        } else if (proInfo.compress == CompressFlag.COMPRESS_FLAG_UNDO) {
            compressFlag = 1;
        }
        // Put compressFlag
        startBuffer.putByteOne(compressFlag);

        int groupType = proInfo.groupParam.getGroupType();
        // Put groupType
        startBuffer.putByteOne(groupType);

        switch (groupType) {
            case GroupType.NOR:
                if (proInfo.groupParam instanceof JProgramGroupNor) {
                    JProgramGroupNor groupNor = (JProgramGroupNor) proInfo.groupParam;
                    // Put playType
                    startBuffer.putByteOne(groupNor.playType);
                    // Put playParam
                    startBuffer.putBytesFour(groupNor.playParam);
                }
                break;
            default:
                Log.i(TAG, "Unsupported types");
                return -1;
        }

        byte[] data = startBuffer.toByteArray();
        Log.i(TAG, "send Start Program Setup Command");
        return protocol.send(data, data.length);
    }


    /**
     * @param data   Command payload buffer.
     * @param length Length of payload.
     * @return 0 on success, negative on error.
     * @brief send a command to the device.
     */
    public int sendCommand(byte[] data, int length) {
        return protocol.send(data, length);
    }

    // send simple commands
    public int sendSwitchStatus(int status) {
        byte[] data = new byte[2];
        data[0] = (byte) Action.E_ACTION_APP_SET_SWITCH_STATUS;
        data[1] = (byte) status;
        return protocol.send(data, data.length);
    }

    // send simple commands
    public int sendBrightness(int brightness) {
        byte[] data = new byte[2];
        data[0] = (byte) Action.E_ACTION_APP_SET_BN;
        data[1] = (byte) brightness;
        return protocol.send(data, data.length);
    }

    // send simple commands
    public int sendScreenFlip(int flip) {
        byte[] data = new byte[2];
        data[0] = (byte) Action.E_ACTION_APP_SET_FLIP;
        data[1] = (byte) flip;
        return protocol.send(data, data.length);
    }

    public int sendReset() {
        return -1;
    }

    /**
     * @param callback Callback receiving the device info.
     * @return 0 on success, negative on error.
     * @brief Request device information.
     */
    public int getDevInfo(JGetDevInfoCallback callback) {
        if (callback == null) {
            Log.i(TAG, "GetDevInfo fail!, callback is null");
            return -1;
        }

        byte[] data = new byte[1];
        data[0] = (byte) Action.E_ACTION_APP_GET_DEV_INFO;
        getDevInfoCallback = callback;

        return protocol.send(data, data.length);
    }

    /**
     * @brief send the startup screen command (required in serial mode).
     */
    public int sendStartupScreen() {
        byte[] data = new byte[3];
        data[0] = 0x23;
        data[1] = 0x01;
        data[2] = 0x00;
        return protocol.send(data, data.length);
    }

}
