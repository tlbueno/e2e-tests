package io.github.tlbueno.e2e_tests.framework.endpoints.ignite;

import io.github.tlbueno.e2e_tests.framework.common.configuration.Configuration;
import io.github.tlbueno.e2e_tests.framework.common.helper.FileHelper;
import io.github.tlbueno.e2e_tests.framework.endpoints.generic.AbstractGenericTestContainersEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Path;

@Slf4j
public class IgniteEndpointTestContainers extends AbstractGenericTestContainersEndpoint implements IgniteEndpoint {

    private static final String ENV_VAR_CONFIG_URI = "CONFIG_URI";
    private static final String CONF_DOCKER_FILE = "IgniteEndpoint.TestContainers.DockerFile";
    private static final String CONF_IGNITE_INSTANCE_CONFIG_FILE = "IgniteEndpoint.Instance.ConfigFile";

    public IgniteEndpointTestContainers() {
        super();
        container = new GenericContainer<>(getDockerImage());
    }

    private static ImageFromDockerfile getDockerImage() {
        String dockerFileFromConf = Configuration.getProperty(CONF_DOCKER_FILE);
        Path dockerFile = FileHelper.getFilenameFromResourcesAsPath(dockerFileFromConf);
        return new ImageFromDockerfile().withDockerfile(dockerFile);
    }

    @Override
    public void start() {
        String srcFileFromConf = Configuration.getProperty(IgniteEndpoint.CONF_CONFIG_FILE);
        String srcFile = FileHelper.getFilenameFromResources(srcFileFromConf);
        String dstFile = Configuration.getProperty(CONF_IGNITE_INSTANCE_CONFIG_FILE);
        withFileSystemBind(srcFile, dstFile, BindMode.READ_ONLY);
        container.withEnv(ENV_VAR_CONFIG_URI, dstFile);
        withTcpPorts(IgniteEndpoint.DEFAULT_PORTS);
        super.start();
    }

}
