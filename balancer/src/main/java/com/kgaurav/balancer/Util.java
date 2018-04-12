package com.kgaurav.balancer;

import com.kgaurav.balancer.exception.ConnectionFailedException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.Random;

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

    public static String convertStreamToString(InputStream is) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        try {
            sb.append(reader.readLine());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
        return sb.toString();
    }

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
     * Closes the {@link Socket}
     * @param socket
     */
    public static void closeSocket(ServerSocket socket) {
        try {
            LOGGER.info("Closing Server Socket");
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
     * Sends message to a particular address
     * @param address
     * @param port
     * @param data
     * @return true if sent successfully false otherwise
     */
    public static String  sendAndReceiveDataToNode(String address, int port, String data) throws ConnectionFailedException {
        Socket me = null;
        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;
        String responseData = null;
        try {
            LOGGER.info("Connecting to destination");
            me = new Socket(address, port);
            LOGGER.info("Connected to destination");
            outputStream = new DataOutputStream(me.getOutputStream());
            outputStream.write((data+"\n").getBytes());
            LOGGER.info("Data sent to destination");
            inputStream = new DataInputStream(me.getInputStream());
            responseData = convertStreamToString(inputStream);
            //LOGGER.info("Response Data:"+responseData);
            return responseData;
        } catch (IOException e) {
            LOGGER.error("Connection failed with balancer");
            LOGGER.error(e.getMessage(), e);
            throw new ConnectionFailedException(e.getMessage());
        } finally {
            closeOutputStream(outputStream);
            closeInputStream(inputStream);
            closeSocket(me);
        }
    }

    /**
     * Return data to given socket
     * @param socket
     * @param data
     * @return true if sent successfully false otherwise
     */
    public static boolean returnData (Socket socket, String data) throws ConnectionFailedException {
        Socket me = socket;
        DataOutputStream outputStream = null;
        try {
            LOGGER.info("Waiting for opening output stream");
            outputStream = new DataOutputStream(me.getOutputStream());
            outputStream.write((data+"\n").getBytes());
            //LOGGER.info("Got stream writing data");
            return true;
        } catch (IOException e) {
            LOGGER.error("Connection failed with "+socket.getInetAddress().getHostAddress()+":"+socket.getLocalPort());
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
            outputStream.write((data+"\n").getBytes());
            LOGGER.info("Response sent to node");
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

    public static String generateInternalKey() {
        return new BigInteger(130, new Random()).toString(32);
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.trim().length() < 1;
    }
}
