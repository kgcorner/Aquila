package com.test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by admin on 4/9/2018.
 */
public class Server implements Runnable{

    private static final int PORT = 9999;
    private static ServerSocket serverSocket;
    DataOutputStream outputStream = null;
    public static void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(new Server());
        thread.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("Server: awaiting client");
                Socket client = serverSocket.accept();
                System.out.println("Server: connection received");
                InputStream inputStream = client.getInputStream();
                outputStream = new DataOutputStream(client.getOutputStream());
                System.out.println("Server: awaiting massage");
                String dataFromClient = convertStreamToString(inputStream);
                System.out.println("Server: Massage received");
                System.out.println(dataFromClient);
                if(!client.isClosed()) {
                    outputStream.writeUTF("Bye\n");
                }
                else {
                    System.out.println("Server: Socket is closed");
                }
                //outputStream.close();
                System.out.println("Server: Response Sent");
            } catch (IOException e) {
                e.printStackTrace();
            }
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

}
