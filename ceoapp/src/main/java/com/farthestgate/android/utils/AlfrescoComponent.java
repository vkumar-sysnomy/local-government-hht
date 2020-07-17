package com.farthestgate.android.utils;

import android.net.http.AndroidHttpClient;
import android.util.Log;

import com.farthestgate.android.CeoApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class AlfrescoComponent {

    /*private static HttpHost InitializeHost(DefaultHttpClient httpClient) {
       HttpHost targetHost = new HttpHost(CeoApplication.SiteHostName(), CeoApplication.SitePort(), CeoApplication.SiteScheme());
        AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(CeoApplication.SiteUser(), CeoApplication.SitePassword());
        CredentialsProvider provider = httpClient.getCredentialsProvider();
        provider.setCredentials(authScope, credentials);
        return targetHost;
    }*/
	
	public static String executeGetRequest(String completeUrl) {
        String response = null;
        try {
            HttpGet httpGet = new HttpGet(completeUrl);
            HttpParams httpParameters = new BasicHttpParams();
            if (CeoApplication.isVrmLook){
                HttpConnectionParams.setConnectionTimeout(httpParameters, Integer.parseInt(CeoApplication.getSingleviewTimeout()));
            }
            DefaultHttpClient httpClient;
            if (completeUrl.contains("https")) {
                httpClient = (DefaultHttpClient) CreateHttpClient();
            } else {
                httpClient = new DefaultHttpClient();
            }
            if (CeoApplication.isVrmLook) {
                httpClient.setParams(httpParameters);
            }
            HttpResponse httpResponse = httpClient.execute(httpGet);

            /*HttpEntity httpEntity = httpResponse.getEntity();
            if(httpEntity !=null){
                response = EntityUtils.toString(httpEntity);
            }*/
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String encoding = EntityUtils.getContentCharSet(httpResponse.getEntity());
                encoding = encoding == null ? "UTF-8" : encoding;
                InputStream stream = AndroidHttpClient.getUngzippedContent(httpResponse.getEntity());
                InputStreamEntity unzEntity = new InputStreamEntity(stream, -1);
                response = EntityUtils.toString(unzEntity, encoding);
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        }catch (ConnectTimeoutException e)
        {
            CeoApplication.isTimeOutException = true;
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
    }
    return response;
    }
	/*public static String executePostRequest(String url, HttpEntity httpEntity) {
        String response = null;
        try {
            String baseURL = CeoApplication.SiteScheme() + "://" + CeoApplication.SiteHostName() + ":" + CeoApplication.SitePort();
            String completeUrl = baseURL + url;
            HttpPost httpPost = new HttpPost(completeUrl);
            httpPost.setEntity(httpEntity);
            DefaultHttpClient httpClient;
            if (baseURL.contains("https")) {
                httpClient = (DefaultHttpClient) CreateHttpClient();
            } else {
                httpClient = new DefaultHttpClient();
            }
            HttpHost targetHost = InitializeHost(httpClient);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            response = httpClient.execute(targetHost, httpPost, responseHandler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }*/
    private static class CustomSSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public CustomSSLSocketFactory(KeyStore trustStore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(trustStore);
            TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

    public static HttpClient CreateHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory sf = new CustomSSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));
            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }
}
