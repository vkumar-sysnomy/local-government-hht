package com.farthestgate.android.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class GeneralResponse  implements Serializable {


    @SerializedName("isSuccess")
    private boolean isSuccess;

    @SerializedName("msg")
    private String message;

    @SerializedName("error")
    private String Error;


    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return Error;
    }

    public void setError(String error) {
        Error = error;
    }

    @Override
    public String toString() {
        return "GeneralResponse{" +
                "isSuccess=" + isSuccess +
                ", message='" + message + '\'' +
                ", Error='" + Error + '\'' +
                '}';
    }
}
