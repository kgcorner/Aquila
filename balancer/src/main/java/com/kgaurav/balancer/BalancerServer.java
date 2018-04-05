package com.kgaurav.balancer;

import com.google.gson.Gson;
import com.kgaurav.balancer.model.Node;
import com.kgaurav.balancer.model.Response;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Properties;

/**
 * Created by admin on 4/5/2018.
 */
public class BalancerServer implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(BalancerServer.class);
    private ServerSocket serverSocket;
    private Socket connectionSocket;
    private static boolean running;
    private List<Node> nodes = null;
    private Object waitForServerStatus = new Object();
    private static final Properties properties = Util.loadProperties();
    private static final String PATH_TO_NODE_APP = "path.to.node";

    /**
     * Start balancers with given number of nodes
     * @param nodeCount
     * @return
     */
    public boolean startServer(int nodeCount) {
        try {
            serverSocket = new ServerSocket(0);
            LOGGER.info("Balancer started at : "+
                    serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort());
            running = true;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return running;
    }

    private void startNodes(int nodeCount) {
        for (int i = 0; i < nodeCount; i++) {

        }
    }

    private void startBackupNode(){
        String command = "java -jar "+properties.getProperty(PATH_TO_NODE_APP);

    }

    private void startMainNode() {

    }


    @Override
    public void run() {
        DataInputStream inputStream = null;
        while(running)  {
            try {
                connectionSocket = serverSocket.accept();
                inputStream = new DataInputStream(connectionSocket.getInputStream());
                String request = convertStreamToString(inputStream);
                Response response = new Gson().fromJson(request, Response.class);
                if(response.getStatus() == Response.SERVER_STATUS) {
                    this.waitForServerStatus.notify();
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
