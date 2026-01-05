package com.jtkj.library.infrastructure.eventbus;

import org.greenrobot.eventbus.EventBus;

public class EventAgent {
	public static void register(Object subscriber) {
		if (!EventBus.getDefault().isRegistered(subscriber)) {
			EventBus.getDefault().register(subscriber);
		}
	}

	public static void unregister(Object subscriber) {
		EventBus.getDefault().unregister(subscriber);
	}

	public static void post(Object event) {
		EventBus.getDefault().post(event);
	}

	/*public static void registerSticky(Object subscriber) {
		EventBus.getDefault().registerSticky(subscriber);
	}*/

	public static void postSticky(Object subscriber) {
		EventBus.getDefault().postSticky(subscriber);
	}

	public static void removeStickyEvent(Object event) {
		EventBus.getDefault().removeStickyEvent(event);
	}

	public static void removeAllStickyEvents() {
		EventBus.getDefault().removeAllStickyEvents();
	}
}
