package io.github.tlbueno.e2e_tests.framework.common.helper;

import io.github.tlbueno.e2e_tests.framework.endpoints.artemis.ArtemisEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.config.ActiveMQDefaultConfiguration;
import org.apache.activemq.artemis.api.core.management.ActiveMQServerControl;
import org.apache.activemq.artemis.api.core.management.ObjectNameBuilder;

import javax.management.MBeanServerInvocationHandler;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.net.MalformedURLException;

@Slf4j
public final class ArtemisJmxHelper {

    private static final String JMX_URL_BASE = "service:jmx:rmi:///jndi/rmi://";
    private static final String JMX_URL_SUFFIX = "/jmxrmi";

    private ArtemisJmxHelper() {
        super();
    }

    public static boolean isLive(ArtemisEndpoint artemisEndpoint, long delayInMs, long timeoutInMs) {
        ActiveMQServerControl controller = getActiveMQServerController(artemisEndpoint);
        return TimeHelper.timeout(e -> controller.isActive(), delayInMs, timeoutInMs);
    }

    public static boolean isBackup(ArtemisEndpoint artemisEndpoint, long delayInMs, long timeoutInMs) {
        ActiveMQServerControl controller = getActiveMQServerController(artemisEndpoint);
        return TimeHelper.timeout(e -> controller.isBackup(), delayInMs, timeoutInMs);
    }

    public static boolean isReplicaInSync(ArtemisEndpoint artemisEndpoint, long delayInMs, long timeoutInMs) {
        ActiveMQServerControl controller = getActiveMQServerController(artemisEndpoint);
        return TimeHelper.timeout(e -> controller.isReplicaSync(), delayInMs, timeoutInMs);
    }

    private static ActiveMQServerControl getActiveMQServerController(ArtemisEndpoint artemisEndpoint) {
        JMXServiceURL jmxUrl = getJmxUrl(artemisEndpoint);
        ObjectNameBuilder builder = getObjectBuilder(artemisEndpoint);
        ActiveMQServerControl controller;
        try {
            JMXConnector jmx = JMXConnectorFactory.connect(jmxUrl);
            controller = MBeanServerInvocationHandler.newProxyInstance(jmx.getMBeanServerConnection(),
                    builder.getActiveMQServerObjectName(), ActiveMQServerControl.class, false);
        } catch (Exception e) {
            String errMsg = "error on getting ActiveMQServerControl: " + e.getMessage();
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
        return controller;
    }

    private static JMXServiceURL getJmxUrl(ArtemisEndpoint artemisEndpoint) {
        JMXServiceURL url;
        try {
            String hostAndPort = artemisEndpoint.getHostAndPort(ArtemisEndpoint.DEFAULT_JMX_PORT);
            url = new JMXServiceURL(JMX_URL_BASE + hostAndPort + JMX_URL_SUFFIX);
        } catch (MalformedURLException e) {
            String errMsg = "Error on getting JMX url: " + e.getMessage();
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
        return url;
    }

    private static ObjectNameBuilder getObjectBuilder(ArtemisEndpoint artemisEndpoint) {
        return ObjectNameBuilder.create(ActiveMQDefaultConfiguration.getDefaultJmxDomain(), artemisEndpoint.getName(),
                true);
    }

}
