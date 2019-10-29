package com.okhttp.sample_okhttp;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import com.okhttp.callbacks.Callback;
import okhttp3.Response;


public abstract class ListUserCallback extends Callback<List<User>>
{

    @Override
    public List<User> parseNetworkResponse(Response response, int id) throws IOException
    {
        String string = response.body().string();
        List<User> user = new Gson().fromJson(string, List.class);
        return user;
    }


}
