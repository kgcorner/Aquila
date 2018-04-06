package com.kgaurav.balancer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kgaurav.balancer.model.Node;
import com.kgaurav.balancer.model.Response;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by admin on 4/5/2018.
 */
public class BalancerServer implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(BalancerServer.class);
    private ServerSocket serverSocket;
    private Socket connectionSocket;
    private static boolean running;
    private List<Node> mainNodes = null;
    private Queue<Node> availableBackupNodes = null;
    private List<Node> linkedBackupNodes = null;
    private static Object waitForServerStatus = new Object();
    private static final Properties properties = Util.loadProperties();
    private static final String PATH_TO_NODE_APP = "path.to.node";

    /**
     * Start balancers with given number of mainNodes
     * @return
     */
    public boolean startServer() {
        try {
            serverSocket = new ServerSocket(0);
            LOGGER.info("Balancer started at : "+
                    serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());
            running = true;
            mainNodes = new ArrayList<>();
            availableBackupNodes = new LinkedBlockingDeque<>();
            linkedBackupNodes = new ArrayList<>();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return running;
    }

    public void startNodes(int nodeCount) {
        LOGGER.info("Starting " +nodeCount + " nodes");
        for (int i = 0; i < nodeCount; i++) {
            startBackupNode();
            startBackupNode();
        }
        for (int i = 0; i < nodeCount; i++) {
            if(availableBackupNodes.size() < 2) {
                throw new IllegalStateException("Not enough backup nodes are available");
            }
            Node backup1 = availableBackupNodes.poll();
            Node backup2 = availableBackupNodes.poll();
            startMainNode(backup1, backup2);
        }
    }

    private void startBackupNode(){
        String command = "java -jar "+properties.getProperty(PATH_TO_NODE_APP);
        command += " "+serverSocket.getInetAddress().getHostAddress()+" "+ serverSocket.getLocalPort()+" backup";
        LOGGER.info("Starting backup node");
        if(Util.runCommand(command)) {
            LOGGER.info("backup node started");
            try {
                synchronized (waitForServerStatus) {
                    waitForServerStatus.wait();
                }
            } catch (InterruptedException e) {}
        }
        else {
            LOGGER.info("backup node start fail");
        }
        int i = 1;
    }

    private void startMainNode(Node backup1, Node backup2) {
        String command = null;
        StringBuilder sb = new StringBuilder();
        sb.append("java -jar "+properties.getProperty(PATH_TO_NODE_APP));
        sb.append(" "+serverSocket.getInetAddress().getHostAddress());
        sb.append(" "+serverSocket.getLocalPort());
        sb.append(" main");
        sb.append(" "+backup1.getAddress());
        sb.append(" "+backup1.getPort());
        sb.append(" "+backup2.getAddress());
        sb.append(" "+backup2.getPort());
        command = sb.toString();
        if(Util.runCommand(command)) {
            try {
                synchronized (waitForServerStatus) {
                    waitForServerStatus.wait();
                }
            } catch (InterruptedException e) {}
        }
    }


    @Override
    public void run() {
        DataInputStream inputStream = null;
        LOGGER.info("Listening....");
        while(running)  {
            try {
                connectionSocket = serverSocket.accept();
                LOGGER.info("Received connection "+connectionSocket.getInetAddress().getHostAddress()+":"+
                        connectionSocket.getLocalPort());
                inputStream = new DataInputStream(connectionSocket.getInputStream());
                String request = convertStreamToString(inputStream);
                Response response = new Gson().fromJson(request, Response.class);
                if(response.getStatus() == Response.SERVER_STATUS) {
                    JsonParser jsonParser = new JsonParser();
                    JsonElement jsonElement = jsonParser.parse(response.getData());
                    String address = jsonElement.getAsJsonObject().get("address").getAsString();
                    int port = jsonElement.getAsJsonObject().get("port").getAsInt();
                    int type = jsonElement.getAsJsonObject().get("type").getAsInt();
                    Node node = new Node();
                    node.setAddress(address);
                    node.setPort(port);
                    Node.Node_Type nodeType = type == 0? Node.Node_Type.BACKUP : Node.Node_Type.MAIN;
                    node.setType(nodeType);
                    if(nodeType == Node.Node_Type.BACKUP) {
                        availableBackupNodes.add(node);
                        node.setActive(false);
                        LOGGER.info("Backup Node connected at "+address+":"+port);
                    }
                    else {
                        mainNodes.add(node);
                        node.setActive(true);
                        LOGGER.info("Main Node connected at "+address+":"+port);
                    }
                    synchronized (waitForServerStatus) {
                        this.waitForServerStatus.notify();
                    }
                }
                else {
                    //TODO: return data/response to caller
                }

                /*switch (command.getCommandCode()) {
                    case CommandCode.GET:
                        returnItem(command);
                        break;
                    case CommandCode.PUT:
                        saveItem(command);
                        break;
                    case CommandCode.DELETE:
                        deleteItem(command);
                        break;
                }*/
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
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
