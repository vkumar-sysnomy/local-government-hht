package com.farthestgate.android.api;

import android.accounts.AuthenticatorException;

import com.farthestgate.android.utils.TimeoutException;
import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hanson Aboagye on 19/04/2014.
 *
 * This class is not in use at the moment - it was an experiment to get an updated list of makes and models
 *
 */
public class ApiServices
{

    public static final String BASE_URL = "http://www.carqueryapi.com/api/0.3/?callback=?&cmd=";
    public final static int TIMEOUT_LONG = 5000;
    public final static int TIMEOUT_SHORT = 2500;


    private static ApiServices _instance;

    private final APIMethodVO METHOD_MANUFACTURER_GET = new APIMethodVO("getMakes", HttpGet.METHOD_NAME);
    private final APIMethodVO METHOD_MODELS_GET = new APIMethodVO("getModels", HttpGet.METHOD_NAME);

    private DefaultHttpClient httpClient;
    private int statusCode;
    private Gson gson = new Gson();

    public static ApiServices getInstance() {
        if (_instance == null) {
            _instance = new ApiServices();
        }
        return _instance;
    }

    public JSONArray getMakes() throws UnknownHostException, TimeoutException, JSONException, Exception {
        setDefaults();
        List<NameValuePair> params = new LinkedList<NameValuePair>();
     //   params.add(new BasicNameValuePair("sold_in_us","0"));
        HttpGet httpGet = buildGetRequest(METHOD_MANUFACTURER_GET, params);
        String responseString = callServer(httpGet, METHOD_MANUFACTURER_GET);
        responseString = responseString.replace("?(","").replace(");","");

        JSONArray res = new JSONObject(responseString).getJSONArray("Makes");

        return res;
    }

    public JSONArray getModels(String manufacturerID) throws UnknownHostException, TimeoutException, JSONException, Exception {
        setDefaults();
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair("make",manufacturerID));
        HttpGet httpGet = buildGetRequest(METHOD_MODELS_GET, params);
        String responseString = callServer(httpGet, METHOD_MODELS_GET);
        responseString = responseString.replace("?(","").replace(");","");

        JSONArray res = new JSONObject(responseString).getJSONArray("Models");

        return res;
    }

    public static HttpGet buildGetRequest(APIMethodVO method,List<NameValuePair> params) {
        String url = BASE_URL + method.methodName;
        String paramString = URLEncodedUtils.format(params, "utf-8");
        if (params.size() > 0)
            url += "&" + paramString;

        HttpGet httpGet = createBaseHttpGet(url);
        return httpGet;
    }

    public static HttpPost buildPostRequest(APIMethodVO method,
                                            List<NameValuePair> params,
                                            String deviceId) {
        String url = ApiServices.BASE_URL + method.methodName;
        String paramString = URLEncodedUtils.format(params, "utf-8");
        url += paramString;
        HttpPost httpPost = createBaseHttpPost(url);
        return httpPost;
    }

    private void setDefaults() {
        httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter("http.protocol.content-charset", HTTP.UTF_8);
      //  HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), TIMEOUT_SHORT);


        // Device tracking ???
     /*   if (TextUtils.isEmpty(deviceId)) {
            deviceId = "testDevice";
        }*/
    }

    /**
     *
     * Can add header information - possibly device ID
     *
     * @param url
     *
     * @return
     */
    private static HttpPost createBaseHttpPost(String url) {
        HttpPost httpPost = new HttpPost(url);
        return httpPost;
    }

    /**
     * Can add header information - possibly device ID
     *
     * @param url
     * @return
     */
    private static HttpGet createBaseHttpGet(String url) {
        HttpGet httpGet = new HttpGet(url);
        return httpGet;
    }

    private String callServer(HttpRequestBase httpRequest, APIMethodVO method) throws TimeoutException, UnknownHostException, Exception {
        String responseString = null;
        HttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(httpRequest);
        } catch (UnknownHostException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            if (e.getClass() == ConnectTimeoutException.class)
                throw new TimeoutException(e);
        }


        String methodName = "method unknown";
        if (method != null) {
            methodName = method.methodName;
        }

        responseString = EntityUtils.toString(httpResponse.getEntity());
        statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            ServiceErrorVO serviceError = gson.fromJson(responseString, ServiceErrorVO.class);
            if (statusCode == HttpStatus.SC_UNAUTHORIZED || statusCode == HttpStatus.SC_BAD_REQUEST) {
                throw new AuthenticatorException(serviceError.error);
            } else {
                throw new Exception(serviceError.error);
            }
        }
        return responseString;
    }


    public static class APIMethodVO {
        public String methodName;
        public String httpMethod;

        public APIMethodVO(String _methodName, String _httpMethod) {
            methodName = _methodName;
            httpMethod = _httpMethod;
        }
    }

    /**
     *  This is needed for a unified error management process
     */
    public static class ServiceErrorVO extends Throwable {
        public int code;
        public String error;
        public String error_description;
    }

}
