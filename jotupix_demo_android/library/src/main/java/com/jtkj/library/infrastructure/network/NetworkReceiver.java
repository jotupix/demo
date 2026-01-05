package com.jtkj.library.infrastructure.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.jtkj.library.Constants;
import com.jtkj.library.commom.logger.CLog;
import com.jtkj.library.infrastructure.eventbus.EventAgent;
import com.jtkj.library.infrastructure.mvp.IEvent;

/**
 * Created by lenovo on 2017/3/16.
 *
 */
public class NetworkReceiver extends BroadcastReceiver {
    public static final String TAG = "NetworkReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        //获得网络连接服务
        ConnectivityManager connManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
		Constants.isConnected = info != null && info.isAvailable();
        CLog.i(TAG, "NetworkReceiver.onReceiver: " + Constants.isConnected);
        EventAgent.postSticky(new IEvent.NetWorkEvent(IEvent.CODE_NETWORK, Constants.isConnected));
		if (!Constants.isConnected)
			Toast.makeText(context, "连接网络失败，请检查网络设置", Toast.LENGTH_SHORT).show();//todo use R.string.common_network_unavailable in app module
	}
}
