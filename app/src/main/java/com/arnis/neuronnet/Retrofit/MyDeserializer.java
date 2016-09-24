package com.arnis.neuronnet.Retrofit;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by arnis on 03/05/16.
 */
public class MyDeserializer implements JsonDeserializer<Results> {

    @Override
    public Results deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonElement content = json.getAsJsonObject().get("query").getAsJsonObject().get("results");

        return  new Gson().fromJson(content, Results.class);
    }
}
