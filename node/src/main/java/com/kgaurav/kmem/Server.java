package com.kgaurav.kmem;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kgaurav.kmem.data.Store;
import com.kgaurav.kmem.data.SyncSystem;
import com.kgaurav.kmem.exception.ConnectionFailedException;
import com.kgaurav.kmem.model.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

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
            String nodeName = ManagementFactory.getRuntimeMXBean().getName();
            String nodeType = Application.getType() == 0?"Backup":"Main";
            LOGGER.info(nodeName+" "+nodeType+ " Node started at : "+
                    serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());
            running = true;
            Response response = new Response();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("address", serverSocket.getInetAddress().getHostAddress());
            jsonObject.addProperty("port", serverSocket.getLocalPort());
            jsonObject.addProperty("type", Application.getType());
            response.setStatus(Response.SERVER_STATUS);
            response.setData(jsonObject.toString());
            try {
                LOGGER.info("Sending message to balancer");
                Util.sendToBalancer(new Gson().toJson(response));
                LOGGER.info("Message sent to balancer");
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
        LOGGER.info("Listening....");
        DataInputStream inputStream = null;
        while(running)  {
            try {
                connectionSocket = serverSocket.accept();
                inputStream = new DataInputStream(connectionSocket.getInputStream());
                String request = convertStreamToString(inputStream);
                Command command = new Gson().fromJson(request, Command.class);
                switch (command.getCommandCode()) {
                    case CommandCode.GET:
                        LOGGER.debug("fetching Data from main node");
                        returnItem(connectionSocket, command, false);
                        break;
                    case CommandCode.PUT:
                        LOGGER.debug("Saving Data in main node");
                        saveItem(connectionSocket, command, false);
                        break;
                    case CommandCode.DELETE:
                        LOGGER.debug("Deleting Data from main node");
                        deleteItem(connectionSocket, command, false);
                        break;
                    case CommandCode.BACK_GET:
                        LOGGER.debug("fetching Data from backup node");
                        returnItem(connectionSocket, command, true);
                        break;
                    case CommandCode.BACK_PUT:
                        LOGGER.debug("Saving Data in backup node");
                        saveItem(connectionSocket, command, true);
                        break;
                    case CommandCode.BACK_DELETE:
                        LOGGER.debug("Deleting Data from backup node");
                        deleteItem(connectionSocket, command, true);
                        break;
                    case CommandCode.ADD_BACKUP_NODE:
                        LOGGER.debug("Adding new backup node");
                        addBackupNode(connectionSocket, command);
                        break;
                    case CommandCode.SHUTDOWN:
                        shutDown();
                        break;
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void shutDown() {
        LOGGER.info("Shutting Down node at "+serverSocket.getInetAddress().getHostAddress()+":"+
                serverSocket.getLocalPort());
        this.running = false;
        Util.closeSocket(serverSocket);
    }

    private void addBackupNode(Socket connectionSocket, Command command) {
        String nodeStr = command.getOtherData();
        Node node = new Gson().fromJson(nodeStr, Node.class);
        SyncSystem.addBackupNode(node);
        Response response = new Response();
        response.setMessage("Node added");
        String data = new Gson().toJson(response);
        try {
            Util.sendToBalancer(connectionSocket, data);
        } catch (ConnectionFailedException e) {
            LOGGER.error("Failed to connect with balancer");
        }
    }

    private void returnItem(Socket connection, Command command, boolean backup) {
        InternalItem internalItem = command.getData();
        Item item = Store.getItem(internalItem.getId());
        if(!backup) {
            Socket socket = null;
            OutputStream outputStream = null;
            Response response = new Response();
            response.setData(new Gson().toJson(item));
            String data = new Gson().toJson(response);

            try {
                Util.sendToBalancer(connection, data);
            } catch (ConnectionFailedException e) {
                LOGGER.error("Failed to connect with balancer");
            }
        }
    }

    private void saveItem(Socket connectionSocket, Command command, boolean backup) {
        InternalItem internalItem = command.getData();
        Item item = new Item(internalItem.getKey(), internalItem.getValue());
        Store.putItem(internalItem.getId(), item);
        if(!backup) {
            Response response = new Response();
            response.setMessage("Item is saved");
            String data = new Gson().toJson(response);
            try {
                Util.sendToBalancer(connectionSocket, data);
            } catch (ConnectionFailedException e) {
                LOGGER.error("Failed to connect with balancer");
            }
            command.setCommandCode(CommandCode.BACK_PUT);
            syncData(command);
        }
    }

    /**
     * Sync the data with backup nodes
     * @param command
     */
    private void syncData(Command command) {
        List<Node> backupNodes = SyncSystem.getBackupNodes();
        for(Node node : backupNodes) {
            String commandString = new Gson().toJson(command);
            try {
                Util.sendDataToNode(node.getAddress(), node.getPort(), commandString);
            } catch (ConnectionFailedException e) {
                LOGGER.error("Failed to sync data " + commandString);
            }
        }
    }

    private void deleteItem(Socket connectionSocket, Command command, boolean backup) {
        InternalItem internalItem = command.getData();
        Store.removeItem(internalItem.getId());
        if(!backup) {
            Response response = new Response();
            response.setMessage("Item is deleted");
            String data = new Gson().toJson(response);
            try {
                Util.sendToBalancer(connectionSocket, data);
            } catch (ConnectionFailedException e) {
                LOGGER.error("Failed to connect with balancer");
            }
            command.setCommandCode(CommandCode.BACK_DELETE);
            syncData(command);
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
            sb.append(reader.readLine());
        } catch (IOException e) {
            throw e;
        }

        return sb.toString();
    }
}
