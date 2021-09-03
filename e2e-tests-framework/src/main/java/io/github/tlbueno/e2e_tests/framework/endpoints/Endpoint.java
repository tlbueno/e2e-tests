package io.github.tlbueno.e2e_tests.framework.endpoints;

import io.github.tlbueno.e2e_tests.framework.common.configuration.Configuration;
import io.github.tlbueno.e2e_tests.framework.endpoints.generic.GenericEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.Network;

import java.util.Objects;

@Slf4j
public final class Endpoint {

    private static final String CONF_NETWORK_PROVIDER = "Network.Provider";
    private static final String DEFAULT_NETWORK_PROVIDER = "TestContainers";
    private static final String DEFAULT_SERVICE_PROVIDER_SUFFIX = "TestContainers";
    private static final String SERVICE_PROVIDER_SUFFIX = ".service.provider";

    private Endpoint() {
        super();
    }

    public static <T extends GenericEndpoint> T getEndpoint(Class<T> clazz) {
        T service;
        String providerName = Configuration.getProperty(clazz.getSimpleName() + SERVICE_PROVIDER_SUFFIX);
        String className;
        className = Objects.requireNonNullElseGet(providerName, () -> clazz.getName() +
                DEFAULT_SERVICE_PROVIDER_SUFFIX);
        try {
            service = (T) Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | ClassCastException e) {
            String errMsg = "Error loading service using class name " + className + ": " + e.getMessage();
            log.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }
        return service;
    }

    public static Object getNetwork() {
        Object network;
        String providerName = Configuration.getProperty(CONF_NETWORK_PROVIDER);
        if (DEFAULT_NETWORK_PROVIDER.equals(providerName)) {
            network = Network.newNetwork();
        } else {
            try {
                network = Class.forName(providerName).getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException | ClassCastException e) {
                String errMsg = "Error loading network provider using class name " + providerName + ": " +
                        e.getMessage();
                log.error(errMsg, e);
                throw new RuntimeException(errMsg, e);
            }
        }
        return network;
    }

}
