package com.jtkj.library.infrastructure.mvp;

/**
 * Created by yfxiong on 2017/5/3.
 * Copyright  yfxiong www.jotus-tech.com. All Rights Reserved.
 */
public class IEvent {
	public int id = -1;
	public int code = -1;

	public final static int CODE_SUCCESS = 0;
	public final static int CODE_ERROR = -1;
	public final static int CODE_NETWORK = 1;
	public final static int CODE_LOGIN_SUCCESS = 2;
	public final static int CODE_UNLOGIN = -2;
	public final static int CODE_LOGOUT = -3;
	public final static int CODE_LOGOUT_FRAGMENT = -4;
	public final static int CODE_UNLOGIN_FRAGMENT = -5;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		IEvent event = (IEvent) o;

		return id == event.id;

	}

	public static class LoginSuccessEvent extends IEvent {
		public LoginSuccessEvent(int eventId) {
			id = eventId;
		}
	}

	public static class UnLoginEvent extends IEvent {
		public UnLoginEvent(int eventId) {
			id = eventId;
		}
	}

	public static class LogoutEvent extends IEvent {
		public LogoutEvent(int eventId) {
			id = eventId;
		}
	}

	public static class NetWorkEvent extends IEvent{
		public boolean mIsConnected;
		public NetWorkEvent(int eventId, boolean connected) {
			id = eventId;
			mIsConnected = connected;
		}
	}
}