package com.kgaurav.kmem;

import com.google.gson.JsonObject;

/**
 * Created by admin on 4/5/2018.
 */
public class Test {
    public static void main(String[] args) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("address", "this is address");
        jsonObject.addProperty("port", "this is port");
        System.out.println(jsonObject.toString());
    }
}
