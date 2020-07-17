package com.farthestgate.android.retrofit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;


import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.farthestgate.android.R;
import com.farthestgate.android.model.GeneralResponse;
import com.farthestgate.android.utils.CommonUtils;

import java.io.IOException;
import java.net.UnknownHostException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;




public abstract class RetrofitCallback<T extends GeneralResponse> implements Callback<T> {

    protected Context context;
    protected ProgressDialog progressDialog;
    private int noInternetErrorId, noInternetToastLength = Toast.LENGTH_SHORT;
    private String noInternetError;
    private boolean showNoInternetMessage;

    public RetrofitCallback(Context context) {
        this(context, "", false);
    }

    public RetrofitCallback(Context context, int progressDialogMessageId, boolean showNoInternetMessage) {
        this(context,
                progressDialogMessageId == 0 ? "" : context.getString(progressDialogMessageId),
                showNoInternetMessage);
    }

    public RetrofitCallback(Context context, String progressDialogMessage, boolean showNoInternetMessage) {
        this(context,
                (context instanceof Activity && !((Activity) context).isFinishing() && !TextUtils.isEmpty(progressDialogMessage)) ?
                        ProgressDialog.show(context, "", progressDialogMessage, true) : null,
                showNoInternetMessage);
    }

    public RetrofitCallback(Context context, ProgressDialog progressDialog) {
        this(context, progressDialog, false);
    }

    private RetrofitCallback(Context context, ProgressDialog progressDialog, boolean showNoInternetMessage) {
        this.context = context;
        this.progressDialog = progressDialog;
        this.showNoInternetMessage = showNoInternetMessage;

        if (progressDialog != null
                && !progressDialog.isShowing()
                && (context instanceof Activity && !((Activity) context).isFinishing())) {
            progressDialog.show();
        }
    }

    public RetrofitCallback<T> setNoInternetErrorId(int noInternetErrorId) {
        this.noInternetErrorId = noInternetErrorId;
        return this;
    }

    public RetrofitCallback<T> setNoInternetError(String noInternetError) {
        this.noInternetError = noInternetError;
        return this;
    }

    public RetrofitCallback<T> setNoInternetToastLength(int toastLength) {
        this.noInternetToastLength = toastLength;
        return this;
    }

    @Override
    public void onResponse( Call<T> call,  Response<T> response) {

        T body = response.body();
        if (body != null) {
            if (body.isSuccess() && response.isSuccessful()) {
                onApiSuccess(body);
            } else {

                if (progressDialog != null) {
                    try {
                        progressDialog.dismiss();
                    } catch (IllegalArgumentException | IllegalStateException ignored) {
                    }
                }
               else {
                    onApiFailure(response);
                }
            }
        } else {
            Retrofit2_Util.throwNullBody();
            onApiFailure(response);
        }
        if (progressDialog != null) {
            try {
                progressDialog.dismiss();
            } catch (IllegalArgumentException | IllegalStateException ignored) {
            }
        }
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable throwable) {

        throwable.printStackTrace();
        try {
            if (progressDialog != null && progressDialog.isShowing() && context instanceof Activity && !(((Activity) context).isFinishing())) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (showNoInternetMessage) {
            if (throwable.getCause() instanceof UnknownHostException) {
                onInternetNotAvailable();
            } else {
                if (context != null) {
                    CommonUtils.showToast(context, context.getString(R.string.no_internet_connection),
                            noInternetToastLength);
                }
            }
        }
    }

    /**
     * Use isNetworkError(Throwable t) instead
     *
     * @param throwable
     * @return ERROR_KIND.NETWORK or null
     */
    @Deprecated
    protected ERROR_KIND getKind(Throwable throwable) {
        if (throwable instanceof IOException)
            return ERROR_KIND.NETWORK;
        return null;
    }

    protected boolean isHTTPError(Response<T> response) {
        return response.code() > 200;
    }

    protected boolean isNetworkError(Throwable throwable) {
        return throwable instanceof IOException;
    }

    protected abstract void onApiFailure(@Nullable Response<T> response);

    protected abstract void onApiSuccess(@NonNull T response);

    private void onInternetNotAvailable() {
        CommonUtils.showToast(context, getNoInternetMessage(), noInternetToastLength);
    }

    private String getNoInternetMessage() {
        if (context == null) {
            return "";
        }
        return TextUtils.isEmpty(noInternetError) ? context.getString(getNoInternetMessageId()) : noInternetError;
    }

    private int getNoInternetMessageId() {
        return noInternetErrorId == 0 ? R.string.no_internet_connection : noInternetErrorId;
    }

    public enum ERROR_KIND {
        HTTP,
        NETWORK,
        UNEXPECTED,
        CONVERSION
    }
}
