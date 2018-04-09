package com.test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by admin on 4/9/2018.
 */
public class Client {

    private static final int PORT = 9999;
    public static void start() {
        try {
            Socket socket = new Socket("0.0.0.0", PORT);
            System.out.println("Client: Connected with server");
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            InputStream inputStream = socket.getInputStream();
            outputStream.writeUTF("Hello\n");
            //outputStream.flush();
            System.out.println("Client: Massage sent");
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            System.out.println(convertStreamToString(inputStream));
            System.out.println("Client: response received");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String convertStreamToString(InputStream is) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        try {
            System.out.println("Client: waiting for response");
            sb.append(reader.readLine());
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
