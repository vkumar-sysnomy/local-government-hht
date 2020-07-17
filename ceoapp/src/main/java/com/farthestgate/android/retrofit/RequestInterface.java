package com.farthestgate.android.retrofit;

import com.farthestgate.android.model.GeneralResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface RequestInterface {
    @Multipart
    @POST("hhtImageUpload/")
    Call<GeneralResponse> uploadImage(@Part("ceoId") RequestBody id,
                                      @Part("deviceID") RequestBody deviceID,
                                      @Part("ticketType") RequestBody ticketType,
                                      @Part MultipartBody.Part image);
}
