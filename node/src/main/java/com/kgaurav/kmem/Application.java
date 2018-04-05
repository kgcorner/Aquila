package com.kgaurav.kmem;

import com.kgaurav.kmem.data.SyncSystem;
import com.kgaurav.kmem.model.Node;
import org.apache.log4j.Logger;

/**
 * Created by admin on 3/22/2018.
 */
public class Application {
    private static final Logger LOGGER = Logger.getLogger(Application.class);
    private static String LB_ADDRESS = null;
    private static int LB_PORT = 0;
    private static String type = null;
    public static void main(String[] args) {
        if(args.length != 7) {
            if(args.length != 4) {
                LOGGER.error("Usage:");
                LOGGER.error("<Application name> <LB Address> <LB Port> <backup/main><BKP Node1 address> <BKP Node1 Port> " +
                        "<BKP Node2 address> <BKP Node2 Port>");
            }
            else {
                type = "BACKUP";
                startAsBackup(args);
            }
        }else {
            type = "MAIN";
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
        if(started) {
            Thread thread = new Thread(server);
            thread.start();
        }
    }

    /**
     * Start Node as main node
     * @param args
     */
    private static void startAsMain(String[] args) {
        LB_ADDRESS = args[0];
        LB_PORT = Integer.parseInt(args[1]);
        Node node1 = new Node();
        node1.setAddress(args[2]);
        node1.setPort(Integer.parseInt(args[3]));
        Node node2 = new Node();
        node2.setAddress(args[4]);
        node2.setPort(Integer.parseInt(args[5]));
        SyncSystem.addBackupNode(node1);
        SyncSystem.addBackupNode(node2);
        Server server = new Server();
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

    public static String getType() {
        return type;
    }
}
