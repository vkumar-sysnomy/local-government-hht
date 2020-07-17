package com.farthestgate.android.utils;

import android.content.Context;
import android.widget.Toast;

public class CommonUtils {
    private static Toast toast;
    public static void showToast(Context context, String toastMessage, int toastDuration) {
        try {
            if (toast != null) {
                toast.cancel();
            }
            toast = Toast.makeText(context, toastMessage, toastDuration);
            toast.show();
        } catch (IllegalStateException | NullPointerException e) {
           e.printStackTrace();
        }
    }
}
