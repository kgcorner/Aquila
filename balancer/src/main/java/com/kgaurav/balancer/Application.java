package com.kgaurav.balancer;

import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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
        startBalancer(server);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        server.startNodes(nodeCount);
        Receptionist receptionist = Receptionist.getInstance();
        receptionist.deployReceptionist();
        startReceptionist(receptionist);
        LOGGER.info("Balancer started successfully");



        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                Application.shutDown();
            }
        }));
    }

    public static void shutDown() {
        BalancerServer.getInstance().shutDownBalancer();
        Receptionist.getInstance().stopReceptionist();
    }

    private static void startBalancer(BalancerServer server) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(server);
    }

    private static void startReceptionist(Receptionist server) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(server);
    }
}
