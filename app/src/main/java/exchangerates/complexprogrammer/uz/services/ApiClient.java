package exchangerates.complexprogrammer.uz.services;

import android.content.Context;

import java.io.File;
import java.util.concurrent.TimeUnit;

import exchangerates.complexprogrammer.uz.BuildConfig;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public static final String BASE_URL = "https://complexprogrammer.uz/";
    public static final String FLAG_URL = BASE_URL + "static/flag_image/%s.svg";
    private static final String CBU_URL = "https://cbu.uz/";

    private static OkHttpClient httpClient;
    private static ApiInterface api;
    private static CbuApi cbuApi;

    /** Shared client with one connection pool and an HTTP cache. */
    public static synchronized OkHttpClient getHttpClient(Context context) {
        if (httpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .cache(new Cache(new File(context.getApplicationContext().getCacheDir(), "http"), 5 * 1024 * 1024))
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS);
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
                builder.addInterceptor(logging);
            }
            httpClient = builder.build();
        }
        return httpClient;
    }

    public static synchronized ApiInterface getInterface(Context context) {
        if (api == null) {
            api = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getHttpClient(context))
                    .build()
                    .create(ApiInterface.class);
        }
        return api;
    }

    /** Central Bank archive API, used for the rate history chart. */
    public static synchronized CbuApi getCbu(Context context) {
        if (cbuApi == null) {
            cbuApi = new Retrofit.Builder()
                    .baseUrl(CBU_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getHttpClient(context))
                    .build()
                    .create(CbuApi.class);
        }
        return cbuApi;
    }
}
