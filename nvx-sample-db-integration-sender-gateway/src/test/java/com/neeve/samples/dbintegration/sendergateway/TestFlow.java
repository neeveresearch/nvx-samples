package com.neeve.samples.dbintegration.sendergateway;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Test;

/**
 * A test case that tests the application flow.
 */
public class TestFlow extends AbstractTest {
    @Test
    public void testFlow() throws Throwable {
        int sendCount = 1000;
        // configure environment
        Properties env = new Properties();
        env.put("nv.ddl.profiles", "test");
        env.put("app.sendCount", "" + sendCount);

        // start the processor
        com.neeve.samples.dbintegration.sendergateway.app.App app = startApp(com.neeve.samples.dbintegration.sendergateway.app.App.class, "processor", "processor", env);
        Thread.sleep(1000);

        // start the gateway
        com.neeve.samples.dbintegration.sendergateway.gateway.App gateway = startApp(com.neeve.samples.dbintegration.sendergateway.gateway.App.class, "gateway", "gateway", env);
        Thread.sleep(1000);

        // wait till processed
        long timeout = System.currentTimeMillis() + 60000;
        while (app.getNumProcessed() < 1000 && System.currentTimeMillis() < timeout) {
            Thread.sleep(500);
        }

        assertEquals("App did not receive all messages", gateway.getSentCount(), app.getNumProcessed());
    }
}
