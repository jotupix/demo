package com.jtkj.library.fastble.callback;


import com.jtkj.library.fastble.exception.BleException;

public abstract class BleMtuChangedCallback extends BleBaseCallback {

    public abstract void onSetMTUFailure(BleException exception);

    public abstract void onMtuChanged(int mtu);

}
