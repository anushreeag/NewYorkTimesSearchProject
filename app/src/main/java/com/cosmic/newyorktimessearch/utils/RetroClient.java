package com.cosmic.newyorktimessearch.utils;

import cz.msebera.android.httpclient.HttpRequestInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by anushree on 9/23/2017.
 */

public class RetroClient {



    /*String apiKey = "YOUR-API-KEY-HERE";

    RequestInterceptor requestInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            request.addQueryParam("apikey", apiKey);
        }
    }; */

    /********
     * URLS
     *******/
    private static final String ROOT_URL = "http://api.nytimes.com";

    /**
     * Get Retrofit Instance
     */
    private static Retrofit getRetrofitInstance() {
        return new Retrofit.Builder()
                .baseUrl(ROOT_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Get API Service
     *
     * @return API Service
     */
    public static ApiService getApiService() {
        return getRetrofitInstance().create(ApiService.class);
    }
}
