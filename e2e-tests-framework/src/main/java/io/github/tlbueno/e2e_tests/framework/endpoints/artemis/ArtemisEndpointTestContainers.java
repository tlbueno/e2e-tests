package io.github.tlbueno.e2e_tests.framework.endpoints.artemis;

import io.github.tlbueno.e2e_tests.framework.common.configuration.Configuration;
import io.github.tlbueno.e2e_tests.framework.common.helper.FileHelper;
import io.github.tlbueno.e2e_tests.framework.endpoints.generic.AbstractGenericTestContainersEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.shaded.com.google.common.primitives.Ints;

import java.io.File;
import java.nio.file.Path;

@Slf4j
public final class ArtemisEndpointTestContainers extends AbstractGenericTestContainersEndpoint implements ArtemisEndpoint {

    private static final String BUILD_ARG_BASE_IMAGE = "BASE_IMAGE";
    private static final String CONF_ARTEMIS_INSTANCE_BIN_DIR = "ArtemisEndpoint.TestContainers.Artemis.Instance.Bin.Dir";
    private static final String CONF_ARTEMIS_INSTANCE_DATA_DIR = "ArtemisEndpoint.TestContainers.Artemis.Instance.Data.Dir";
    private static final String CONF_ARTEMIS_INSTANCE_ETC_DIR = "ArtemisEndpoint.TestContainers.Artemis.Instance.Etc.Dir";
    private static final String CONF_ARTEMIS_INSTANCE_INSTALL_DIR = "ArtemisEndpoint.TestContainers.Artemis.Install.Dir";
    private static final String CONF_ARTEMIS_INSTANCE_LIB_DIR = "ArtemisEndpoint.TestContainers.Artemis.Instance.Lib.Dir";
    private static final String CONF_ARTEMIS_INSTANCE_LOG_DIR = "ArtemisEndpoint.TestContainers.Artemis.Instance.Log.Dir";
    private static final String CONF_DOCKER_FILE = "ArtemisEndpoint.TestContainers.DockerFile";
    private static final String CONF_DOCKER_IMAGE = "ArtemisEndpoint.TestContainers.DockerFile.BaseImage";
    private static final String ENV_VAR_JAVA_HOME = "JAVA_HOME";

    public ArtemisEndpointTestContainers() {
        super();
        container = new GenericContainer<>(getDockerImage());
    }

    private static ImageFromDockerfile getDockerImage() {
        String dockerFileFromConf = Configuration.getProperty(CONF_DOCKER_FILE);
        Path dockerFile = FileHelper.getFilenameFromResourcesAsPath(dockerFileFromConf);
        String dockerImageName = Configuration.getProperty(CONF_DOCKER_IMAGE);
        return new ImageFromDockerfile().withDockerfile(dockerFile).withBuildArg(BUILD_ARG_BASE_IMAGE, dockerImageName);
    }

    @Override
    public void withInstallDir(String dirPath) {
        withFileSystemBind(dirPath, Configuration.getProperty(CONF_ARTEMIS_INSTANCE_INSTALL_DIR), BindMode.READ_ONLY);
    }

    @Override
    public void withConfigDir(String dirPath) {
        withFileSystemBind(dirPath, Configuration.getProperty(CONF_ARTEMIS_INSTANCE_ETC_DIR), BindMode.READ_ONLY);
    }

    @Override
    public void withConfigFile(String srcFilePath, String dstFileName) {
        String destination = Configuration.getProperty(CONF_ARTEMIS_INSTANCE_ETC_DIR) + File.separator + dstFileName;
        withFileSystemBind(srcFilePath, destination, BindMode.READ_ONLY);
    }

    @Override
    public void withLibFile(String filePath) {
        String destination = Configuration.getProperty(CONF_ARTEMIS_INSTANCE_LIB_DIR) + File.separator +
                FilenameUtils.getName(filePath);
        withFileSystemBind(filePath, destination, BindMode.READ_ONLY);
    }

    @Override
    public void withDataDir(String dirPath) {
        FileHelper.createDirectories(dirPath);
        withFileSystemBind(dirPath, Configuration.getProperty(CONF_ARTEMIS_INSTANCE_DATA_DIR), BindMode.READ_WRITE);
    }

    @Override
    public void withLogDir(String dirPath) {
        FileHelper.createDirectories(dirPath);
        withFileSystemBind(dirPath, Configuration.getProperty(CONF_ARTEMIS_INSTANCE_LOG_DIR), BindMode.READ_WRITE);
    }

    @Override
    public void withJavaHome(String dirPath) {
        container.withEnv(ENV_VAR_JAVA_HOME, dirPath);
    }

    @Override
    public void start() {
        container.addExposedPorts(Ints.toArray(ArtemisEndpoint.DEFAULT_PORTS));
        withInstallDir(getArtemisInstallDir());
        withJavaHome(getArtemisJavaHomeDir());
        withFileSystemBind(getArtemisDefaultBinResourceDir(), Configuration.getProperty(CONF_ARTEMIS_INSTANCE_BIN_DIR),
                BindMode.READ_ONLY);
        withConfigDir(getArtemisDefaultEtcResourceDir());
        super.start();
    }

}
