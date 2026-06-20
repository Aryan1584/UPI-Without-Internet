package com.demo.upimesh.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URI;

@Component
public class BrowserLauncher {
    private static final Logger log = LoggerFactory.getLogger(BrowserLauncher.class);

    private final Environment environment;

    public BrowserLauncher(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openDashboard() {
        if (!environment.getProperty("upi.mesh.open-browser", Boolean.class, true) || isTestRun()) {
            return;
        }

        String port = environment.getProperty("server.port", "8080");
        String contextPath = environment.getProperty("server.servlet.context-path", "");
        String url = "http://localhost:" + port + contextPath;

        if (GraphicsEnvironment.isHeadless()) {
            log.info("Dashboard ready at {}", url);
            return;
        }

        try {
            openUrl(url);
            log.info("Opened dashboard at {}", url);
        } catch (Exception e) {
            log.info("Dashboard ready at {} (automatic browser launch failed: {})", url, e.getMessage());
        }
    }

    private void openUrl(String url) throws Exception {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI.create(url));
            return;
        }

        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            start("rundll32", "url.dll,FileProtocolHandler", url);
        } else if (os.contains("mac")) {
            start("open", url);
        } else {
            start("xdg-open", url);
        }
    }

    private void start(String... command) throws IOException {
        new ProcessBuilder(command).start();
    }

    private boolean isTestRun() {
        return System.getProperty("surefire.test.class.path") != null
                || System.getProperty("failsafe.test.class.path") != null
                || "true".equalsIgnoreCase(System.getenv("CI"));
    }
}
