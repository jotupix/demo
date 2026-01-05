package com.jtkj.jotupix.core;

public class JInfo {
    public int switchStatus;
    public int brightness;
    public int flip;
    public int supportLocalMic;
    public int localMicStatus;
    public int localMicMode;
    public int enableShowId;
    public int proMaxNum;
    public int enableRemote;
    public int timerMaxNum;
    public int devType;
    public int projectCode;
    public int version;
    public int developerFlag;
    public int pktMaxSize;
    public int devId;
    public int devWidth;
    public int devHeight;
    public String devName;

    @Override
    public String toString() {
        return "JInfo{" +
                "switchStatus=" + switchStatus +
                ", brightness=" + brightness +
                ", flip=" + flip +
                ", supportLocalMic=" + supportLocalMic +
                ", localMicStatus=" + localMicStatus +
                ", localMicMode=" + localMicMode +
                ", enableShowId=" + enableShowId +
                ", proMaxNum=" + proMaxNum +
                ", enableRemote=" + enableRemote +
                ", timerMaxNum=" + timerMaxNum +
                ", devType=" + devType +
                ", projectCode=" + projectCode +
                ", version=" + version +
                ", developerFlag=" + developerFlag +
                ", pktMaxSize=" + pktMaxSize +
                ", devId=" + devId +
                ", devWidth=" + devWidth +
                ", devHeight=" + devHeight +
                ", devName='" + devName + '\'' +
                '}';
    }
}
