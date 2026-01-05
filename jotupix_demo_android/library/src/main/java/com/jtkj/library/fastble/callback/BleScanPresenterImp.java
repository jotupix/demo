package com.jtkj.library.fastble.callback;

import com.jtkj.library.fastble.data.BleDevice;

public interface BleScanPresenterImp {

    void onScanStarted(boolean success);

    void onScanning(BleDevice bleDevice);

}
