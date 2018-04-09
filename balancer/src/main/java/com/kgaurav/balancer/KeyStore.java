package com.kgaurav.balancer;


import com.kgaurav.balancer.model.Node;
import com.kgaurav.balancer.model.NodeInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class KeyStore {

    private static final Map<String, String> INTERNAL_ID_MAP = new TreeMap<>();
    private static final Map<Integer, NodeWrapper> NODE_MAP = new HashMap<>();
    private static final KeyStore INSTANCE = new KeyStore();
    private static int nextIndex;

    private KeyStore() {
        List<Node> mainNodes = BalancerServer.getInstance().getMainNodes();
        int index = 0;
        for (Node node: mainNodes) {
            NodeWrapper nodeWrapper = new NodeWrapper();
            nodeWrapper.setKeyCount(0);
            nodeWrapper.setNode(node);
            NODE_MAP.put(index, nodeWrapper);
            index++;
        }
        nextIndex = 0;
    }

    public static KeyStore getInstance() {
        return INSTANCE;
    }

    /**
     * Returns NodeInfo containing data who's key is given
     * @param key
     * @return
     */
    public NodeInfo getNodeInfo(String key) {
        if(!INTERNAL_ID_MAP.containsKey(key)) {
            return null;
        }
        String internalId = INTERNAL_ID_MAP.get(key);
        int nodeIndex = Integer.parseInt(internalId.substring(internalId.lastIndexOf(".")), internalId.length());
        if(!NODE_MAP.containsKey(nodeIndex)) {
            return null;
        }
        Node node = NODE_MAP.get(nodeIndex).getNode();
        NodeInfo info = new NodeInfo(internalId, node);
        return info;
    }

    /**
     * Stores a key and returns node info for same
     * @param key
     * @return
     */
    public NodeInfo storeKey(String key) {
        String internalId = Util.generateInternalKey();
        internalId += ("."+nextIndex);
        NodeWrapper wrapper = NODE_MAP.get(nextIndex);
        wrapper.setKeyCount(wrapper.getKeyCount()+1);
        Node node = wrapper.getNode();
        nextIndex = (nextIndex+1) == NODE_MAP.size() ? 0 : (nextIndex+1);
        return new NodeInfo(internalId, node);
    }



    class NodeWrapper implements Comparable<NodeWrapper>{
        private Node node;
        private int keyCount;

        public Node getNode() {
            return node;
        }

        public void setNode(Node node) {
            this.node = node;
        }

        public int getKeyCount() {
            return keyCount;
        }

        public void setKeyCount(int keyCount) {
            this.keyCount = keyCount;
        }

        @Override
        public int compareTo(NodeWrapper o) {
            return keyCount - o.keyCount;
        }
    }
}
