package io.github.tlbueno.e2e_tests.framework.client.jms;

import lombok.extern.slf4j.Slf4j;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

@Slf4j
public abstract class AbstractJmsClient implements JmsClient {

    protected String url;
    protected String username;
    protected String password;
    protected ConnectionFactory connectionFactory;
    protected int ackMode;
    protected boolean transacted;
    private Connection connection;
    private Session session;

    @Override
    public void connect() {
        try {
            connection = connectionFactory.createConnection(username, password);
            session = connection.createSession(transacted, ackMode);
        } catch (JMSException e) {
            String errMsg = "Error opening connection to " + url + ": " + e.getMessage();
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public void disconnect() {
        close();
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                String errMsg = "Error closing connection to " + url + ": " + e.getMessage();
                log.error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            }
        }
    }

}
