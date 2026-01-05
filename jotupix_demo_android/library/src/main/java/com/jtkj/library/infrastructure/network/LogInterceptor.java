package com.jtkj.library.infrastructure.network;


import com.jtkj.library.commom.logger.CLog;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * http://blog.csdn.net/it_talk/article/details/51734507
 */
class LogInterceptor implements Interceptor {
	private static final String TAG = "LogInterceptor";

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request();
		//the request url
		String url = request.url().toString();
		//the request method
		String method = request.method();
		long t1 = System.nanoTime();//纳秒
		CLog.d(TAG, String.format(Locale.getDefault(), "Sending %s request [url = %s]", method, url));
		//the request header
		Headers headers = request.headers();
		if (headers != null) {
			StringBuilder sb = new StringBuilder("Request Header [");
			for (int i = 0, count = headers.size(); i < count; i++) {
				String name = headers.name(i);
				if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
					sb.append(name).append(": ").append(headers.value(i)).append("; ");
				}
			}
			sb.append("]");
			CLog.d(TAG, String.format(Locale.getDefault(), "%s %s", method, sb.toString()));
		}
		//the request body
		RequestBody requestBody = request.body();
		if (requestBody != null) {
			StringBuilder sb = new StringBuilder("Request Body [");
			Buffer buffer = new Buffer();
			requestBody.writeTo(buffer);
			Charset charset = Charset.forName("UTF-8");
			MediaType contentType = requestBody.contentType();
			if (contentType != null) {
				charset = contentType.charset(charset);
			}
			if (isPlaintext(buffer)) {
				sb.append(buffer.readString(charset));
				sb.append(" (Content-Type = ").append(contentType == null ? "null" : contentType.toString()).append(",")
						.append(requestBody.contentLength()).append("-byte body)");
			} else {
				sb.append(" (Content-Type = ").append(contentType != null ? contentType.toString() : "application/json")
						.append(",binary ").append(requestBody.contentLength()).append("-byte body omitted)");
			}
			sb.append("]");
			CLog.d(TAG, String.format(Locale.getDefault(), "%s %s", method, sb.toString()));
		}

		Response response = chain.proceed(request);//发送请求
		long t2 = System.nanoTime();
		//the response time
		CLog.d(TAG, String.format(Locale.getDefault(), "Received response for [url = %s] in %.1fms", url, (t2 - t1) / 1e6d));
		//the response state
		CLog.d(TAG, String.format(Locale.CHINA, "Received response is %s, code[%d], message[%s]",
				response.isSuccessful() ? "success" : "fail", response.code(), response.message()));
		//the response data
		ResponseBody body = response.body();
		BufferedSource source = body.source();
		source.request(Long.MAX_VALUE); // Buffer the entire body.
		Buffer buffer = source.buffer();
		Charset charset = Charset.defaultCharset();
		MediaType contentType = body.contentType();
		String subtype = "json";
		if (contentType != null) {
			CLog.d(TAG, "Response Body MediaType: " + contentType.toString());
			subtype = contentType.subtype().toLowerCase();
			charset = contentType.charset(charset);
		}
		if (isPlaintext(buffer) || "json".equals(subtype)) {
			String bodyString = buffer.clone().readString(charset);
			CLog.d(TAG, String.format("Received response json string [%s]", bodyString));
		} else {
			CLog.d(TAG, String.format(Locale.getDefault(),
					"Received response [%1$s], buffer size [%2$d]", subtype, buffer.size()));
		}
		return response;
	}

	private static boolean isPlaintext(Buffer buffer) {
		try {
			Buffer prefix = new Buffer();
			long byteCount = buffer.size() < 64 ? buffer.size() : 64;
			buffer.copyTo(prefix, 0, byteCount);
			for (int i = 0; i < 16; i++) {
				if (prefix.exhausted()) {
					break;
				}
				int codePoint = prefix.readUtf8CodePoint();
				if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
					return false;
				}
			}
			return true;
		} catch (EOFException e) {
			return false; // Truncated UTF-8 sequence.
		}
	}
}
