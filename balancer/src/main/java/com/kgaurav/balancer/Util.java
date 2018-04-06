package com.kgaurav.balancer;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Created by admin on 4/5/2018.
 */
public class Util {
    private static final Logger LOGGER = Logger.getLogger(Util.class);
    public static Properties loadProperties() {
        InputStream stream = Util.class.getResourceAsStream("/application.properties");
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return properties;
    }

    public static boolean runCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);

            if(!process.isAlive()) {
                InputStream stream = process.getErrorStream();
                String output =IOUtils.toString(stream);
                LOGGER.error(output);
            }
            return process.isAlive();
        } catch (IOException e) {
            return false;
        }
    }
}
