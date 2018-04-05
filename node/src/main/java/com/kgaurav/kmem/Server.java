package com.kgaurav.kmem;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kgaurav.kmem.data.Store;
import com.kgaurav.kmem.exception.ConnectionFailedException;
import com.kgaurav.kmem.model.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by admin on 3/22/2018.
 */
public class Server implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Server.class);
    private ServerSocket serverSocket;
    private Socket connectionSocket;
    private static boolean running;

    public boolean startServer() {
        try {
            serverSocket = new ServerSocket(0);
            LOGGER.info(Application.getType()+ " Node started at : "+
                    serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());
            running = true;
            Response response = new Response();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("address", serverSocket.getInetAddress().getHostAddress());
            jsonObject.addProperty("port", serverSocket.getLocalPort());
            jsonObject.addProperty("type", Application.getType());
            response.setStatus(CommandCode.SERVER_STATUS);
            response.setData(jsonObject.toString());
            try {
                Util.sendToBalancer(new Gson().toJson(response));
            } catch (ConnectionFailedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return running;
    }

    @Override
    public void run() {
        DataInputStream inputStream = null;
        while(running)  {
            try {
                connectionSocket = serverSocket.accept();
                inputStream = new DataInputStream(connectionSocket.getInputStream());
                String request = convertStreamToString(inputStream);
                Command command = new Gson().fromJson(request, Command.class);
                switch (command.getCommandCode()) {
                    case CommandCode.GET:
                        returnItem(command);
                        break;
                    case CommandCode.PUT:
                        saveItem(command);
                        break;
                    case CommandCode.DELETE:
                        deleteItem(command);
                        break;
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void returnItem(Command command) {
        InternalItem internalItem = command.getData();
        Item item = Store.getItem(internalItem.getId());
        Socket socket = null;
        OutputStream outputStream = null;
        Response response = new Response();
        response.setData(new Gson().toJson(item));
        String data = new Gson().toJson(response);
        try {
            Util.sendToBalancer(data);
        } catch (ConnectionFailedException e) {
            LOGGER.error("Failed to connect with balancer");
        }
    }

    private void saveItem(Command command) {
        InternalItem internalItem = command.getData();
        Item item = new Item(internalItem.getKey(), internalItem.getValue());
        Store.putItem(internalItem.getId(), item);
        Response response = new Response();
        response.setMessage("Item is saved");
        String data = new Gson().toJson(response);
        try {
            Util.sendToBalancer(data);
        } catch (ConnectionFailedException e) {
            LOGGER.error("Failed to connect with balancer");
        }
    }

    private void deleteItem(Command command) {
        InternalItem internalItem = command.getData();
        Store.removeItem(internalItem.getId());
        Response response = new Response();
        response.setMessage("Item is deleted");
        String data = new Gson().toJson(response);
        try {
            Util.sendToBalancer(data);
        } catch (ConnectionFailedException e) {
            LOGGER.error("Failed to connect with balancer");
        }
    }

    public static void stopServer() {
        running = false;
    }

    private static String convertStreamToString(InputStream is) throws IOException{
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
