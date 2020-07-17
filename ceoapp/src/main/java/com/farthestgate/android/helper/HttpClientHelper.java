package com.farthestgate.android.helper;

import android.util.Log;

import com.farthestgate.android.CeoApplication;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jitendra on 03/04/15.
 */
public class HttpClientHelper {
    private static final String PAY_BY_PHONE_GET_URL ="https://lambeth.verrus.com/ParkEnforce/default.aspx?user=%s&password=%s&method=GetPaidAtWaveNumber&WaveNumber=%s";
    private static List<String> convertInputStreamToStringList(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        List<String> results = new ArrayList<String>();
        while ((line = bufferedReader.readLine()) != null)
            results.add(line);
        inputStream.close();
        return results;
    }
    public static List<String> doGet(String verrusCode){
        InputStream inputStream = null;
        List<String> results = new ArrayList<String>();
        String url = String.format(PAY_BY_PHONE_GET_URL, CeoApplication.PayByPhoneUserId(),CeoApplication.PayByPhoneUserPassword(),verrusCode);
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            inputStream = httpResponse.getEntity().getContent();
            if(inputStream != null) {
                results = convertInputStreamToStringList(inputStream);
            }

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return results;
    }
}
