package com.farthestgate.android.retrofit;



import androidx.annotation.Nullable;

import com.farthestgate.android.model.GeneralResponse;

import retrofit2.Response;



public final class Retrofit2_Util<T extends GeneralResponse> {

    private static Retrofit2_Util instance = new Retrofit2_Util();

    private Retrofit2_Util() {

    }

    public static Retrofit2_Util getInstance() {
        return instance;
    }

    public static void throwNullBody() {
        try {
            throw new Exception() {
                @Override
                public String getMessage() {
                    return "Response body null!!";
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void throwUnpreparedClient() {

    }

    public String codeError(@Nullable Response<T> response) {
        String go = "Network Failure!";
        GeneralResponse innerResp = null;
        if (response != null) {
            if (response.body() instanceof GeneralResponse)
                innerResp = response.body();
            if (innerResp != null)
                go = innerResp.getError();
            else if (response.code() >= 300 && response.code() < 310)
                go = "Resource Removed";
            else if (response.code() >= 400 && response.code() < 410)
                go = "Resource Not Found";
            else if (response.code() >= 500 && response.code() < 510)
                go = "Server Error";
        }
        return go;
    }
}
