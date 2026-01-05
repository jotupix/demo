package com.jtkj.library.infrastructure.network;

import androidx.collection.ArrayMap;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * 添加公共参数到Request Body
 */
class JsonBodyInterceptor implements Interceptor {
//	private static final String TAG = JsonBodyInterceptor.class.getSimpleName();

	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	private ArrayMap<String, String> mParamsMap;

	JsonBodyInterceptor(ArrayMap<String, String> params) {
		mParamsMap = params;
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request();
		if (mParamsMap == null) {
			return chain.proceed(request);
		}

		String oldBody = bodyToString(request.body());
		if (oldBody.length() > 0) {//只是为了临时兼容之前接口没按文档实现的情况 todo
			try {
				new JSONObject(oldBody);
			} catch (Exception e) {
				return chain.proceed(request);
			}
		}

		String url = request.url().toString();
		if (isMediaRequest(url)) {
			return chain.proceed(request);
		}

		//1.1添加公共参数到Body
//		RequestBody tmp = request.body();
//		if (tmp instanceof FormBody) {//基于RequestBody转换更全面
//			FormBody oldBody = (FormBody) tmp;
//			for (int i = 0; i < oldBody.size(); i++) {
//				mParamsMap.put(oldBody.encodedName(i), oldBody.encodedValue(i));
//			}
//		}
//
//		String params = new Gson().toJson(mParamsMap);
//		CLog.i(TAG, "Request Body " + params);
//		RequestBody body = RequestBody.create(JSON, params);
//		request = request.newBuilder().post(body).build();
//		return chain.proceed(request);
		//1.2添加公共参数到Body
		String params = new Gson().toJson(mParamsMap);
		RequestBody commonBody = RequestBody.create(JSON, params);
		String commonJson = bodyToString(commonBody);
		commonJson = commonJson.substring(0, commonJson.length() - 1);
		String newBody = commonJson + ((oldBody.length() > 2) ? "," + oldBody.substring(1, oldBody.length()) : "}");

		RequestBody body = RequestBody.create(JSON, newBody);
		request = request.newBuilder().post(body).build();
		return chain.proceed(request);
		//2.0添加公共参数到URL
//		HttpUrl.Builder urlBuilder = request.url().newBuilder().addQueryParameter("token", "tokenValue");
//		HttpUrl httpUrl = urlBuilder.build();
//		request = request.newBuilder().url(httpUrl).build();
//		return chain.proceed(request);
	}

	private static boolean isMediaRequest(String url) {
		String subtype = url.substring(url.lastIndexOf("."), url.length()).toLowerCase();
		return ".png".equals(subtype) || ".mpeg".equals(subtype) || ".mp4".equals(subtype) || ".jpg".equals(subtype)
				|| ".jpeg".equals(subtype) || ".webp".equals(subtype);
	}

	private static String bodyToString(final RequestBody request){
		try {
			final Buffer buffer = new Buffer();
			if(request != null)
				request.writeTo(buffer);
			else
				return "";
			return buffer.readUtf8();
		}
		catch (final IOException e) {
			return "";
		}
	}
}
