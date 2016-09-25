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

    private final static String part1 = "yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22";
    private static String symbol = "AAPL";
    private final static String part2 = "%22%20and%20startDate%20%3D%20%22";
    private static String startDate = "2015-09-22";
    private static final String part3 = "%22%20and%20endDate%20%3D%20%22";
    private static String endDate = "2016-09-10";
    private static final String part4 = "%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=";
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

    private static String requestURL;

    public static Call<Results> get(String symbol,String from,String to) {

        Data.symbol = symbol;
        Data.startDate = from;
        Data.endDate = to;

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

        requestURL = part1+symbol+part2+startDate+part3+endDate+part4;

        return api.getList(requestURL);
    }

    public static Call<Results> check(){

        String date = endDate.substring(8,10);
        date = Integer.toString(Integer.parseInt(date)+4);
        endDate = endDate.substring(0,8)+date;

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

        requestURL = part1+symbol+part2+startDate+part3+endDate+part4;

        return api.getList(requestURL);
    }

}

