package com.jtkj.library.infrastructure.network;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class HeaderInterceptor implements Interceptor {
	private Map<String, String> mHeaders;

	public HeaderInterceptor(Map<String, String> headers) {
		mHeaders = headers;
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		if (mHeaders == null || mHeaders.size() == 0)
			return chain.proceed(chain.request());

		Request.Builder builder = chain.request().newBuilder();
		Set<String> keys = mHeaders.keySet();
		for (String key : keys) {
			builder.addHeader(key, mHeaders.get(key)).build();
		}
		return chain.proceed(builder.build());
	}
}
