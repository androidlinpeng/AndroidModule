package com.okhttp.Builder;


import com.okhttp.request.OtherRequest;
import com.okhttp.request.RequestCall;
import com.okhttp.utils.OkHttpUtils;


public class HeadBuilder extends GetBuilder
{
    @Override
    public RequestCall build()
    {
        return new OtherRequest(null, null, OkHttpUtils.METHOD.HEAD, url, tag, params, headers,id).build();
    }
}
