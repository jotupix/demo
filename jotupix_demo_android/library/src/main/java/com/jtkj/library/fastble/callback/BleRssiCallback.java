package com.jtkj.library.fastble.callback;


import com.jtkj.library.fastble.exception.BleException;

public abstract class BleRssiCallback extends BleBaseCallback {

    public abstract void onRssiFailure(BleException exception);

    public abstract void onRssiSuccess(int rssi);

}