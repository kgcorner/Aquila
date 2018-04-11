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
        InputStream inputStream = null;
        String responseData = null;
        try {
            me = new Socket(address, port);
            outputStream = new DataOutputStream(me.getOutputStream());
            inputStream = me.getInputStream();
            outputStream.write((data+"\n").getBytes());
            LOGGER.info("Data written from client");
            responseData = convertStreamToString(inputStream);
            return responseData;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
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
            sb.append(reader.readLine());
        } catch (IOException e) {
            throw e;
        }

        return sb.toString();
    }

    public static Process runCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);

            if(!process.isAlive()) {
                InputStream stream = process.getErrorStream();
                String output =IOUtils.toString(stream);
                LOGGER.error(output);
            }
            return process;
        } catch (IOException e) {
            return null;
        }
    }

    public static String getApplicationBinaryLocation() {
        String path = UtilTest.class.getResource("").getPath();
        path = path+"../../../../../../libs/balancer-1.0-SNAPSHOT.jar";
        path = path.substring(1);
        return path;
    }
}
