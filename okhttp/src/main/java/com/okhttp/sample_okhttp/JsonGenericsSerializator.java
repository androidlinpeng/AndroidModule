package com.okhttp.sample_okhttp;


import com.google.gson.Gson;

import com.okhttp.callbacks.IGenericsSerializator;


public class JsonGenericsSerializator implements IGenericsSerializator {
    Gson mGson = new Gson();
    @Override
    public <T> T transform(String response, Class<T> classOfT) {
        return mGson.fromJson(response, classOfT);
    }
}
