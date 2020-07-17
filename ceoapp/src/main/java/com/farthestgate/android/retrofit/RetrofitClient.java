package com.farthestgate.android.retrofit;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final int CONNECT_TIMEOUT_SECS = 50;
    private static final int WRITE_TIMEOUT_SECS = 50;
    private static final int READ_TIMEOUT_SECS = 50;


    static Retrofit getClient(String baseURL) throws Exception {

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT_SECS, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT_SECS, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SECS, TimeUnit.SECONDS);

        Retrofit.Builder retrofitBuilder = new Retrofit.Builder();
        return retrofitBuilder.baseUrl(baseURL)
                .addConverterFactory(GsonStringConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();
    }
}
