package io.github.tlbueno.e2e_tests.activemq.artemis.tests.journal.retention;

import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.RedissonNode;
import org.redisson.api.RExecutorService;
import org.redisson.api.RedissonClient;
import org.redisson.api.annotation.RInject;
import org.redisson.config.Config;
import org.redisson.config.RedissonNodeConfig;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;

public class RedissonTest {

    @Test
    public void RedisTest() throws IOException, InterruptedException {

        Network net = Network.newNetwork();
        GenericContainer<?> redis1 = new GenericContainer<>(DockerImageName.parse("redis:latest"));
        redis1.withExposedPorts(7000);
        redis1.withNetwork(net);
        redis1.withNetworkAliases("redis1");
        redis1.withCommand("redis-server /usr/local/etc/redis/redis.conf");
        redis1.withFileSystemBind("/tmp/redis.conf", "/usr/local/etc/redis/redis.conf");
        redis1.start();

        GenericContainer<?> redis2 = new GenericContainer<>(DockerImageName.parse("redis:latest"));
        redis2.withExposedPorts(7000);
        redis2.withNetwork(net);
        redis2.withNetworkAliases("redis2");
        redis2.withCommand("redis-server /usr/local/etc/redis/redis.conf");
        redis2.withFileSystemBind("/tmp/redis.conf", "/usr/local/etc/redis/redis.conf");
        redis2.start();

        GenericContainer<?> redis3 = new GenericContainer<>(DockerImageName.parse("redis:latest"));
        redis3.withExposedPorts(7000);
        redis3.withNetwork(net);
        redis3.withNetworkAliases("redis3");
        redis3.withCommand("redis-server /usr/local/etc/redis/redis.conf");
        redis3.withFileSystemBind("/tmp/redis.conf", "/usr/local/etc/redis/redis.conf");
        redis3.start();

        Container.ExecResult results = redis1.execInContainer("redis-cli --cluster create redis1:7000 redis2:7000 redis3:7000 --cluster-replicas 1 --cluster-yes");

        Config config = new Config();
        config.useClusterServers().addNodeAddress("redis://127.0.0.0:" + redis1.getMappedPort(7000),
                "redis://127.0.0.0:" + redis2.getMappedPort(7001),
                "redis://127.0.0.0:" + redis3.getMappedPort(7001));

        RedissonClient redisson = Redisson.create(config);

        RedissonNodeConfig nodeConfig = new RedissonNodeConfig(config);
        nodeConfig.setExecutorServiceWorkers(Collections.singletonMap("myExecutor", 1));
        RedissonNode node = RedissonNode.create(nodeConfig);
        node.start();

        RExecutorService e = redisson.getExecutorService("myExecutor");
        e.execute(new RunnableTask());

        e.shutdown();
        node.shutdown();

    }

    public static class RunnableTask implements Runnable, Serializable {

        @RInject
        RedissonClient redisson;

        @Override
        public void run() {
            System.out.println("Remote redis test!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }

    }
}
