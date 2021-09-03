package io.github.tlbueno.e2e_tests.activemq.artemis.tests.journal.retention;

import io.github.tlbueno.e2e_tests.framework.client.ignite.IgniteClient;
import io.github.tlbueno.e2e_tests.framework.common.helper.FileHelper;
import io.github.tlbueno.e2e_tests.framework.endpoints.Endpoint;
import io.github.tlbueno.e2e_tests.framework.endpoints.artemis.ArtemisEndpoint;
import io.github.tlbueno.e2e_tests.framework.endpoints.ignite.IgniteEndpoint;
import org.apache.ignite.Ignite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

class IgniteEndpointTests {

    private static final String TEST_RESOURCES = "journal/retention";
    private static final String ARTEMIS_BROKER_XML = FileHelper.getFilenameFromResources(TEST_RESOURCES + "/broker.xml");

    private static ArtemisEndpoint artemis;
    private static IgniteEndpoint igniteInstance;
    private static Ignite ignite;

    @BeforeAll
    public static void setUpBeforeAll() {
        Object network = Endpoint.getNetwork();
        artemis = Endpoint.getEndpoint(ArtemisEndpoint.class);
        artemis.withNetwork(network);
        artemis.withName(artemis.getArtemisDefaultInstanceName());
        artemis.withConfigFile(ARTEMIS_BROKER_XML, "broker.xml");
        artemis.withSystemDateTime(ZonedDateTime.now());

        igniteInstance = Endpoint.getEndpoint(IgniteEndpoint.class);
        igniteInstance.withNetwork(network);
        igniteInstance.withSystemDateTime(ZonedDateTime.now());
    }

    @BeforeEach
    public void setUpBeforeEach() {
        artemis.start();
        igniteInstance.start();
        ignite = IgniteClient.getIgniteClient(List.of(igniteInstance.getHostAndPort(47500)));
    }

    @AfterEach
    public void tearDownAfterEach() {
        artemis.stop();
        igniteInstance.stop();
    }

    @Test
    void myTest() {
        // Executing custom Java compute task on server nodes.
        final String url = "amqp://artemisInstance:61616";
        String user = artemis.getArtemisDefaultUsername();
        String pass = artemis.getArtemisDefaultPassword();

        ignite.compute(ignite.cluster().forServers()).broadcast(new Producer(url, user, pass));

        ZonedDateTime updatedDate = ZonedDateTime.now().plus(1, ChronoUnit.MONTHS);
        artemis.updateSystemDateTime(updatedDate);
        igniteInstance.updateSystemDateTime(updatedDate);

        ignite.compute(ignite.cluster().forServers()).broadcast(new Producer(url, user, pass));

        ignite.compute(ignite.cluster().forServers()).broadcast(new Producer(url, user, pass));

    }
}
