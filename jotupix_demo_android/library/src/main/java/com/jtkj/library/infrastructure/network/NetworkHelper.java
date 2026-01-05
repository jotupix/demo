package com.jtkj.library.infrastructure.network;

import android.content.Context;
import androidx.collection.ArrayMap;
import android.widget.Toast;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.jtkj.library.Constants;
import com.jtkj.library.R;
import com.jtkj.library.commom.logger.CLog;
import com.jtkj.library.commom.tools.NetworkUtils;
import com.jtkj.library.infrastructure.network.entity.BaseRequest;
import com.jtkj.library.infrastructure.network.exception.RetryException;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class NetworkHelper {
	private final static String TAG = "NetworkHelper";

	/**请求超时时长*/
	private final static int DEFAULT_TIMEOUT = 10;
	/**并发连接数*/
	private final static int MAX_IDLE_CONNECTIONS = 3;
	/**每个连接保持时间*/
	private final static long KEEP_ALIVE_DURATION = 10;
	/**缓存大小*/
	private final static int CACHE_SIZE = 100 * 1024 * 1024;

//	private static final int MAX_HEAP_SIZE = (int) Runtime.getRuntime().maxMemory();
//	private static final int MAX_MEMORY_CACHE_SIZE = MAX_HEAP_SIZE / 8;
//	private static final int MAX_DISK_CACHE_SIZE = 50 * ByteConstants.MB;

	private volatile static NetworkHelper mINSTANCE;

	private Retrofit mRetrofit;
//	private BaseService httpService;
	private OkHttpClient.Builder builder;
	private String mBaseUrl;
	private ArrayMap<String, String> mCommonParams;

	private NetworkHelper(Context ctx, String baseUrl, ArrayMap<String, String> commonParams) {
		mBaseUrl = baseUrl;
		mCommonParams = commonParams;
		OkHttpClient.Builder builder = new OkHttpClient.Builder()
				.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
				.writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
				.readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
				.retryOnConnectionFailure(true)
//				.cookieJar(new CustomCookieManger(ctx))
				.connectionPool(new ConnectionPool(MAX_IDLE_CONNECTIONS, KEEP_ALIVE_DURATION, TimeUnit.SECONDS));

		try {
			File directory = new File(ctx.getCacheDir(), ctx.getPackageName());
			Cache cache = new Cache(directory, CACHE_SIZE);
			builder.cache(cache);
		} catch (Exception e) {
			CLog.e(TAG, "Could not create http cache", e);
		}

		builder.addInterceptor(new JsonBodyInterceptor(commonParams));
		if (Constants.DEBUG_TOGGLE) {
			builder.addNetworkInterceptor(new LogInterceptor());
			builder.addNetworkInterceptor(new StethoInterceptor());
		}

		OkHttpClient okHttpClient = builder.build();

		mRetrofit = new Retrofit.Builder()
				.client(okHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.baseUrl(baseUrl)
				.build();
	}

	public static NetworkHelper getInstance(Context ctx, String baseUrl, ArrayMap<String, String> commonParams) {
		if (mINSTANCE == null) {
			synchronized (NetworkHelper.class) {
				if (mINSTANCE == null) {
					mINSTANCE = new NetworkHelper(ctx, baseUrl, commonParams);
				}
			}
		}
		return mINSTANCE;
	}

	public static Object createService(final Class service) {
		return mINSTANCE.mRetrofit.create(service);
	}

	public static Object createService(final Class service, int timeout) {
		OkHttpClient.Builder builder = new OkHttpClient.Builder()
				.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
				.writeTimeout(timeout, TimeUnit.SECONDS)
				.readTimeout(timeout, TimeUnit.SECONDS)
				.retryOnConnectionFailure(true)
				.connectionPool(new ConnectionPool(MAX_IDLE_CONNECTIONS, KEEP_ALIVE_DURATION, TimeUnit.SECONDS));

		// TODO: 2017/4/21 set cache

		builder.addInterceptor(new JsonBodyInterceptor(mINSTANCE.mCommonParams));
		if (Constants.DEBUG_TOGGLE) {
			builder.addNetworkInterceptor(new LogInterceptor());
			builder.addNetworkInterceptor(new StethoInterceptor());
		}

		OkHttpClient okHttpClient = builder.build();
		Retrofit retrofit = new Retrofit.Builder()
				.client(okHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.baseUrl(mINSTANCE.mBaseUrl)
				.build();
		return retrofit.create(service);
	}

	/**
	 * 处理http请求
	 *
	 * @param entity 封装的请求数据
	 */
//	@SuppressWarnings("unchecked")
//	public void doHttpDeal(BaseRequest<? extends BaseRequest> entity) {
//		Observable observable = entity.getObservable(createService(entity.getObservable()))
//				.compose(entity.getRxAppCompatActivity().bindToLifecycle())//生命周期管理
//				.observeOn(AndroidSchedulers.mainThread())//回调线程
//				.subscribeOn(Schedulers.io())//http请求线程
//				.unsubscribeOn(Schedulers.io())//http请求线程
//				.retryWhen(new RetryException())//失败后的retry配置
//				.map(entity);//结果判断
//		observable.subscribe(entity.getSubscriber());//数据回调
//	}
	@SuppressWarnings("unchecked")
	public static void execute(BaseRequest entity) {
		if (!NetworkUtils.isNetworkAvailable(entity.getRxAppCompatActivity())) {
			Toast.makeText(entity.getRxAppCompatActivity(), R.string.check_network, Toast.LENGTH_SHORT).show();
			return;
		}
		Observable observable = entity.getObservable()
				.compose(entity.getRxAppCompatActivity().bindToLifecycle())//生命周期管理
				.observeOn(AndroidSchedulers.mainThread())//回调线程
				.subscribeOn(Schedulers.io())//http请求线程
				.unsubscribeOn(Schedulers.io())//http请求线程
				.retryWhen(new RetryException())//失败后的retry配置
				.map(entity);//结果判断
		observable.subscribe(entity.getSubscriber());//数据回调
	}
}
