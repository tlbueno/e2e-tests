package io.github.tlbueno.e2e_tests.framework.client.jms.producer;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public final class SingleQueueProducer {

    private SingleQueueProducer() {
        super();
    }

    public static Map<String, String> produceRandomMsgs(Session session, String queueName, int numOfMessages) {
        Map<String, String> producedMsgs = new HashMap<>();
        Faker faker = new Faker();
        try {
            Queue producerQueue = session.createQueue(queueName);
            MessageProducer producer = session.createProducer(producerQueue);
            for (int i = 0; i < numOfMessages; i++) {
                TextMessage message = session.createTextMessage(faker.lorem().paragraph());
                producer.send(message);
                log.debug("produced " + message.getText());
                producedMsgs.put(message.getJMSMessageID(), message.getText());
            }
        } catch (JMSException e) {
            String errMsg = "error on producing message: " + e.getMessage();
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
        return producedMsgs;
    }

}
