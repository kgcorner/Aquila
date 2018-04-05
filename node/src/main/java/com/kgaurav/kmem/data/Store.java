package com.kgaurav.kmem.data;

import com.kgaurav.kmem.model.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 3/23/2018.
 */
public class Store {
    private static final Map<String, Item> items = new HashMap<>();

    public static Item getItem(String key) {
        return items.get(key);
    }

    public static Item putItem(String key, Item item) {
        return items.put(key, item);
    }

    public static Item removeItem(String key) {
        return items.remove(key);
    }
}
