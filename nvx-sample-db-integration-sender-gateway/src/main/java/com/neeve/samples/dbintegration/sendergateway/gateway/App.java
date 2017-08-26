package com.neeve.samples.dbintegration.sendergateway.gateway;

import java.util.Random;

import com.neeve.aep.AepMessageSender;
import com.neeve.cli.annotations.Command;
import com.neeve.cli.annotations.Configured;
import com.neeve.server.app.annotations.AppInjectionPoint;
import com.neeve.server.app.annotations.AppMain;
import com.neeve.server.app.annotations.AppStat;
import com.neeve.stats.IStats.Counter;
import com.neeve.stats.StatsFactory;

import com.neeve.samples.dbintegration.sendergateway.app.messages.InventoryUpdateRequest;

/**
 * The sender gateway app 
 *  
 * <p> 
 * This app implements the gateway app portion of sample app that demonstrates 
 * how to copy data from an external resource to an X app's state repository. 
 * In this sample, the X app stores product inventory in its state repository 
 * and this gateway app sources inventory updates from an external resource 
 * and sends the updates to the X app via messaging. 
 *  
 * The following link discusses integrating external data sources with X app 
 * state repositories and the flow implemented by this sample in more detail 
 *  
 * https://docs.neeveresearch.com/display/KB/Integrating+State+with+External+Sources 
 * </p> 
 */
public class App {
    private Random _random = new Random(System.currentTimeMillis());
    private AepMessageSender _messageSender;

    @Configured(property = "app.sendCount", defaultValue="0")
    private int sendCount;

    @AppStat
    final private Counter sentCounter = StatsFactory.createCounterStat("Sent Message Count");

    final private void connectToExternalResource() {
        /*
         * This is where one would put code to estabish connectivity to 
         * the external resource from where inventory updates are sourced. 
         */
    }

    final private void disconnectFromExternalResource() {
        /*
         * This is where one would put code to close connectivity to 
         * the external resource from where inventory updates are sourced. 
         */
    }

    final private InventoryUpdateRequest getNextInventoryUpdate() {
        /*
         * This is where one would fetch inventory updates from 
         * the external resource. In this sample, we just simulate 
         * this by creating a random InventoryUpdateRequest. This 
         * should be replaced by some mechanism by which this 
         * thread is blocked waiting for inventory updates to 
         * be received and then convert the inventory updates 
         * to InventoryUpdateRequest messages and return
         */
        InventoryUpdateRequest request = InventoryUpdateRequest.create();
        request.setSku(_random.nextInt(10));
        request.setCount(_random.nextInt(100000));
        return request;
    }

    @AppInjectionPoint
    final public void setMessageSender(AepMessageSender messageSender) {
        _messageSender = messageSender;
    }

    /**
     * The gateway app's main entry point. The X runtime will spin up 
     * a thread and invoke this method (identified by AppMain) upon 
     * successful startup of the gateway app 
     */
    @AppMain
    final public void main(final String args[]) {
        connectToExternalResource();
        try {
            for (int i = 0; (sendCount == 0 ? true : i < sendCount); i++) {
                InventoryUpdateRequest request = getNextInventoryUpdate();
                _messageSender.sendMessage("requests", request);
                sentCounter.increment();
            }
        }
        finally { 
            disconnectFromExternalResource();
        }
    }

    @Command
    final public long getSentCount() {
        return sentCounter.getCount();
    }
}
