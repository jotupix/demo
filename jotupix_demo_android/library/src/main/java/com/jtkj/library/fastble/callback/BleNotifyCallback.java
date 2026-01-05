package com.jtkj.library.fastble.callback;


import com.jtkj.library.fastble.exception.BleException;

public abstract class BleNotifyCallback extends BleBaseCallback {

    public abstract void onNotifySuccess();

    public abstract void onNotifyFailure(BleException exception);

    public abstract void onCharacteristicChanged(byte[] data);

}
