package com.farthestgate.android.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aaronnewton on 10/03/2014.
 */

public class SharedPreferenceHelper {
    public static final String SHARED_PREFERENCE_FILE = "app_shared_preferences";
    public static final String START_OF_DAY = "StartOfDay";

    private SharedPreferences sharedPreferences;
    private final GsonBuilder gsonBuilder;
    private final Map<String, Object> cacheMap;

    public SharedPreferenceHelper(Context context){
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
        gsonBuilder = new GsonBuilder();

//        gsonBuilder.registerTypeAdapter(Map.class,
//                new ConnectionProfileConfigKeySerializer());
//        gsonBuilder.registerTypeAdapter(Map.class,
//                new ConnectionProfileConfigKeyDeserializer());
        cacheMap = new HashMap<String, Object>();
    }

    public void saveValue(String key, Object value){
        if(value == null){
            if(sharedPreferences.contains(key)){
                sharedPreferences.edit().remove(key).commit();
                if(isInCache(key)){
                    cacheMap.remove(key);
                }
            }
            return;
        }

        if(Boolean.class.isAssignableFrom(value.getClass())
                || boolean.class.isAssignableFrom(value.getClass())){
            sharedPreferences.edit().putBoolean(key, (Boolean) value).commit();
        }
        else if(Float.class.isAssignableFrom(value.getClass())
                || float.class.isAssignableFrom(value.getClass())){
            sharedPreferences.edit().putFloat(key, (Float) value).commit();
        }
        else if(Long.class.isAssignableFrom(value.getClass())
                || long.class.isAssignableFrom(value.getClass())){
            sharedPreferences.edit().putLong(key, (Long) value).commit();
        }
        else if(Integer.class.isAssignableFrom(value.getClass())
                || int.class.isAssignableFrom(value.getClass())){
            sharedPreferences.edit().putInt(key, (Integer) value).commit();
        }
        else if(String.class.isAssignableFrom(value.getClass())){
            sharedPreferences.edit().putString(key, (String) value).commit();
        }
        else if(Serializable.class.isAssignableFrom(value.getClass())){
            sharedPreferences.edit()
                    .putString(key, getSerializableAsString((Serializable) value))
                    .commit();
        }

        saveToCache(key, value);
    }

    @SuppressWarnings("unchecked")
    public <O> O getValue(String key,Class<O> objectClass, O defaultValue) {
        assert objectClass != null;

        if(isInCache(key)){
            return getFromCache(key);
        }

        if(sharedPreferences.contains(key)){
            O value = null;

            if(Boolean.class.isAssignableFrom(objectClass)
                    || boolean.class.isAssignableFrom(objectClass)){
                value = (O) Boolean.valueOf(sharedPreferences.getBoolean(key,
                        (Boolean) defaultValue));
            }
            else if(Float.class.isAssignableFrom(objectClass)
                    || float.class.isAssignableFrom(objectClass)){
                value = (O) Float.valueOf(sharedPreferences.getFloat(key, (Float) defaultValue));
            }
            else if(Long.class.isAssignableFrom(objectClass)
                    || long.class.isAssignableFrom(objectClass)){
                value = (O) Long.valueOf(sharedPreferences.getLong(key, (Long) defaultValue));
            }
            else if(Integer.class.isAssignableFrom(objectClass)
                    || int.class.isAssignableFrom(objectClass)){
                value = (O) Integer.valueOf(sharedPreferences.getInt(key, (Integer) defaultValue));
            }
            else if(String.class.isAssignableFrom(objectClass)){
                value = (O) sharedPreferences.getString(key, (String) defaultValue);
            }
            else if(Serializable.class.isAssignableFrom(objectClass)){
                value = getSerializable(key, objectClass);
            }

            saveToCache(key, value);
            return value;
        }

        return defaultValue;
    }

    private void saveToCache(String key, Object value){
        cacheMap.put(key, value);
    }

    private boolean isInCache(String key){
        return cacheMap.containsKey(key);
    }

    private <O> O getFromCache(String key){
        return (O) cacheMap.get(key);
    }

    private <O> O getSerializable(String key, Class<O> objectClass){
        String jsonString = getValue(key, String.class, null);
        return jsonString == null ? null : gsonBuilder.create().fromJson(jsonString, objectClass);
    }

    private String getSerializableAsString(Serializable value){
        return value == null ? null : gsonBuilder.create().toJson(value);
    }

    public void clearSharedPrefs(Context ctx)
    {
        sharedPreferences.edit().clear().apply();
    }
    public void clearSharedPrefs(Context context, String prefName, int mode){
        sharedPreferences = context.getSharedPreferences(prefName, mode);
        sharedPreferences.edit().clear().commit();
    }

    public void saveObject(Context context, String key, Object object){
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        String json = new Gson().toJson(object);
        mEditor.putString(key, json);
        mEditor.commit();
    }

    public <T> T getObject(Context context, String key, Class<T> objectClass){
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(key, null);
        return new Gson().fromJson(json, objectClass);
    }

    /**
     * Save a string into shared preference
     *
     * @param key   The name of the preference to modify
     * @param value The new value for the preference
     */
    public void saveString(String key, String value) {
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.putString(key, value);
        mEditor.commit();
    }

    public void saveLong(String key, long value) {
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.putLong(key, value);
        mEditor.commit();
    }

    public void saveBoolean(String key, Boolean value)
    {
        SharedPreferences.Editor mEditor = sharedPreferences.edit();
        mEditor.putBoolean(key,value);
        mEditor.commit();
    }

    /**
     * Retrieve a String value from the preferences.
     *
     * @param key          The name of the preference to retrieve.
     * @param defaultValue Value to return if this preference does not exist.
     * @return Returns the preference value if it exists, or defaultValue.
     * Throws ClassCastException if there is a preference with this name that is not a String.
     */
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public long getLong(String key) {
        return sharedPreferences.getLong(key,0);
    }
    public boolean getBoolan(String key) {
        return sharedPreferences.getBoolean(key,false);
    }


}
