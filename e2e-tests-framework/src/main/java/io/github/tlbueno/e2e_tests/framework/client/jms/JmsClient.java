package io.github.tlbueno.e2e_tests.framework.client.jms;

import javax.jms.Session;

public interface JmsClient extends AutoCloseable {

    void connect();

    Session getSession();

    void disconnect();

}
