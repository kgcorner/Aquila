package com.kgaurav.balancer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kgaurav.balancer.exception.ConnectionFailedException;
import com.kgaurav.balancer.model.*;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    private OutputStream outputStream;
    private static final int PORT = Integer.parseInt(Util.loadProperties().getProperty("receptionist.port"));

    public static Receptionist getInstance() {
        if(INSTANCE == null)
            INSTANCE = new Receptionist();
        return INSTANCE;
    }

    public void deployReceptionist() {
        try {
            receptionistSocket = new ServerSocket(PORT);
            running = true;
            LOGGER.info("Receptionist Deployed successfully at "+receptionistSocket.getInetAddress().getHostAddress()
            +":"+receptionistSocket.getLocalPort());
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
                LOGGER.debug("received request from :"+clientConnection.getInetAddress().getHostAddress()+":"+clientConnection.getPort());
                inputStream = new DataInputStream(clientConnection.getInputStream());
                String requestData = Util.convertStreamToString(inputStream);
                Request request = new Gson().fromJson(requestData, Request.class);
                if(request != null) {
                    switch (request.getRequestCode()) {
                        case RequestCode.GET:
                            LOGGER.info("Fetching Data");
                            returnData(clientConnection, request);
                            break;
                        case RequestCode.SET:
                            LOGGER.info("Saving Data");
                            saveData(clientConnection, request, false);
                            break;
                        case RequestCode.PUT:
                            LOGGER.info("Updating Data");
                            saveData(clientConnection, request, true);
                            break;
                        case RequestCode.DEL:
                            LOGGER.info("Deleting Data");
                            deleteData(clientConnection, request);
                            break;
                        case RequestCode.INFO:
                            LOGGER.info("Returing Info");
                            sendInfo(clientConnection, request);
                            break;
                    }

                }

            } catch (IOException e) {
                LOGGER.error("Error occurred while accepting client connection");
                LOGGER.error(e.getMessage(), e);
            }
            catch (Exception e) {
                LOGGER.error("Unknown Error occurred while accepting client connection");
                LOGGER.error(e.getMessage(), e);
            }
            finally {
                Util.closeInputStream(inputStream);
            }
        }
    }

    /**
     * Send information about running main and backup nodes
     * @param clientConnection
     * @param request
     */
    private void sendInfo(Socket clientConnection, Request request) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("main", BalancerServer.getInstance().getMainNodes().size());
        jsonObject.addProperty("available", BalancerServer.getInstance().getAvailableBackupNodes().size());
        jsonObject.addProperty("linked", BalancerServer.getInstance().getLinkedBackupNodes().size());
        Response response = new Response();
        response.setData(jsonObject.toString());
        sendItem(clientConnection, response);
    }

    private void saveData(Socket clientConnection, Request request, boolean isUpdate) {
        LOGGER.debug("Inside save data method");
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
            LOGGER.info("Creating command");
            Command command = new Command();
            command.setCommandCode(CommandCode.PUT);
            command.setData(item);
            String commandData = new Gson().toJson(command);
            try {
                LOGGER.info("Asking node to store data");
                if(commandData != null && isUpdate) {
                    LOGGER.error("this is for test");
                }
                String responseData = Util.sendAndReceiveDataToNode(node.getAddress(), node.getPort(), commandData);
                Response responseFromNode = new Gson().fromJson(responseData, Response.class);

                if(!isUpdate)
                    response.setData(responseFromNode.getMessage());
                else
                    response.setData("Item updated successfully");
                LOGGER.info("Response received from Node");
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
        item.setKey(key);
        command.setData(item);
        NodeInfo info = keyStore.getNodeInfo(key);
        if(info == null) {
            sendNoItemFound(clientConnection);
        }
        else {
            Node node = info.getNode();
            item.setId(info.getInternalId());
            String commandData = new Gson().toJson(command);
            try {
                String responseData = Util.sendAndReceiveDataToNode(node.getAddress(), node.getPort(), commandData);
                Response response = new Gson().fromJson(responseData, Response.class);
                if(response == null) {
                    sendNoItemFound(clientConnection);
                }
                InternalItem internalItem = new Gson().fromJson(response.getData(), InternalItem.class);
                response = new Response();
                response.setData(internalItem.getValue());
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
            LOGGER.debug("Sending Item to client");
            Util.returnData(clientConnection, responseData);
            LOGGER.debug("Item sent to client");
        } catch (ConnectionFailedException e) {
            LOGGER.error("Error occurred while sending response to client");
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void deleteData(Socket clientConnection, Request request) {
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
            LOGGER.info("Creating command");
            Command command = new Command();
            command.setCommandCode(CommandCode.DELETE);
            command.setData(item);
            String commandData = new Gson().toJson(command);
            try {
                LOGGER.info("Asking node to store data");
                String responseData = Util.sendAndReceiveDataToNode(node.getAddress(), node.getPort(), commandData);
                Response responseFromNode = new Gson().fromJson(responseData, Response.class);
                response.setData(responseFromNode.getMessage());
                LOGGER.info("Response received from Node");
            } catch (ConnectionFailedException e) {
                LOGGER.error("Error occurred while getting response from node");
                LOGGER.error(e.getMessage(), e);
            }
            sendItem(clientConnection, response);
        }
    }

    public void stopReceptionist() {
        LOGGER.info("Shutting Down receptionist at "+receptionistSocket.getInetAddress().getHostAddress()+":"+
                receptionistSocket.getLocalPort());
        this.running = false;
        Util.closeSocket(receptionistSocket);
    }

    public String getRecenptionistAddress() {
        return receptionistSocket.getInetAddress().getHostAddress()+":"+receptionistSocket.getLocalPort();
    }

}
