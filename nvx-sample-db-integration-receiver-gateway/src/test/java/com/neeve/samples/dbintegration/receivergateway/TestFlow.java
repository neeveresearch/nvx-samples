package com.neeve.samples.dbintegration.receivergateway;

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

        // start the gateway
        com.neeve.samples.dbintegration.receivergateway.gateway.App gateway = startApp(com.neeve.samples.dbintegration.receivergateway.gateway.App.class, "gateway", "gateway", env);
        Thread.sleep(1000);

        // start the app
        startApp(com.neeve.samples.dbintegration.receivergateway.app.App.class, "processor", "processor", env);
        Thread.sleep(1000);

        // start the driver
        com.neeve.samples.dbintegration.receivergateway.driver.App driver = startApp(com.neeve.samples.dbintegration.receivergateway.driver.App.class, "driver", "driver", env);
        Thread.sleep(1000);

        // wait till received
        long timeout = System.currentTimeMillis() + 60000;
        while (gateway.getReceivedCount() < sendCount && System.currentTimeMillis() < timeout) {
            Thread.sleep(500);
        }
        assertEquals("App did not receive all messages", driver.getSentCount(), gateway.getReceivedCount());
    }
}
