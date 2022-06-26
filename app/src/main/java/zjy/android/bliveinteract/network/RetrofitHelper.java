package zjy.android.bliveinteract.network;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitHelper {

    private static OkHttpClient client;

    public static Api createApi() {
        return new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.live.bilibili.com/")
                .client(client())
                .build()
                .create(Api.class);
    }

    public static Api createUserApi() {
        return new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(NewGsonConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://tenapi.cn/")
                .client(client())
                .build()
                .create(Api.class);
    }

    private static OkHttpClient client() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .addInterceptor(new HttpLoggingInterceptor(message -> Log.e("RetrofitHelper",
                            "log: " + message)).setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build();
        }
        return client;
    }

}
