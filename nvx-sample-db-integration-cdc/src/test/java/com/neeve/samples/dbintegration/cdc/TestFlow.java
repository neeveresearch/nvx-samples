package com.neeve.samples.dbintegration.cdc;

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

        // start the CDC runner
        EmbeddedXVM runnerXVM = EmbeddedXVM.create(config, "runner", env);
        runnerXVM.start();
        Thread.sleep(1000);

        // start the driver
        EmbeddedXVM driverXVM = EmbeddedXVM.create(config, "driver", env);
        driverXVM.start();

        // get apps
        com.neeve.samples.dbintegration.cdc.app.App app = (com.neeve.samples.dbintegration.cdc.app.App)appXVM.getApplication("processor");
        assertNotNull("Failed to locate processor app", app);
        com.neeve.samples.dbintegration.cdc.runner.App runner = (com.neeve.samples.dbintegration.cdc.runner.App)runnerXVM.getApplication("runner");
        assertNotNull("Failed to locate runner app", runner);
        com.neeve.samples.dbintegration.cdc.driver.App driver = (com.neeve.samples.dbintegration.cdc.driver.App)driverXVM.getApplication("driver");
        assertNotNull("Failed to locate driver app", driver);

        // wait till received
        long timeout = System.currentTimeMillis() + 60000;
        while (runner.getReceivedCount() < 1000 && System.currentTimeMillis() < timeout) {
            Thread.sleep(500);
        }
        assertEquals("App did not receive all messages", driver.getSentCount(), runner.getReceivedCount());
    }
}
