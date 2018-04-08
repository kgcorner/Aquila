package com.kgaurav.balancer;

import com.google.gson.Gson;
import com.kgaurav.balancer.exception.ConnectionFailedException;
import com.kgaurav.balancer.model.*;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Receptionist looks after the requests coming from Client
 */
public class Receptionist implements Runnable{

    private static ServerSocket receptionistSocket = null;
    private static Receptionist INSTANCE;
    private static final Logger LOGGER = Logger.getLogger(Receptionist.class);
    private static boolean running = false;
    private Socket clientConnection;
    private KeyStore keyStore = null;







    public static Receptionist getInstance() {
        if(INSTANCE == null)
            INSTANCE = new Receptionist();
        return INSTANCE;
    }

    public void deployReceptionist() {
        DataInputStream inputStream = null;
        try {
            receptionistSocket = new ServerSocket(0);
            running = true;
            LOGGER.info("Receptionist Deployed successfully");
        } catch (IOException e) {
            LOGGER.error("Error occurred while deploying Receptionist");
            LOGGER.error(e.getMessage(), e);
        }
    }
    private Receptionist() {
        keyStore = KeyStore.getInstance();
    }


    @Override
    public void run() {
        DataInputStream inputStream = null;
        while (running) {
            try {
                this.clientConnection = receptionistSocket.accept();
                inputStream = new DataInputStream(clientConnection.getInputStream());
                String requestData = Util.convertStreamToString(inputStream);
                Request request = new Gson().fromJson(requestData, Request.class);
                if(request != null) {
                    switch (request.getRequestCode()) {
                        case RequestCode.GET:
                            returnData(clientConnection, request);
                            break;
                        case RequestCode.SET:
                            saveData(clientConnection, request);
                            break;
                        case RequestCode.PUT:
                            updateData(clientConnection, request);
                            break;
                        case RequestCode.DEL:
                            deleteData(clientConnection, request);
                            break;
                    }

                }

            } catch (IOException e) {
                LOGGER.error("Error occurred while accepting client connection");
                LOGGER.error(e.getMessage(), e);
            }
            catch (Exception e) {
                LOGGER.error("Error occurred while accepting client connection");
                LOGGER.error(e.getMessage(), e);
            }
            finally {
                Util.closeInputStream(inputStream);
            }
        }
    }

    private void saveData(Socket clientConnection, Request request) {
        String key = request.getKey();
        Response response = new Response();
        if(Util.isNullOrEmpty(key)) {
            response.setData("Invalid key");
        }
        else {
            NodeInfo nodeInfo = keyStore.storeKey(key);
            Node node = nodeInfo.getNode();
            InternalItem item = new InternalItem();
            item.setId(nodeInfo.getInternalId());
            item.setKey(request.getKey());
            item.setValue(request.getValue());
            Command command = new Command();
            command.setCommandCode(CommandCode.PUT);
            command.setData(item);
            String commandData = new Gson().toJson(command);
            try {
                String responseData = Util.sendAndReceiveDataToNode(node.getAddress(), node.getPort(), commandData);
                Response responseFromNode = new Gson().fromJson(responseData, Response.class);
                response.setData(responseFromNode.getMessage());
            } catch (ConnectionFailedException e) {
                LOGGER.error("Error occurred while getting response from node");
                LOGGER.error(e.getMessage(), e);
            }
            sendItem(clientConnection, response);
        }
    }

    private void returnData(Socket clientConnection, Request request) {
        String key = request.getKey();
        Command command = new Command();
        command.setCommandCode(CommandCode.GET);
        InternalItem item = new InternalItem();
        NodeInfo info = keyStore.getNodeInfo(key);
        if(info == null) {
            sendNoItemFound(clientConnection);
        }
        else {
            Node node = info.getNode();
            String commandData = new Gson().toJson(command);
            try {
                String responseData = Util.sendAndReceiveDataToNode(node.getAddress(), node.getPort(), commandData);
                Response response = new Gson().fromJson(responseData, Response.class);
                if(response != null) {
                    sendNoItemFound(clientConnection);
                }
                InternalItem internalItem = new Gson().fromJson(response.getData(), InternalItem.class);
                response = new Response();
                response.setData(item.getValue());
                sendItem(clientConnection, response);
            } catch (ConnectionFailedException e) {
                LOGGER.error("Error occurred while getting response from node");
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void sendNoItemFound(Socket clientConnection) {
        Response response = new Response();
        response.setData("No Item found");
        String responseData = new Gson().toJson(response);
        try {
            Util.returnData(clientConnection, responseData);
        } catch (ConnectionFailedException e) {
            LOGGER.error("Error occurred while sending response to client");
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void sendItem(Socket clientConnection, Response response) {
        String responseData = new Gson().toJson(response);
        try {
            Util.returnData(clientConnection, responseData);
        } catch (ConnectionFailedException e) {
            LOGGER.error("Error occurred while sending response to client");
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void updateData(Socket clientConnection, Request request) {
    }

    private void deleteData(Socket clientConnection, Request request) {
    }

    public void stopReceptionist() {
        running = false;
        try {
            receptionistSocket.close();
        } catch (IOException e) {
            LOGGER.error("Error occurred while shutting down Receptionist");
            LOGGER.error(e.getMessage(), e);
        }
    }
}
