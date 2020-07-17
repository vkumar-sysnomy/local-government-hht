package com.farthestgate.android.helper;

import android.os.Environment;

import com.farthestgate.android.utils.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hanson on 06/05/2014.
 */
public class ResourceHelper {

    private static final String MAKES_FILE = Environment.getExternalStorageDirectory() + "/ceoappdata/configdata/vehicles/vehicle_makes.json";
    private static final String MODELS_DIR = Environment.getExternalStorageDirectory() + "/ceoappdata/configdata/vehicles/models/";


    public static List<String> getVehicleMakes() throws FileNotFoundException {
        ArrayList<String> makeList = new ArrayList<String>();

        try {
            InputStream fileIS = new FileInputStream(new File(MAKES_FILE));
            JSONArray makeJSON = new JSONObject(StringUtil.getStringFromInputStream(fileIS)).getJSONArray("vehicleMakes");

            for (int indx =0;indx < makeJSON.length(); indx++)
            {
                makeList.add(makeJSON.getString(indx));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return makeList;
    }

    public static List<String> getManufacturerModels(String manufacturer) throws FileNotFoundException {
        ArrayList<String> modelList = new ArrayList<String>();

        try {
            String newPath = MODELS_DIR + manufacturer + ".json";
            InputStream fileIS = new FileInputStream(new File(newPath));
            JSONArray makeJSON = new JSONObject(StringUtil.getStringFromInputStream(fileIS)).getJSONArray("vehicleModel");

            for (int indx =0;indx < makeJSON.length(); indx++)
            {
                modelList.add(makeJSON.getString(indx));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return modelList;
    }


}
