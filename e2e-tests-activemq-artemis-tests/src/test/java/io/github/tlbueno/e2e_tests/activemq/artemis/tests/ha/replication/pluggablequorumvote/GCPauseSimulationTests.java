package io.github.tlbueno.e2e_tests.activemq.artemis.tests.ha.replication.pluggablequorumvote;

import io.github.tlbueno.e2e_tests.framework.client.jms.JmsClient;
import io.github.tlbueno.e2e_tests.framework.client.jms.protocol.amqp.JmsClientAmqpBuilder;
import io.github.tlbueno.e2e_tests.framework.common.helper.ArtemisJmxHelper;
import io.github.tlbueno.e2e_tests.framework.common.helper.FileHelper;
import io.github.tlbueno.e2e_tests.framework.common.helper.TimeHelper;
import io.github.tlbueno.e2e_tests.framework.endpoints.Endpoint;
import io.github.tlbueno.e2e_tests.framework.endpoints.artemis.ArtemisEndpoint;
import io.github.tlbueno.e2e_tests.framework.endpoints.zookeeper.ZookeeperEndpointCluster;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class GCPauseSimulationTests {

    private static final String TEST_RESOURCES = "ha/replication/pluggablequorumvote";
    private static final String PRIMARY_TEST_RESOURCES = GCPauseSimulationTests.TEST_RESOURCES + "/primary";
    private static final String PRIMARY_TEST_RESOURCES_DATA = GCPauseSimulationTests.PRIMARY_TEST_RESOURCES + "/data";
    private static final String PRIMARY_TEST_RESOURCES_ETC = GCPauseSimulationTests.PRIMARY_TEST_RESOURCES + "/etc";
    private static final String ARTEMIS_PRIMARY_DATA = FileHelper.getFilenameFromResources(GCPauseSimulationTests.PRIMARY_TEST_RESOURCES_DATA);
    private static final String ARTEMIS_PRIMARY_BROKER_XML = FileHelper.getFilenameFromResources(GCPauseSimulationTests.PRIMARY_TEST_RESOURCES_ETC + "/broker.xml");

    private static final String BACKUP_TEST_RESOURCES = GCPauseSimulationTests.TEST_RESOURCES + "/backup";
    private static final String BACKUP_TEST_RESOURCES_DATA = GCPauseSimulationTests.BACKUP_TEST_RESOURCES + "/data";
    private static final String BACKUP_TEST_RESOURCES_ETC = GCPauseSimulationTests.BACKUP_TEST_RESOURCES + "/etc";
    private static final String ARTEMIS_BACKUP_DATA = FileHelper.getFilenameFromResources(GCPauseSimulationTests.BACKUP_TEST_RESOURCES_DATA);
    private static final String ARTEMIS_BACKUP_BROKER_XML = FileHelper.getFilenameFromResources(GCPauseSimulationTests.BACKUP_TEST_RESOURCES_ETC + "/broker.xml");
    private static final String CLIENT_QUEUE_NAME = "queueA";
    private static final String TEXT_MESSAGE = "test message - ";
    private static ZookeeperEndpointCluster zkCluster;
    private static ArtemisEndpoint artemisPrimary;
    private static ArtemisEndpoint artemisBackup;

    @BeforeAll
    public static void setUpBeforeAll() {
        Object network = Endpoint.getNetwork();

        GCPauseSimulationTests.zkCluster = new ZookeeperEndpointCluster(3, "zk");
        GCPauseSimulationTests.zkCluster.withNetwork(network);
        GCPauseSimulationTests.zkCluster.start();

        GCPauseSimulationTests.artemisPrimary = Endpoint.getEndpoint(ArtemisEndpoint.class);
        GCPauseSimulationTests.artemisPrimary.withNetwork(network);
        GCPauseSimulationTests.artemisPrimary.withName("artemisPrimary");
        GCPauseSimulationTests.artemisPrimary.withConfigFile(GCPauseSimulationTests.ARTEMIS_PRIMARY_BROKER_XML, "broker.xml");
        GCPauseSimulationTests.artemisPrimary.withDataDir(GCPauseSimulationTests.ARTEMIS_PRIMARY_DATA);

        GCPauseSimulationTests.artemisBackup = Endpoint.getEndpoint(ArtemisEndpoint.class);
        GCPauseSimulationTests.artemisBackup.withNetwork(network);
        GCPauseSimulationTests.artemisBackup.withName("artemisBackup");
        GCPauseSimulationTests.artemisBackup.withConfigFile(GCPauseSimulationTests.ARTEMIS_BACKUP_BROKER_XML, "broker.xml");
        GCPauseSimulationTests.artemisBackup.withDataDir(GCPauseSimulationTests.ARTEMIS_BACKUP_DATA);
        GCPauseSimulationTests.artemisBackup.withLogWait(ArtemisEndpoint.BACKUP_ANNOUNCED_LOG_REGEX);
    }

    @AfterAll
    public static void tearDownAfterAll() {
        GCPauseSimulationTests.zkCluster.stop();
    }

    private static int produce(int numOfMessages, int lastProduced) {
        JmsClient client = new JmsClientAmqpBuilder(GCPauseSimulationTests.getClientURL()).
                withUsername(artemisPrimary.getArtemisDefaultUsername()).
                withPassword(artemisPrimary.getArtemisDefaultPassword()).
                withTransactedSession(true).
                withAckMode(Session.SESSION_TRANSACTED).
                build();
        client.connect();
        Session session = client.getSession();

        int producedCounter = 0;
        try {
            Queue producerQueue = session.createQueue(GCPauseSimulationTests.CLIENT_QUEUE_NAME);
            MessageProducer producer = session.createProducer(producerQueue);

            while (producedCounter < numOfMessages) {
                producedCounter++;
                TextMessage message = session.createTextMessage(GCPauseSimulationTests.TEXT_MESSAGE + (lastProduced + producedCounter));
                message.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
                producer.send(message);
                GCPauseSimulationTests.log.info("produced " + message.getText());
                GCPauseSimulationTests.log.info("last produced: " + (lastProduced + producedCounter));
            }
            session.commit();
            session.close();
        } catch (JMSException e) {
            String errMsg = "Error on producing message: " + e.getMessage();
            GCPauseSimulationTests.log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
        return producedCounter;
    }

    private static int consume(int numOfMessages, int lastConsumed) {
        JmsConnectionFactory consumerCF = new JmsConnectionFactory(GCPauseSimulationTests.getClientURL());
        int consumedCounter = 0;
        try {
            Connection consumerConnection = consumerCF.createConnection(artemisPrimary.getArtemisDefaultUsername(), artemisPrimary.getArtemisDefaultPassword());
            Session consumerSession = consumerConnection.createSession(true, Session.SESSION_TRANSACTED);
            Queue consumerQueue = consumerSession.createQueue(GCPauseSimulationTests.CLIENT_QUEUE_NAME);
            MessageConsumer consumer = consumerSession.createConsumer(consumerQueue);
            consumerConnection.start();

            while (consumedCounter < numOfMessages) {
                TextMessage message = (TextMessage) consumer.receive(5000);
                consumedCounter++;
                assertThat(message).as("ensure consumed message is not null").isNotNull();
                assertThat(message.getText()).as("ensure consumed message is correct").isEqualTo(GCPauseSimulationTests.TEXT_MESSAGE + (lastConsumed + consumedCounter));
                GCPauseSimulationTests.log.info("consumed " + message.getText());
                GCPauseSimulationTests.log.info("last consumed: " + (lastConsumed + consumedCounter));
            }
            consumerSession.commit();
            consumerConnection.close();
        } catch (JMSException e) {
            String errMsg = "Error on consuming message: " + e.getMessage();
            GCPauseSimulationTests.log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
        return consumedCounter;
    }

    private static String getClientURL() {
        return "failover:(amqp://" + GCPauseSimulationTests.artemisPrimary.getHostAndPort(ArtemisEndpoint.DEFAULT_ALL_PROTOCOLS_PORT) + ",amqp://" + GCPauseSimulationTests.artemisBackup.getHostAndPort(ArtemisEndpoint.DEFAULT_ALL_PROTOCOLS_PORT) + ")?failover.amqpOpenServerListAction=IGNORE";
    }

    @BeforeEach
    public void setUpBeforeEach() {
        GCPauseSimulationTests.artemisPrimary.start();
        GCPauseSimulationTests.artemisBackup.start();
    }

    @AfterEach
    public void tearDownAfterEach() throws IOException {
        try {
            GCPauseSimulationTests.artemisBackup.stop();
            GCPauseSimulationTests.artemisPrimary.stop();
        } finally {
            FileUtils.cleanDirectory(new File(GCPauseSimulationTests.ARTEMIS_BACKUP_DATA));
            FileUtils.cleanDirectory(new File(GCPauseSimulationTests.ARTEMIS_PRIMARY_DATA));
        }
    }

    @Test
    void testScenario1a() {
        int lastProducedMessage = 0;
        int lastConsumedMessage = 0;
        final int totalMessages = 600;

        // ensure artemisPrimary is the live
        assertThat(ArtemisJmxHelper.isLive(GCPauseSimulationTests.artemisPrimary, 100, 5000)).isTrue();

        // ensure artemisBackup is the backup
        assertThat(ArtemisJmxHelper.isBackup(GCPauseSimulationTests.artemisBackup, 100, 5000)).isTrue();

        // client start to produce message on primary
        lastProducedMessage = GCPauseSimulationTests.produce(totalMessages / 2, lastProducedMessage);
        assertThat(lastProducedMessage).isEqualTo(totalMessages / 2);

        // ensure client can consume (just a few) messages from primary
        lastConsumedMessage = GCPauseSimulationTests.consume(lastProducedMessage / 3, lastConsumedMessage);
        assertThat(lastConsumedMessage).isEqualTo(lastProducedMessage / 3);

        // ensure replica is in sync
        assertThat(ArtemisJmxHelper.isReplicaInSync(GCPauseSimulationTests.artemisPrimary, 100, 5000)).isTrue();

        // pause the artemisPrimary
        GCPauseSimulationTests.artemisPrimary.pause();
        assertThat(GCPauseSimulationTests.artemisPrimary.getStatus()).isEqualTo("paused");

        // ensure artemisBackup became the live
        assertThat(ArtemisJmxHelper.isLive(GCPauseSimulationTests.artemisBackup, 100, 40000)).isTrue();

        // client should be able to produce messages to backup
        lastProducedMessage += GCPauseSimulationTests.produce(totalMessages / 2, lastProducedMessage);
        assertThat(lastProducedMessage).isEqualTo(totalMessages);

        // client should be able to consume messages from backup
        lastConsumedMessage += GCPauseSimulationTests.consume(lastProducedMessage / 6, lastConsumedMessage);
        assertThat(lastConsumedMessage).isEqualTo(lastProducedMessage / 3);

        // unpause the artemisPrimary
        GCPauseSimulationTests.artemisPrimary.unpause();
        assertThat(GCPauseSimulationTests.artemisPrimary.getStatus()).isEqualTo("running");

        // artemisPrimary should shutdown yourself as the journal has changed since it was paused.
        boolean primaryExited = TimeHelper.timeout(e -> "exited".equals(GCPauseSimulationTests.artemisPrimary.getStatus()), 500, 5000);
        assertThat(primaryExited).isTrue();

        // restart artemisPrimary
        GCPauseSimulationTests.artemisPrimary.restartWithStop();

        // ensure artemisPrimary is the live
        assertThat(ArtemisJmxHelper.isLive(GCPauseSimulationTests.artemisPrimary, 100, 20000)).isTrue();

        // ensure artemisBackup is the backup
        assertThat(ArtemisJmxHelper.isBackup(GCPauseSimulationTests.artemisBackup, 100, 5000)).isTrue();

        // ensure replica is in sync
        assertThat(ArtemisJmxHelper.isReplicaInSync(GCPauseSimulationTests.artemisPrimary, 100, 5000)).isTrue();

        // ensure all remaining produced messages were consumed from primary.
        lastConsumedMessage += GCPauseSimulationTests.consume((lastProducedMessage - lastConsumedMessage), lastConsumedMessage);
        assertThat(lastConsumedMessage).isEqualTo(totalMessages);
    }

    @Test
    void testScenario1b() {
        int lastProducedMessage = 0;
        int lastConsumedMessage = 0;
        final int totalMessages = 600;

        // ensure artemisPrimary is the live
        assertThat(ArtemisJmxHelper.isLive(GCPauseSimulationTests.artemisPrimary, 100, 5000)).isTrue();

        // ensure artemisBackup is the backup
        assertThat(ArtemisJmxHelper.isBackup(GCPauseSimulationTests.artemisBackup, 100, 5000)).isTrue();

        // client start to produce message on primary
        lastProducedMessage = GCPauseSimulationTests.produce(totalMessages / 2, lastProducedMessage);
        assertThat(lastProducedMessage).isEqualTo(totalMessages / 2);

        // ensure client can consume (just a few) messages from primary
        lastConsumedMessage = GCPauseSimulationTests.consume(lastProducedMessage / 3, lastConsumedMessage);
        assertThat(lastConsumedMessage).isEqualTo(lastProducedMessage / 3);

        // ensure replica is in sync
        assertThat(ArtemisJmxHelper.isReplicaInSync(GCPauseSimulationTests.artemisPrimary, 100, 5000)).isTrue();

        // pause the artemisPrimary
        GCPauseSimulationTests.artemisPrimary.pause();
        assertThat(GCPauseSimulationTests.artemisPrimary.getStatus()).isEqualTo("paused");

        // ensure artemisBackup is live
        assertThat(ArtemisJmxHelper.isLive(GCPauseSimulationTests.artemisBackup, 100, 40000)).isTrue();

        // client should be able to produce messages to backup
        lastProducedMessage += GCPauseSimulationTests.produce(totalMessages / 2, lastProducedMessage);
        assertThat(lastProducedMessage).isEqualTo(totalMessages);

        // client should be able to consume messages from backup
        lastConsumedMessage += GCPauseSimulationTests.consume(lastProducedMessage / 6, lastConsumedMessage);
        assertThat(lastConsumedMessage).isEqualTo(lastProducedMessage / 3);

        // unpause the artemisPrimary
        GCPauseSimulationTests.artemisPrimary.unpause();
        assertThat(GCPauseSimulationTests.artemisPrimary.getStatus()).isEqualTo("running");

        // artemisPrimary should shutdown yourself as the journal has changed since it was paused.
        boolean primaryExited = TimeHelper.timeout(e -> "exited".equals(GCPauseSimulationTests.artemisPrimary.getStatus()), 500, 5000);
        assertThat(primaryExited).isTrue();

        // kill artemisBackup
        GCPauseSimulationTests.artemisBackup.kill();

        //update artemisPrimary log message as it should not be live
        GCPauseSimulationTests.artemisPrimary.withLogWait(".*Not a candidate for NodeID.*");

        // restart artemisPrimary
        GCPauseSimulationTests.artemisPrimary.restartWithStop();

        // restart artemisBackup
        GCPauseSimulationTests.artemisBackup.start();

        // ensure artemisPrimary is the live
        assertThat(ArtemisJmxHelper.isLive(GCPauseSimulationTests.artemisPrimary, 100, 40000)).as("Check primary instance is the live").isTrue();

        // ensure artemisBackup is the backup
        assertThat(ArtemisJmxHelper.isBackup(GCPauseSimulationTests.artemisBackup, 100, 5000)).as("Check backup instance is the backup").isTrue();

        // ensure replica is in sync
        assertThat(ArtemisJmxHelper.isReplicaInSync(GCPauseSimulationTests.artemisPrimary, 100, 5000)).as("Check if replica is in sync").isTrue();

        // ensure all remaining produced messages were consumed from primary.
        lastConsumedMessage += GCPauseSimulationTests.consume((lastProducedMessage - lastConsumedMessage), lastConsumedMessage);
        assertThat(lastConsumedMessage).as("Check if consumed all messages").isEqualTo(totalMessages);

    }

}
