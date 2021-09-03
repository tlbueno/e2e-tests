package io.github.tlbueno.e2e_tests.framework.endpoints.generic;

import com.github.dockerjava.api.model.Bind;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.com.google.common.primitives.Ints;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public abstract class AbstractGenericTestContainersEndpoint implements GenericEndpoint {


    private static final String ENV_VAR_FAKETIME_DONT_FAKE_MONOTONIC = "FAKETIME_DONT_FAKE_MONOTONIC";
    private static final String ENV_VAR_FAKETIME_DONT_FAKE_MONOTONIC_VALUE = "1";
    private static final String ENV_VAR_FAKETIME_DONT_RESET = "FAKETIME_DONT_RESET";
    private static final String ENV_VAR_FAKETIME_DONT_RESET_VALUE = "1";
    private static final String ENV_VAR_FAKETIME_NO_CACHE = "FAKETIME_NO_CACHE";
    private static final String ENV_VAR_FAKETIME_NO_CACHE_VALUE = "1";
    private static final String ENV_VAR_FAKETIME_TIMESTAMP_FILE = "FAKETIME_TIMESTAMP_FILE";
    private static final String ENV_VAR_LD_PRELOAD = "LD_PRELOAD";
    private static final String ENV_VAR_LD_PRELOAD_VALUE = "/usr/lib64/faketime/libfaketimeMT.so.1";
    private static final String FAKETIME_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String FAKETIME_DATE_TIME_FORMAT_PREFIX = "@";
    private static final String FAKETIME_FILENAME = "/tmp/faketime.rc";
    private static final String FAKETIME_TEMP_FILE_PREFIX = "faketime-";
    private static final String FAKETIME_TEMP_FILE_SUFFIX = ".rc";

    private final Slf4jLogConsumer logConsumer;

    protected GenericContainer<?> container;

    private String name;
    private Path tempFaketimeFile;

    protected AbstractGenericTestContainersEndpoint() {
        super();
        logConsumer = new Slf4jLogConsumer(log);
    }

    @Override
    public void withName(String name) {
        this.name = name;
    }

    @Override
    public void withNetwork(Object network) {
        container.withNetwork((Network) network);
    }

    @Override
    public void withTcpPorts(List<Integer> ports) {
        container.addExposedPorts(Ints.toArray(ports));
    }

    @Override
    public void withLogWait(String regex) {
        container.setWaitStrategy(Wait.forLogMessage(regex, 1));
    }

    @Override
    public void withSystemDateTime(ZonedDateTime zonedDateTime) {
        String dateTime = zonedDateTime.format(DateTimeFormatter.ofPattern(FAKETIME_DATE_TIME_FORMAT));
        try {
            tempFaketimeFile = Files.createTempFile(FAKETIME_TEMP_FILE_PREFIX, FAKETIME_TEMP_FILE_SUFFIX);
            String fakeTimeFormat = FAKETIME_DATE_TIME_FORMAT_PREFIX + dateTime;
            Files.write(tempFaketimeFile, fakeTimeFormat.getBytes(StandardCharsets.UTF_8));
            withFileSystemBind(tempFaketimeFile.toAbsolutePath().toString(), FAKETIME_FILENAME, BindMode.READ_WRITE);
        } catch (IOException e) {
            String errMsg = "Unable to set faketime file: " + e.getMessage();
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
        container.withEnv(ENV_VAR_LD_PRELOAD, ENV_VAR_LD_PRELOAD_VALUE);
        container.withEnv(ENV_VAR_FAKETIME_DONT_RESET, ENV_VAR_FAKETIME_DONT_RESET_VALUE);
        container.withEnv(ENV_VAR_FAKETIME_NO_CACHE, ENV_VAR_FAKETIME_NO_CACHE_VALUE);
        container.withEnv(ENV_VAR_FAKETIME_DONT_FAKE_MONOTONIC, ENV_VAR_FAKETIME_DONT_FAKE_MONOTONIC_VALUE);
        container.withEnv(ENV_VAR_FAKETIME_TIMESTAMP_FILE, FAKETIME_FILENAME);
    }

    @Override
    public void start() {
        if (name != null && !name.isEmpty()) {
            container.withCreateContainerCmdModifier(cmd -> cmd.withHostName(name));
            container.withNetworkAliases(name);
        }
        withStdOutLog();
        container.start();
    }

    @Override
    public void stop() {
        container.stop();
    }

    @Override
    public void pause() {
        container.getDockerClient().pauseContainerCmd(container.getContainerId()).exec();
    }

    @Override
    public void unpause() {
        container.getDockerClient().unpauseContainerCmd(container.getContainerId()).exec();
    }

    @Override
    public void kill() {
        container.getDockerClient().killContainerCmd(container.getContainerId()).exec();
        container.stop();
    }

    @Override
    public void restartWithStop() {
        container.stop();
        container.start();
    }

    @Override
    public void restartWithKill() {
        kill();
        container.start();
    }

    @Override
    public void updateSystemDateTime(ZonedDateTime zonedDateTime) {
        String dateTime = zonedDateTime.format(DateTimeFormatter.ofPattern(FAKETIME_DATE_TIME_FORMAT));
        String fakeTimeFormat = FAKETIME_DATE_TIME_FORMAT_PREFIX + dateTime;
        try {
            Files.write(tempFaketimeFile, fakeTimeFormat.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            String errMsg = "unable to set time on faketime file: " + e.getMessage();
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
    }

    @Override
    public String getHost() {
        return container.getHost();
    }

    @Override
    public int getPort(int port) {
        return container.getMappedPort(port);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getStatus() {
        return container.getDockerClient().inspectContainerCmd(container.getContainerId()).exec().getState().
                getStatus();
    }

    @Override
    public String getHostAndPort(int port) {
        return container.getHost() + ":" + getPort(port);
    }

    private void withStdOutLog() {
        if (container.getLogConsumers().contains(logConsumer)) {
            return;
        }
        if (name != null && !name.isEmpty()) {
            logConsumer.withPrefix(name);
        }
        container.withLogConsumer(logConsumer);
    }

    protected void withFileSystemBind(String source, String destination, BindMode mode) {
        List<Bind> currentBinds = container.getBinds();
        boolean alreadyContainsBind = currentBinds.stream().anyMatch(p -> p.getVolume().getPath().equals(destination));
        if (!alreadyContainsBind) {
            container.withFileSystemBind(source, destination, mode);
        } else {
            log.warn("Ignoring bind " + destination + " as it already exist");
        }
    }

}
