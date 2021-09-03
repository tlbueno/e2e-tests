package io.github.tlbueno.e2e_tests.framework.endpoints.generic;

import java.time.ZonedDateTime;
import java.util.List;

public interface GenericEndpoint {

    void withName(String name);

    void withNetwork(Object network);

    void withTcpPorts(List<Integer> ports);

    void withLogWait(String regex);

    void withSystemDateTime(ZonedDateTime zonedDateTime);

    void start();

    void stop();

    void pause();

    void unpause();

    void kill();

    void restartWithStop();

    void restartWithKill();

    void updateSystemDateTime(ZonedDateTime zonedDateTime);

    String getHost();

    int getPort(int port);

    String getName();

    String getStatus();

    String getHostAndPort(int port);

}
