package com.kgaurav.kmem.data;

import com.google.gson.Gson;
import com.kgaurav.kmem.Util;
import com.kgaurav.kmem.exception.ConnectionFailedException;
import com.kgaurav.kmem.model.Command;
import com.kgaurav.kmem.model.Node;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by admin on 3/23/2018.
 */
public class SyncSystem implements Runnable {
    private static final List<Node> backupNodes = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(SyncSystem.class);
    private Command command;

    public SyncSystem(Command command) {
        this.command = command;
    }

    @Override
    public void run() {
        if(this.command != null) {
            String data = new Gson().toJson(command);
            for(Node node : backupNodes) {
                try {
                    Util.sendDataToNode(node.getAddress(), node.getPort(), data);
                } catch (ConnectionFailedException e) {
                    LOGGER.error("Sync failed with backup Node "+node.getAddress()+":"+node.getPort());
                    LOGGER.error("data:"+data);
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Add backup nodes
     * @param node
     */
    public static void addBackupNode(Node node) {
        backupNodes.add(node);
    }

    /**
     * Removes Dead node from backup node list
     * @param node
     */
    public static void removeDeadNode(Node node) {
        backupNodes.remove(node);
    }

    /**
     * Get backup nodes
     * @return
     */
    public static List<Node> getBackupNodes() {
        return Collections.unmodifiableList(backupNodes);
    }
}


