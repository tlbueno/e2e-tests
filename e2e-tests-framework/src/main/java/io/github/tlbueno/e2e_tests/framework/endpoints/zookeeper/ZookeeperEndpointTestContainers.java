package io.github.tlbueno.e2e_tests.framework.endpoints.zookeeper;

import io.github.tlbueno.e2e_tests.framework.common.configuration.Configuration;
import io.github.tlbueno.e2e_tests.framework.endpoints.generic.AbstractGenericTestContainersEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.com.google.common.primitives.Ints;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public final class ZookeeperEndpointTestContainers extends AbstractGenericTestContainersEndpoint implements ZookeeperEndpoint {

    private static final String CONF_DOCKER_IMAGE = "ZookeeperEndPoint.TestContainers.DockerImage";
    private static final String ENV_VAR_ZOO_MY_ID = "ZOO_MY_ID";
    private static final String ENV_VAR_ZOO_SERVERS = "ZOO_SERVERS";
    private static final String ENV_VAR_ZOO_STANDALONE_ENABLED = "ZOO_STANDALONE_ENABLED";

    public ZookeeperEndpointTestContainers() {
        super();
        String dockerImage = Configuration.getProperty(CONF_DOCKER_IMAGE);
        container = new GenericContainer<>(DockerImageName.parse(dockerImage));
        container.addExposedPorts(Ints.toArray(ZookeeperEndpoint.DEFAULT_PORTS));
    }

    @Override
    public void withZooMyId(int id) {
        container.withEnv(ENV_VAR_ZOO_MY_ID, String.valueOf(id));
    }

    @Override
    public void withZooServers(String zooServers) {
        container.withEnv(ENV_VAR_ZOO_SERVERS, zooServers);
    }

    @Override
    public void withStandAloneEnabled(boolean value) {
        container.withEnv(ENV_VAR_ZOO_STANDALONE_ENABLED, Boolean.toString(value));
    }

}
