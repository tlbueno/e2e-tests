package io.github.tlbueno.e2e_tests.framework.common.helper;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Capabilities;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.Network;

@Slf4j
public final class WebDriverContainer {

    private WebDriverContainer() {
        super();
    }

    public static BrowserWebDriverContainer<?> getBrowser(Capabilities capabilities, Object network) {
        BrowserWebDriverContainer<?> browser = new BrowserWebDriverContainer<>();
        browser.withCapabilities(capabilities);
        if (network != null) {
            browser.withNetwork((Network) network);
        }
        browser.start();
        return browser;
    }

}
