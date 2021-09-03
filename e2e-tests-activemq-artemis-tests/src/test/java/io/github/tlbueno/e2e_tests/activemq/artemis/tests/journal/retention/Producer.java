package io.github.tlbueno.e2e_tests.activemq.artemis.tests.journal.retention;

import io.github.tlbueno.e2e_tests.framework.client.jms.JmsClient;
import io.github.tlbueno.e2e_tests.framework.client.jms.producer.SingleQueueProducer;
import io.github.tlbueno.e2e_tests.framework.client.jms.protocol.amqp.JmsClientAmqpBuilder;
import org.apache.ignite.lang.IgniteRunnable;

import javax.jms.Session;

public class Producer implements IgniteRunnable {

    private String url;
    private String user;
    private String pass;

    public Producer(String url, String user, String pass) {
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    @Override
    public void run() {
        JmsClient client =
                new JmsClientAmqpBuilder(url).
                        withUsername(user).
                        withPassword(pass).
                        withTransactedSession(false).
                        withAckMode(Session.AUTO_ACKNOWLEDGE).
                        build();
        client.connect();
        SingleQueueProducer.produceRandomMsgs(client.getSession(), "retentionTestQueue", 500);
        client.disconnect();
    }
}
