package com.kgaurav.kmem;

import com.kgaurav.kmem.data.SyncSystem;
import com.kgaurav.kmem.model.Node;
import com.kgaurav.kmem.model.NodeType;
import org.apache.log4j.Logger;

/**
 * Created by admin on 3/22/2018.
 */
public class Application {
    private static final Logger LOGGER = Logger.getLogger(Application.class);
    private static String LB_ADDRESS = null;
    private static int LB_PORT = 0;
    private static int type = -1;
    public static void main(String[] args) {
        if(args.length != 7) {
            if(args.length != 3) {
                LOGGER.error("Total arguments needed 7 found "+args.length);
                LOGGER.error("Usage:");
                LOGGER.error("<Application name> <LB Address> <LB Port> <backup/main> <BKP Node1 address> <BKP Node1 Port> " +
                        "<BKP Node2 address> <BKP Node2 Port>");
                System.exit(1);
            }
            else {
                if(args[2].equals("backup")) {
                    type = NodeType.BACKUP_NODE;
                    startAsBackup(args);
                } else {
                    LOGGER.error("Only backup node can be created with there agrs");
                    System.exit(1);
                }

            }
        }else {
            type = NodeType.ACTIVE_NODE;
            startAsMain(args);
        }
    }

    /**
     * Starts ndoe as backup node
     * @param args
     */
    private static void startAsBackup(String[] args) {
        LB_ADDRESS = args[0];
        LB_PORT = Integer.parseInt(args[1]);
        Server server = new Server();
        boolean started = server.startServer();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("Program shutdown");
            }
        }));

        if(started) {
            Thread thread = new Thread(server);
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    LOGGER.error(e.getMessage(), e);
                }
            });
            thread.start();
        }
        else {
            LOGGER.error("Failed to start backup node");
        }
    }

    /**
     * Start Node as main node
     * @param args
     */
    private static void startAsMain(String[] args) {
        LOGGER.info("Starting Main node, parameters count:"+args.length);
        LB_ADDRESS = args[0];
        LB_PORT = Integer.parseInt(args[1]);
        Node node1 = new Node();
        node1.setAddress(args[3]);
        node1.setPort(Integer.parseInt(args[4]));
        Node node2 = new Node();
        node2.setAddress(args[5]);
        node2.setPort(Integer.parseInt(args[6]));
        SyncSystem.addBackupNode(node1);
        SyncSystem.addBackupNode(node2);
        Server server = new Server();
        LOGGER.info("parameters are parsed");
        boolean started = server.startServer();
        if(started) {
            Thread thread = new Thread(server);
            thread.start();
            LOGGER.info("Backup Node set to "+node1.getAddress()+":"+node1.getPort()+" and "
                    + node1.getAddress()+":"+node1.getPort());
        }
    }

    public static String getLbAddress() {
        return LB_ADDRESS;
    }

    public static int getLbPort() {
        return LB_PORT;
    }

    public static int getType() {
        return type;
    }
}
