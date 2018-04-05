package com.kgaurav.balancer;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by admin on 4/5/2018.
 */
public class Util {
    private static final Logger LOGGER = Logger.getLogger(Util.class);
    public static Properties loadProperties() {
        InputStream stream = Util.class.getResourceAsStream("application.properties");
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return properties;
    }

    //public void run
}
