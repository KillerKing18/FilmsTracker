package com.example.filmstracker.webservices;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RequestInterceptor implements Interceptor {
    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request originalRequest = chain.request();
        HttpUrl originalHttpUrl = originalRequest.url();

        HttpUrl url = originalHttpUrl.newBuilder()
                .addQueryParameter("apikey", Repository.API_KEY)
                .build();

        Request request = originalRequest.newBuilder()
                .url(url)
                .build();

        return chain.proceed(request);
    }
}
