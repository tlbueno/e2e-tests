package io.github.tlbueno.e2e_tests.framework.client.jms.protocol.amqp;

import io.github.tlbueno.e2e_tests.framework.client.jms.AbstractJmsClient;
import lombok.extern.slf4j.Slf4j;

import javax.jms.ConnectionFactory;

@Slf4j
public final class JmsClientAmqp extends AbstractJmsClient {

    JmsClientAmqp() {
    }

    void setUrl(String url) {
        this.url = url;
    }

    void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    void setUsername(String username) {
        this.username = username;
    }

    void setPassword(String password) {
        this.password = password;
    }

    void setTransacted(boolean transacted) {
        this.transacted = transacted;
    }

    void setAckMode(int ackMode) {
        this.ackMode = ackMode;
    }

}
