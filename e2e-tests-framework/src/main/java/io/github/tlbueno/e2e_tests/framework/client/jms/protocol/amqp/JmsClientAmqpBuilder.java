package io.github.tlbueno.e2e_tests.framework.client.jms.protocol.amqp;

import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.jms.JmsConnectionFactory;

@Slf4j
public final class JmsClientAmqpBuilder {

    private final JmsClientAmqp jmsAmqpClient = new JmsClientAmqp();

    public JmsClientAmqpBuilder(String url) {
        jmsAmqpClient.setUrl(url);
        jmsAmqpClient.setConnectionFactory(new JmsConnectionFactory(url));
    }

    public JmsClientAmqpBuilder withUsername(String username) {
        jmsAmqpClient.setUsername(username);
        return this;
    }

    public JmsClientAmqpBuilder withPassword(String password) {
        jmsAmqpClient.setPassword(password);
        return this;
    }

    public JmsClientAmqpBuilder withTransactedSession(boolean transacted) {
        jmsAmqpClient.setTransacted(transacted);
        return this;
    }

    public JmsClientAmqpBuilder withAckMode(int ackMode) {
        jmsAmqpClient.setAckMode(ackMode);
        return this;
    }

    public JmsClientAmqp build() {
        return jmsAmqpClient;
    }

}
