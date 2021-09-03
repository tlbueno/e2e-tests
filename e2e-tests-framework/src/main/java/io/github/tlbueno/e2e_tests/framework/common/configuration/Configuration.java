package io.github.tlbueno.e2e_tests.framework.common.configuration;

import io.github.tlbueno.e2e_tests.framework.common.helper.FileHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Slf4j
public final class Configuration {

    private static final String DEFAULT_CONFIG_FILE = "artemis-e2e-test-config.properties";
    private static final String CONFIG_FILE_VAR = "artemis-e2e-tests.config";
    private static Properties defaultConfig;
    private static Properties userConfig;

    private Configuration() {
        super();
    }

    private static void load(String configFileName, Properties config) {
        try (FileInputStream file = new FileInputStream(configFileName)) {
            config.load(file);
        } catch (IOException e) {
            String errMsg = "Error loading configuration file: " + configFileName + ": " + e.getMessage();
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
    }

    private static String getUserConfigFile() {
        String configFile;
        log.info("Trying to get config file from environment variable " + CONFIG_FILE_VAR);
        String configFileEnvVar = System.getenv(CONFIG_FILE_VAR);
        if (configFileEnvVar == null) {
            log.info("environment variable not found, trying to get config file from system property " +
                    CONFIG_FILE_VAR);
            String configFileSysProp = System.getProperty(CONFIG_FILE_VAR);
            if (configFileSysProp == null) {
                //TODO: change to a better way to get current directory.
                String userDirectory = Paths.get("").toAbsolutePath().toString();
                log.info("system property not found, using config file from current directory " + userDirectory);
                configFile = userDirectory + File.separator + DEFAULT_CONFIG_FILE;
            } else {
                configFile = configFileSysProp;
            }
        } else {
            configFile = configFileEnvVar;
        }
        String absoluteConfigFile = null;
        Path configFilePath = Paths.get(configFile);
        if (Files.exists(configFilePath) && Files.isReadable(configFilePath)) {
            absoluteConfigFile = configFilePath.toAbsolutePath().toString();
        }
        return absoluteConfigFile;
    }

    private static String getDefaultConfigFile() {
        return FileHelper.getFilenameFromResources(DEFAULT_CONFIG_FILE);
    }

    public static String getProperty(String propertyName) {
        if (defaultConfig == null) {
            defaultConfig = new Properties();
            load(getDefaultConfigFile(), defaultConfig);
        }
        if (userConfig == null) {
            userConfig = new Properties();
            String userConfigFile = getUserConfigFile();
            if (userConfigFile != null) {
                load(getUserConfigFile(), userConfig);
            } else {
                log.info("User config file not found or is not readable. Using defaults values for configuration");
            }
        }
        String envProp = System.getenv(propertyName);
        if (envProp != null) {
            return envProp;
        }
        String sysProp = System.getProperty(propertyName);
        if (sysProp != null) {
            return sysProp;
        }
        String userProp = userConfig.getProperty(propertyName);
        if (userProp != null) {
            return userProp;
        }
        return defaultConfig.getProperty(propertyName);
    }

}
