package com.okhttp.sample_okhttp;

import com.google.gson.Gson;

import java.io.IOException;

import com.okhttp.callbacks.Callback;
import okhttp3.Response;


public abstract class UserCallback extends Callback<User>
{
    @Override
    public User parseNetworkResponse(Response response, int id) throws IOException
    {
        String string = response.body().string();
        User user = new Gson().fromJson(string, User.class);
        return user;
    }


}
