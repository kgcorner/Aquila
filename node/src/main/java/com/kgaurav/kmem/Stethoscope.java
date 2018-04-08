package com.kgaurav.kmem;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.kgaurav.kmem.data.SyncSystem;
import com.kgaurav.kmem.exception.ConnectionFailedException;
import com.kgaurav.kmem.model.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Checks backup nodes breath
 */
public class Stethoscope implements Runnable{
    private static final String FREQUENCY_IN_SECOND_LABEL = "breath.check.in.second";
    private static final int FREQUENCY = Integer.parseInt(Util.loadConfigs().getProperty(FREQUENCY_IN_SECOND_LABEL));
    private static final Logger LOGGER = Logger.getLogger(Stethoscope.class);
    private static List<Node> dieingNodes = new ArrayList<>();
    private static final int MAX_BREATH_THREAD = 5;
    private static ScheduledExecutorService executorService = null;

    @Override
    public void run() {
        try {
            if (Application.STOP) {
                executorService.shutdownNow();
                return;
            }
            Command command = new Command();
            command.setCommandCode(CommandCode.BREATH);
            command.setData(null);
            List<Node> backupNodes = SyncSystem.getBackupNodes();
            for (Node node : backupNodes) {
                String data = new Gson().toJson(command);
                try {
                    Util.sendDataToNode(node.getAddress(), node.getPort(), data);
                } catch (ConnectionFailedException e) {
                    LOGGER.error("Heart beat failed for node " + node.getAddress() + ":" + node.getPort());
                    LOGGER.error(e.getMessage(), e);
                    if (!dieingNodes.contains(node)) {
                        dieingNodes.add(node);
                    } else {
                        //Send Dead node info to balancer
                        SyncSystem.removeDeadNode(node);
                        Response response = new Response();
                        String nodeStr = new Gson().toJson(node);
                        response.setStatus(Response.DEAD_NODE);
                        response.setData(nodeStr);
                        try {
                            LOGGER.info("Sending dead node message to balancer");
                            Util.sendToBalancer(new Gson().toJson(response));
                            LOGGER.info("Message dead node  sent to balancer");
                        } catch (ConnectionFailedException x) {
                            LOGGER.error(x.getMessage(), x);
                        }
                    }
                }
            }
        }
        catch (Exception x) {
            //Catch block for preventing Scheduler to get killed
            LOGGER.error(x.getMessage(), x);
        }
    }

    private Stethoscope() {}

    /**
     * Starts the {@link Stethoscope} to check heartbeat of backup nodes
     */
    public static void start() {
        executorService = Executors.newScheduledThreadPool(MAX_BREATH_THREAD);
        executorService.scheduleAtFixedRate(new Stethoscope(), FREQUENCY, FREQUENCY * 1000,
                TimeUnit.MILLISECONDS);

    }
}
