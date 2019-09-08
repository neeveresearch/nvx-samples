/**
 * Copyright (c) 2016 Neeve Research, LLC. All Rights Reserved.
 * Confidential and proprietary information of Neeve Research, LLC.
 * Copyright Version 1.0
 */
package com.neeve.talon.starter;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Test;

import com.neeve.talon.starter.driver.ReceiveDriver;
import com.neeve.talon.starter.driver.SendDriver;

final public class TestFlow extends AbstractTest {
    @Test
    public void testFlow() throws Throwable {
        int sendCount = 10000;
        int sendRate = 1000;
        // configure
        Properties env = new Properties();
        env.put("nv.ddl.profiles", "test");
        env.put("nv.optimizefor", "throughput");
        // disable clustering to speed up app startup:
        env.put("x.apps.processor.storage.clustering.enabled", "false");
        env.put("driver.sendCount", String.valueOf(sendCount));
        env.put("driver.sendRate", String.valueOf(sendRate));

        //Start the receiver
        ReceiveDriver receiver = startApp(ReceiveDriver.class, "receiver", "receiver", env);

        Thread.sleep(1000);

        //Start the processor
        startApp(Application.class, "processor", "processor-1", env);

        Thread.sleep(1000);

        //Start the sender
        SendDriver sender = startApp(SendDriver.class, "sender", "sender", env);

        long timeout = System.currentTimeMillis() + 60000;
        while (receiver.getCount() < sendCount && System.currentTimeMillis() < timeout) {
            Thread.sleep(500);
        }

        //Sleep to catch any duplicates
        Thread.sleep(1000);

        assertEquals("Receiver did not receive all Events", sender.getCount(), receiver.getCount());
    }
}
