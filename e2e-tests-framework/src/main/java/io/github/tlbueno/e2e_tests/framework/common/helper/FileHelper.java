package io.github.tlbueno.e2e_tests.framework.common.helper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public final class FileHelper {

    private FileHelper() {
        super();
    }

    public static String getFilenameFromResources(String filePath) {
        String convertedFilePath = FilenameUtils.separatorsToSystem(filePath);
        return getFilenameFromResourcesAsPath(convertedFilePath).toString();
    }

    public static Path getFilenameFromResourcesAsPath(String filePath) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(filePath);
        if (resource == null) {
            String errMsg = "File not found in resources: " + filePath;
            RuntimeException e = new RuntimeException(errMsg);
            log.error(errMsg, e);
            throw e;
        }
        return Paths.get(resource.getFile()).toAbsolutePath();
    }

    public static void createDirectories(String path) {
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            String errMsg = "Unable to create directories: " + e.getMessage();
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
    }

}
