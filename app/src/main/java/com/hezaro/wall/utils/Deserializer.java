package com.hezaro.wall.utils;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 6/30/19 1:48 PM.
 */
public class Deserializer {

    void deserial() {
        JsonDeserializer<String> axisDeserializer = (json, typeOfT, context) -> {
            Type valueType;
            String axisFormat = json.getAsJsonObject().get("format").getAsString();
            switch (axisFormat) {
                case "inbox":
                    valueType = new TypeToken<String>() {
                    }.getType();
                    break;

                case "source":
                    valueType = new TypeToken<String>() {
                    }.getType();
                    break;

                default:
                case "date":
                case "string":
                    valueType = new TypeToken<String>() {
                    }.getType();
                    break;
            }

            return new Gson().fromJson(json, valueType);
        };
    }

}
