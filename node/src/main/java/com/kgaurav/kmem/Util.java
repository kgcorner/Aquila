package com.kgaurav.kmem;

import com.kgaurav.kmem.data.SyncSystem;
import com.kgaurav.kmem.exception.ConnectionFailedException;
import com.kgaurav.kmem.model.Command;
import com.kgaurav.kmem.model.CommandCode;
import com.kgaurav.kmem.model.Node;
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

    /**
     * Sends message to a particular address
     * @param address
     * @param port
     * @param data
     * @return true if sent successfully false otherwise
     */
    public static boolean sendDataToNode(String address, int port, String data) throws ConnectionFailedException {
        Socket me = null;
        DataOutputStream outputStream = null;
        try {
            LOGGER.info("Connecting to destination");
            me = new Socket(address, port);
            LOGGER.info("Connected to destination");
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
