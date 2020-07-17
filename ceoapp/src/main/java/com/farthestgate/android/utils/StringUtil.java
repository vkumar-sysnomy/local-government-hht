package com.farthestgate.android.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPOutputStream;

/**
 * Created by aaronnewton on 11/03/2014.
 */
public class StringUtil {


    public static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }



    public static boolean isEmpty(CharSequence chsq) {
        boolean result;
        if(chsq == null) {
            result = true;

        } else if(chsq.length() == 0) {
            result = true;

        } else {
            result = false;
        }

        return result;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static int getInt(String str) {
        return getInt(str, 0);
    }


    public static int getInt(String str, int defaultVal) {
        int value;
        try {
            value = Integer.parseInt(str);
        } catch(NumberFormatException e) {
            value = defaultVal;
        }
        return value;
    }



    public static String compress(String str) throws IOException
    {
        if (str == null || str.length() == 0) {
            return str;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(str.getBytes());
        gzip.close();
        return out.toString("ISO-8859-1");
    }

    }
