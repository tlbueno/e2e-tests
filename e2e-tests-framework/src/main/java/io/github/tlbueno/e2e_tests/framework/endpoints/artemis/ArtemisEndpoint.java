package io.github.tlbueno.e2e_tests.framework.endpoints.artemis;

import io.github.tlbueno.e2e_tests.framework.common.configuration.Configuration;
import io.github.tlbueno.e2e_tests.framework.common.helper.FileHelper;
import io.github.tlbueno.e2e_tests.framework.endpoints.generic.GenericEndpoint;

import java.util.List;

public interface ArtemisEndpoint extends GenericEndpoint {

    String BACKUP_ANNOUNCED_LOG_REGEX = ".*AMQ221031: backup announced\\n";

    int DEFAULT_ALL_PROTOCOLS_PORT = 61616;
    int DEFAULT_AMQP_PORT = 5672;
    int DEFAULT_HORNETQ_PORT = 5445;
    int DEFAULT_JMX_PORT = 1099;
    int DEFAULT_JVM_DEBUGGER = 5005;
    int DEFAULT_MQTT_PORT = 1883;
    int DEFAULT_STOMP_PORT = 61613;
    int DEFAULT_WEB_CONSOLE_PORT = 8161;
    List<Integer> DEFAULT_PORTS = List.of(DEFAULT_ALL_PROTOCOLS_PORT, DEFAULT_AMQP_PORT, DEFAULT_MQTT_PORT, DEFAULT_JVM_DEBUGGER, DEFAULT_STOMP_PORT,
            DEFAULT_HORNETQ_PORT, DEFAULT_WEB_CONSOLE_PORT, DEFAULT_JMX_PORT);

    String CONF_ARTEMIS_DEFAULT_DIR = "ArtemisEndpoint.Artemis.Defaults.Dir";
    String CONF_ARTEMIS_DEFAULT_INSTANCE_NAME = "ArtemisEndpoint.Artemis.Defaults.InstanceName";
    String CONF_ARTEMIS_DEFAULT_PASSWORD = "ArtemisEndpoint.Artemis.Defaults.Password";
    String CONF_ARTEMIS_DEFAULT_USERNAME = "ArtemisEndpoint.Artemis.Defaults.UserName";
    String CONF_ARTEMIS_INSTALL_DIR = "ArtemisEndpoint.Artemis.Install.Dir";
    String CONF_ARTEMIS_JAVA_HOME_DIR = "ArtemisEndpoint.Java.Home";

    String BIN_DIR = "/bin";
    String ETC_DIR = "/etc";

    void withInstallDir(String dirPath);

    void withConfigDir(String dirPath);

    void withConfigFile(String srcFilePath, String dstFileName);

    void withLibFile(String filePath);

    void withDataDir(String dirPath);

    void withLogDir(String dirPath);

    void withJavaHome(String dirPath);

    default String getArtemisInstallDir() {
        return Configuration.getProperty(CONF_ARTEMIS_INSTALL_DIR);
    }

    default String getArtemisDefaultBinResourceDir() {
        return FileHelper.getFilenameFromResources(Configuration.getProperty(CONF_ARTEMIS_DEFAULT_DIR) + BIN_DIR);
    }

    default String getArtemisDefaultEtcResourceDir() {
        return FileHelper.getFilenameFromResources(Configuration.getProperty(CONF_ARTEMIS_DEFAULT_DIR) + ETC_DIR);
    }

    default String getArtemisDefaultInstanceName() {
        return Configuration.getProperty(CONF_ARTEMIS_DEFAULT_INSTANCE_NAME);
    }

    default String getArtemisDefaultUsername() {
        return Configuration.getProperty(CONF_ARTEMIS_DEFAULT_USERNAME);
    }

    default String getArtemisDefaultPassword() {
        return Configuration.getProperty(CONF_ARTEMIS_DEFAULT_PASSWORD);
    }

    default String getArtemisJavaHomeDir() {
        return Configuration.getProperty(CONF_ARTEMIS_JAVA_HOME_DIR);
    }

}
