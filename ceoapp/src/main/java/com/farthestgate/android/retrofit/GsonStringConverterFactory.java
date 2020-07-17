package com.farthestgate.android.retrofit;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;
import retrofit2.Retrofit;


public class GsonStringConverterFactory extends Converter.Factory {
    private static final MediaType MEDIA_TYPE = MediaType.parse("text/plain");
    private final Gson gson;

    private GsonStringConverterFactory(Gson gson) {
        this.gson = gson;
    }

    public static GsonStringConverterFactory create() {
        return create(new Gson());
    }

    public static GsonStringConverterFactory create(Gson gson) {
        if (gson == null) {
            throw new NullPointerException("gson == null");
        } else {
            return new GsonStringConverterFactory(gson);
        }
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        if (String.class.equals(type)) {
            return new Converter<String, RequestBody>() {
                @Override
                public RequestBody convert(String value) throws IOException {
                    return RequestBody.create(MEDIA_TYPE, value);
                }
            };
        }
        return null;
    }
}
