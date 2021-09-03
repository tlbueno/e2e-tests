package io.github.tlbueno.e2e_tests.framework.endpoints.zookeeper;

import io.github.tlbueno.e2e_tests.framework.endpoints.Endpoint;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ZookeeperEndpointCluster {

    private static final String REPLACEMENT_NODE_NAME = "%NODE_NAME%";
    private static final String REPLACEMENT_ID = "%ID%";
    private static final String ZOO_SERVERS_PATTERN = "server." + REPLACEMENT_ID + "=" + REPLACEMENT_NODE_NAME + ":" +
            ZookeeperEndpoint.FOLLOWER_PORT + ":" + ZookeeperEndpoint.ELECTION_PORT + ";" +
            ZookeeperEndpoint.CLIENT_PORT;

    private final Map<String, ZookeeperEndpoint> nodesMap = new HashMap<>();
    private final List<String> zooServers = new ArrayList<>();


    public ZookeeperEndpointCluster(int clusterSize, String nodeNamePrefix) {
        for (int i = 1; i <= clusterSize; i++) {
            createNode(i, nodeNamePrefix);
        }
    }

    public void start() {
        nodesMap.forEach((String name, ZookeeperEndpoint node) -> {
            node.withZooServers(String.join(" ", zooServers));
            node.start();
        });
    }

    public void stop() {
        nodesMap.forEach((name, node) -> node.stop());
    }

    public void withNetwork(Object network) {
        nodesMap.forEach((name, node) -> node.withNetwork(network));
    }

    private void createNode(int id, String name) {
        String nodeName = name + id;
        ZookeeperEndpoint node = Endpoint.getEndpoint(ZookeeperEndpoint.class);
        node.withName(nodeName);
        node.withZooMyId(id);
        node.withLogWait(ZookeeperEndpoint.ADMIN_SERVER_LOG_REGEX);
        node.withStandAloneEnabled(false);
        String zooServerWithId = StringUtils.replace(ZOO_SERVERS_PATTERN, REPLACEMENT_ID, String.valueOf(id));
        String zooServerWithIdAndNodeName = StringUtils.replace(zooServerWithId, REPLACEMENT_NODE_NAME, name);
        zooServers.add(zooServerWithIdAndNodeName);
        nodesMap.put(nodeName, node);
    }

}
