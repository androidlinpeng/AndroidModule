package com.okhttp.callbacks;


public interface IGenericsSerializator {
    <T> T transform(String response, Class<T> classOfT);
}
