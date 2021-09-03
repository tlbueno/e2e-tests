package io.github.tlbueno.e2e_tests.framework.endpoints.ignite;

import io.github.tlbueno.e2e_tests.framework.endpoints.generic.GenericEndpoint;

import java.util.List;

public interface IgniteEndpoint extends GenericEndpoint {

    List<Integer> DEFAULT_PORTS = List.of(10800, 11211, 47100, 47500);

    String CONF_CONFIG_FILE = "IgniteEndpoint.ConfigFile";

}
