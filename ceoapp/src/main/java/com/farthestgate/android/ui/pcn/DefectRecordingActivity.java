package com.farthestgate.android.ui.pcn;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.webkit.WebView;
import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.R;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.utils.DeviceUtils;

import org.apache.commons.net.util.Base64;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DefectRecordingActivity extends Activity {

    private WebView         webViewDefect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_defect_recording);
        webViewDefect = (WebView) findViewById(R.id.webViewDefect);
        webViewDefect.getSettings().setJavaScriptEnabled(true);
        String htmlContent = "<html></html><head></head><body><span>No current connection available.</span><br /><span>Please try later.</span></body>";
        if(!DeviceUtils.isConnected(this)){
            webViewDefect.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
        }else{
            try {
                TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
                String deviceNumber = CeoApplication.getUUID();
                String key = new SimpleDateFormat("yyyyMMddHHmmsshh").format(new Date());
                String encryptedCeo = encrypt(key, DBHelper.getCeoUserId()/*CeoApplication.CEOLoggedIn.userId*/);
                String urlParams = "?hht=" + deviceNumber + "&ceoauth=" + encryptedCeo;
                if(CeoApplication.DefectReportingUrl()!=null && !CeoApplication.DefectReportingUrl().isEmpty()){
                    webViewDefect.loadUrl(CeoApplication.DefectReportingUrl() + urlParams);
                }else{
                    String htmlContent2 = "<html></html><head></head><body><span>Defect reporting URL not found.</span><br /><span>Please contact your supervisor.</span></body>";
                    webViewDefect.loadDataWithBaseURL(null, htmlContent2, "text/html", "UTF-8", null);
                }

            }catch (Exception ex){
                webViewDefect.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
            }
        }
    }

    private String encrypt(String key, String value) throws Exception {
        byte[] raw = key.getBytes(Charset.forName("UTF-8"));
        if (raw.length != 16) {
            throw new IllegalArgumentException("Invalid key size.");
        }
        SecretKeySpec keySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec,new IvParameterSpec(new byte[16]));
        byte[] cipherText = cipher.doFinal(value.getBytes(Charset.forName("UTF-8")));
        String cipherTextStr  = Base64.encodeBase64String(cipherText);
        return cipherTextStr;
    }

    public static String encrypt(String key1, String key2, String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(key2.getBytes("UTF-8"));
            SecretKeySpec keySpec = new SecretKeySpec(key1.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String key1, String key2, String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(key2.getBytes("UTF-8"));
            SecretKeySpec keySpec = new SecretKeySpec(key1.getBytes("UTF-8"),"AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));
            return new String(original);
            /*
            String key1 = "Bar12345Bar12345"; // 128 bit key
            String key2 = "ThisIsASecretKet";
            System.out.println(decrypt(key1, key2, encrypt(key1, key2, "Hello World")));
             */
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
