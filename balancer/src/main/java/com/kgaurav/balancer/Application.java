package com.kgaurav.balancer;

import org.apache.log4j.Logger;

/**
 * Created by admin on 4/5/2018.
 */
public class Application {
    private static final Logger LOGGER = Logger.getLogger(Application.class);
    public static void main(String[] args) {
        if(args.length != 1) {
            LOGGER.error("Usage:");
            LOGGER.error("<Application name> <number of nodes>");
            System.exit(1);
        }
        int nodeCount = Integer.parseInt(args[0]);
        BalancerServer server = new BalancerServer();
        server.startServer();
        Thread serverThread = new Thread(server);
        serverThread.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        server.startNodes(nodeCount);
    }
}
