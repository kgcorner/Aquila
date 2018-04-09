package com.kgaurav.balancer;

import com.kgaurav.balancer.exception.ConnectionFailedException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

public class UtilTest {
    private static final Logger LOGGER = Logger.getLogger(UtilTest.class);
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
            me = new Socket(address, port);
            outputStream = new DataOutputStream(me.getOutputStream());
            outputStream.write(data.getBytes());
            outputStream.flush();
            me.shutdownOutput();
            LOGGER.info("Data written from client");
            inputStream = new DataInputStream(me.getInputStream());
            responseData = convertStreamToString(inputStream);
            return responseData;
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            closeOutputStream(outputStream);
            closeInputStream(inputStream);
            closeSocket(me);
        }
        return null;
    }



    /**
     * Closes the {@link Socket}
     * @param socket
     */
    public static void closeSocket(Socket socket) {
        try {
            LOGGER.info("Closing Socket");
            socket.close();
        } catch (IOException e) {
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
        }
    }

    public static String convertStreamToString(InputStream is) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw e;
            }
        }

        return sb.toString();
    }
}
