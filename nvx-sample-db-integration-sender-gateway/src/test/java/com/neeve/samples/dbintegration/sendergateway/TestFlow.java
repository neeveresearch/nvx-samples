package com.neeve.samples.dbintegration.sendergateway;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.junit.Test;

import com.neeve.server.embedded.EmbeddedXVM;
import com.neeve.util.UtlFile;

/**
 * A test case that tests the application flow.
 */
public class TestFlow {
    @Test
    public void testFlow() throws Throwable {
        // prepare config/testbed
        URL config = new File(System.getProperty("basedir"), "conf/config.xml").toURI().toURL();
        File testBedRoot = new File(System.getProperty("basedir"), "target/testbed/TestFlow");
        UtlFile.deleteDirectory(testBedRoot);

        // configure environment
        Properties env = new Properties();
        env.put("NVROOT", testBedRoot.getCanonicalPath().toString());
        env.put("nv.discovery.descriptor", "local://test&initWaitTime=0");
        env.put("processor.bus.descriptor", "loopback://processor");
        env.put("app.sendCount", "1000");

        // start the app
        EmbeddedXVM appXVM = EmbeddedXVM.create(config, "processor", env);
        appXVM.start();
        Thread.sleep(1000);

        // start the gateway
        EmbeddedXVM gatewayXVM = EmbeddedXVM.create(config, "gateway", env);
        gatewayXVM.start();

        // get apps
        com.neeve.samples.dbintegration.sendergateway.app.App app = (com.neeve.samples.dbintegration.sendergateway.app.App)appXVM.getApplication("processor");
        assertNotNull("Failed to locate processor app", app);
        com.neeve.samples.dbintegration.sendergateway.gateway.App gateway = (com.neeve.samples.dbintegration.sendergateway.gateway.App)gatewayXVM.getApplication("gateway");
        assertNotNull("Failed to locate gateway app", gateway);

        // wait till processed
        long timeout = System.currentTimeMillis() + 60000;
        while (app.getNumProcessed() < 1000 && System.currentTimeMillis() < timeout) {
            Thread.sleep(500);
        }

        assertEquals("App did not receive all messages", gateway.getSentCount(), app.getNumProcessed());
    }
}
