package io.github.tlbueno.e2e_tests.framework.client.ignite;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public final class IgniteClient {

    private IgniteClient() {
        super();
    }

    public static Ignite getIgniteClient(List<String> servers) {
        Path igniteTempDir;
        try {
            igniteTempDir = Files.createTempDirectory("ignite-");
        } catch (IOException e) {
            var errMsg = "error on creating temporary directory: " + e.getMessage();
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }

        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setWorkDirectory(igniteTempDir.toAbsolutePath().toString());
        cfg.setClientMode(true);
        cfg.setPeerClassLoadingEnabled(true);

        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(servers);
        cfg.setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(ipFinder));

        // Starting the client node
        return Ignition.start(cfg);
    }

}
