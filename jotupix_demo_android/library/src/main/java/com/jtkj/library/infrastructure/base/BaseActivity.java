package com.jtkj.library.infrastructure.base;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.jtkj.library.commom.dialog.DialogBuilder;
import com.jtkj.library.commom.logger.CLog;
import com.jtkj.library.infrastructure.eventbus.EventAgent;
import com.jtkj.library.infrastructure.mvp.IEvent;
import com.jtkj.library.infrastructure.mvp.ViewActivity;
import com.jtkj.library.infrastructure.statics.StatService;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by yfxiong on 2017/5/3.
 * Copyright  yfxiong www.jotus-tech.com. All Rights Reserved.
 *
 * 继承自v7包的AppCompatActivity</br>
 * 集成EventBus、统计SDK</br>
 * 且在destroy时cancel所有以此实现类为TAG的网络请求</br>
 * *建议在onCreate初始化Presenter，在onDestroy销毁Presenter</br>
 * *此处需要考虑要不要把EventBus和网络请求的生命周期压缩到start和stop</br>
 *
 * *先在Fragment的onActivityCreated里恢复变量值，然后在onViewStateRestored里恢复View状态</br>
 * *onCreate(), onCreateView(), onViewCreated()和onActivityCreated()均有savedInstanceState变量作为入参</br>
 * *生命周期保存恢复可参考https://github.com/nuuneoi/StatedFragment</br>
 */
public abstract class BaseActivity extends ViewActivity {
	protected TextView mTitle;

	protected boolean mHadInit = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setStatusBar(this);
		EventAgent.register(this);
		//setTitleBarColor(R.color.color_top_bg_blue);
	}

	/**
	 * 设置activity的半透明状态栏（4.4以上系统、5.1以上系统有效）
	 */
	@SuppressLint("NewApi")
	public static void setStatusBar(Activity activity) {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				Window window = activity.getWindow();
				window.setFlags(
						WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
						WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					window.setStatusBarColor(Color.TRANSPARENT);
				}
			}
		} catch (Exception e) {
			CLog.e("BaseActivity", e.getMessage());
		}
	}

	protected void setTitleBarColor(int color) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			setTranslucentStatus(true);
			SystemBarTintManager tintManager = new SystemBarTintManager(this);
			tintManager.setStatusBarTintEnabled(true);
			tintManager.setStatusBarTintResource(color);//通知栏所需颜色
		}
	}

	@TargetApi(19)
	protected void setTranslucentStatus(boolean on) {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}

	@Override
	protected void onStart() {
		super.onStart();
		//setup ic_back button listener and title
//		View ic_back = findViewById(R.id.mho_action_bar_back);
//		if (ic_back != null) {
//			ic_back.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					finish();
//				}
//			});
//			mTitle = (TextView) findViewById(R.id.mho_action_bar_title);
//			if (mTitle != null) mTitle.setText(getBarTitle());
//		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		StatService.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(this);
	}

	@Override
	protected void onStop() {
		DialogBuilder builder = DialogBuilder.getInstance(this);
		if (builder.isShowing())
			builder.dismiss();
		builder = null;
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		BaseApplication.getInstance().cancelRequest(this.getClass().getName());
		EventAgent.unregister(this);
		super.onDestroy();
	}

	public String getTag() {
		return this.getClass().getName();
	}

	private ProgressDialog mProgressDialog;

	public void showToast(final String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	/**
	 * 在屏幕中间显示toast
	 */
	public void showToastCenter(final String msg) {
		Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	@Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 1)
	public void onMessageEvent(IEvent.UnLoginEvent event) {
		switch (event.id) {
			case IEvent.CODE_UNLOGIN:
				CLog.i("", "BaseActivity receive unlogin event");
				finish();
				break;
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN, sticky = true, priority = 1)
	public void onMessageEvent(IEvent.LogoutEvent event) {
		switch (event.id) {
			case IEvent.CODE_LOGOUT:
				CLog.i("", "BaseActivity receive logout event");
				finish();
				break;
		}
	}
}
