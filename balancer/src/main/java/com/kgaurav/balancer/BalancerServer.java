package com.kgaurav.balancer;

import com.kgaurav.balancer.model.Node;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Created by admin on 4/5/2018.
 */
public class BalancerServer implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(BalancerServer.class);
    private ServerSocket serverSocket;
    private Socket connectionSocket;
    private static boolean running;
    private List<Node> nodes = null;

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

    @Override
    public void run() {

    }
}
