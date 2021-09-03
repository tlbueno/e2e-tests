package io.github.tlbueno.e2e_tests.framework.endpoints.zookeeper;

import io.github.tlbueno.e2e_tests.framework.endpoints.generic.GenericEndpoint;

import java.util.List;

public interface ZookeeperEndpoint extends GenericEndpoint {

    int ADMIN_SERVER_PORT = 8080;
    int CLIENT_PORT = 2181;
    int ELECTION_PORT = 3888;
    int FOLLOWER_PORT = 2888;
    List<Integer> DEFAULT_PORTS = List.of(CLIENT_PORT, FOLLOWER_PORT, ELECTION_PORT, ADMIN_SERVER_PORT);

    String ADMIN_SERVER_LOG_REGEX = ".*Started AdminServer on address.*\\n";

    void withZooMyId(int id);

    void withZooServers(String zooServers);

    void withStandAloneEnabled(boolean value);

}
