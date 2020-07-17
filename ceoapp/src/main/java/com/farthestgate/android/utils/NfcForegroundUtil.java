package com.farthestgate.android.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;

/**
 * Created by aaronnewton on 26/03/2014.
 */
public class NfcForegroundUtil {
//    private static final String TECH_LIST_ARRAY[][] = new String[][] {
//                                  new String[] { NfcA.class.getName(), Ndef.class.getName(), MifareUltralight.class.getName() },
//                                  new String[] { NfcA.class.getName(), IsoDep.class.getName(), NdefFormatable.class.getName() },
//                                  new String[] { NfcA.class.getName() } };

    private static final String TECH_LIST_ARRAY[][] = new String[][] { new String[] { Ndef.class.getName() } };
    private Activity activity = null;
    private NfcAdapter nfc = null;
    private IntentFilter intentFiltersArray[] = null;
    private PendingIntent intent = null;

    public NfcForegroundUtil(Activity activity) {
        super();
        this.activity = activity;
        nfc = NfcAdapter.getDefaultAdapter(activity.getApplicationContext());
        intent = PendingIntent.getActivity(activity, 0, new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException ex) {
            throw new RuntimeException("Unable to specify */* Mime Type", ex);
        }
        intentFiltersArray = new IntentFilter[] { ndef };
    }

    public void enableForeground() {
        nfc.enableForegroundDispatch(activity, intent, intentFiltersArray, NfcForegroundUtil.TECH_LIST_ARRAY);
    }

    public void disableForeground() {
        nfc.disableForegroundDispatch(activity);
    }

    public NfcAdapter getNfc() {
        return nfc;
    }
}
