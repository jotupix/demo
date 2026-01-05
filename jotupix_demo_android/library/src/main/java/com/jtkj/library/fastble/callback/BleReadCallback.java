package com.jtkj.library.fastble.callback;


import com.jtkj.library.fastble.exception.BleException;

public abstract class BleReadCallback extends BleBaseCallback {

    public abstract void onReadSuccess(byte[] data);

    public abstract void onReadFailure(BleException exception);

}
