/**
 * Copyright (c) 2016 Neeve Research, LLC. All Rights Reserved.
 * Confidential and proprietary information of Neeve Research, LLC.
 * Copyright Version 1.0
 */
package com.neeve.talon.starter;

import com.neeve.server.embedded.EmbeddedXVM;
import com.neeve.talon.starter.driver.ReceiveDriver;
import com.neeve.talon.starter.driver.SendDriver;
import com.neeve.util.UtlFile;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.*;

final public class TestFlow {
    @Test
    public void testFlow() throws Throwable {
        URL config = new File(System.getProperty("basedir"), "conf/config.xml").toURI().toURL();
        File testBedRoot = new File(System.getProperty("basedir"), "target/testbed/TestFlow");
        UtlFile.deleteDirectory(testBedRoot);

        Properties env = new Properties();
        env.put("NVROOT", testBedRoot.getCanonicalPath().toString());
        env.put("nv.optimizefor", "throughput");
        env.put("nv.conservecpu", "true");
        env.put("nv.server.stats.enable", "false");
        env.put("nv.server.stats.interval", "5000");
        env.put("nv.server.stats.userstats.trace", "debug");
        env.put("driver.sendRate", "10000");
        env.put("driver.autoSend", "true");
        env.put("processor.tlog.root", new File(testBedRoot, "rdat").getCanonicalPath().toString());

        // use local discovery rather than multicast for portability
        env.put("nv.discovery.descriptor", "local://test&initWaitTime=0");

        // disable clustering for faster startup
        env.put("processor.clustering.enabled", "false");

        //Start the receiver
        EmbeddedXVM receiverXVM = EmbeddedXVM.create(config, "receiver", env);
        receiverXVM.start();
        ReceiveDriver receiver = (ReceiveDriver)receiverXVM.getApplication("receiver");

        Thread.sleep(1000);

        //Start the processor
        EmbeddedXVM processor1XVM = EmbeddedXVM.create(config, "processor-1", env);
        processor1XVM.start();

        Thread.sleep(1000);

        //Start the sender
        EmbeddedXVM senderXVM = EmbeddedXVM.create(config, "sender", env);
        senderXVM.start();
        SendDriver sender = (SendDriver)senderXVM.getApplication("sender");

        long timeout = System.currentTimeMillis() + 60000;
        while (receiver.getCount() < 10000 && System.currentTimeMillis() < timeout) {
            Thread.sleep(500);
        }

        assertEquals("Receiver did not receive all Events", sender.getCount(), receiver.getCount());
    }
}
