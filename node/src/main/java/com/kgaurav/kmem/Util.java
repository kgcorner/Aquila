package com.kgaurav.kmem;

import com.kgaurav.kmem.exception.ConnectionFailedException;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.stream.Stream;

/**
 * Created by admin on 3/23/2018.
 */
public class Util {
    private static final Logger LOGGER = Logger.getLogger(Util.class);

    /**
     * Closes the {@link Socket}
     * @param socket
     */
    public static void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Closes given {@link InputStream}
     * @param stream
     */
    public static void closeInputStream(InputStream stream) {
        try {
            stream.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Closes given {@link OutputStream}
     * @param stream
     */
    public static void closeOutputStream(OutputStream stream) {
        try {
            stream.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Sends data to balancer node
     * @param data
     * @return
     * @throws ConnectionFailedException
     */
    public static boolean sendToBalancer(String data) throws ConnectionFailedException {
        Socket me = null;
        DataOutputStream outputStream = null;
        try {
            LOGGER.info("Connecting to balancer");
            me = new Socket(Application.getLbAddress(), Application.getLbPort());
            LOGGER.info("Connected to balancer");
            outputStream = new DataOutputStream(me.getOutputStream());
            outputStream.write(data.getBytes());
            return true;
        } catch (IOException e) {
            LOGGER.error("Connection failed with balancer");
            LOGGER.error(e.getMessage(), e);
            throw new ConnectionFailedException(e.getMessage());
        }
        finally {
            closeOutputStream(outputStream);
            closeSocket(me);
        }
    }
}
