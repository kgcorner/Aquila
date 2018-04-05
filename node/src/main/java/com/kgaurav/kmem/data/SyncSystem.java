package com.kgaurav.kmem.data;

import com.kgaurav.kmem.model.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 3/23/2018.
 */
public class SyncSystem implements Runnable {
    private static final List<Node> backupNodes = new ArrayList<>();

    @Override
    public void run() {

    }

    public static void addBackupNode(Node node) {
        backupNodes.add(node);
    }
}


