package com.arnis.neuronnet.Retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by arnis on 03/05/16.
 */
public class Data {

    private static String baseURL = "yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22AAPL%22%20and%20startDate%20%3D%20%222015-09-22%22%20and%20endDate%20%3D%20%222016-09-10%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
//    private final String line1 = "yql?q=select%20*%20from%20yahoo.finance.xchange%20where%20pair%20in%20(%22";

//    public String getCURRENCY() {
//        return CURRENCY;
//    }
//
//    public void updateCURRENCY(String curr) {
//        this.CURRENCY = this.CURRENCY+ "," + curr;
//    }
//
//    public void setCURRENCY(String CURRENCY) {
//        this.CURRENCY = CURRENCY;
//    }
//
//    private String CURRENCY="EURRUB,USDRUB";//no semicolon!!!!!
//    private final String line2 = "%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
//    private String requestURL;

    public static Call<Results> get(int type) {

        switch (type){
            case 0: baseURL ="yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22AAPL%22%20and%20startDate%20%3D%20%222015-09-22%22%20and%20endDate%20%3D%20%222016-09-22%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";break;
            case 1: baseURL ="yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22AAPL%22%20and%20startDate%20%3D%20%222015-04-01%22%20and%20endDate%20%3D%20%222015-05-01%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";break;
            case 2: baseURL = "yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22AAPL%22%20and%20startDate%20%3D%20%222016-01-01%22%20and%20endDate%20%3D%20%222016-02-01%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";break;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .build();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Results.class , new MyDeserializer())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://query.yahooapis.com/v1/public/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        API api = retrofit.create(API.class);

        return api.getList(baseURL);
    }

}

