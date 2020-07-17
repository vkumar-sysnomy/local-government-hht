package com.farthestgate.android.retrofit;

import android.content.Context;

import android.util.Log;

import androidx.annotation.Nullable;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.model.GeneralResponse;

import java.io.File;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class RetrofitRequest {
    //public static final String BASE_URL = "http://82.148.231.170:8023/rest/hht/direct/ticket/coreservice/";
    private static RequestInterface retrofitInterface;
    @Nullable
    static RequestInterface getApiRequest() {
        if (retrofitInterface == null)
            try {
                retrofitInterface = RetrofitClient.getClient(CeoApplication.getMultipartUploadBaseURL())
                        .create(RequestInterface.class);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("app", "Unsupported Client Requested! [RetrofitClient.class]");
            }
        return retrofitInterface;
    }

    public static class Async {
        public static void uploadFile(Context context, HashMap<String, String> map,
                                      RetrofitCallback<GeneralResponse> cb, File file) {
            try {
                /*MediaType MEDIA_TYPE_PNG = MediaType.parse("image/jpg");
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", file.getName(),
                                RequestBody.create(MEDIA_TYPE_PNG, file))
                        .addFormDataPart("ceoId", DBHelper.getCeoUserId())
                        .addFormDataPart("deviceID", CeoApplication.getUUID())
                        .addFormDataPart("ticketType", "PCN")

                        .build();*/

                RequestBody requestFile =
                        RequestBody.create(MediaType.parse("multipart/form-data"), file);
               // MultipartBody.Part is used to send also the actual file name
                MultipartBody.Part body =
                        MultipartBody.Part.createFormData("file", file.getName(), requestFile);

                RequestBody ceoID =
                        RequestBody.create(MediaType.parse("multipart/form-data"), DBHelper.getCeoUserId());
                RequestBody deviceID =
                        RequestBody.create(MediaType.parse("multipart/form-data"), CeoApplication.getUUID());
                RequestBody ticketType =
                        RequestBody.create(MediaType.parse("multipart/form-data"), "PCN");

                //TODO: I will send PCN number later because not require now
                //.addFormDataPart("pcnNumber", "#PCN")
                Call<GeneralResponse> call = getApiRequest().uploadImage(ceoID,deviceID,ticketType,body);

                call.enqueue(cb);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
