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
        if(nodeCount == 0) {
            LOGGER.error("At least 1 node is required to start balancer");
            System.exit(1);
        }
        String banner = "\n" +
                "****************************************************************\n"+
                "* A         Q           U           I           L            A *\n"+
                "****************************************************************\n";
        LOGGER.info(banner);
        BalancerServer server = BalancerServer.getInstance();
        server.startServer();
        Thread serverThread = new Thread(server);
        serverThread.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        server.startNodes(nodeCount);
        LOGGER.info("Balancer started successfully");
    }
}
