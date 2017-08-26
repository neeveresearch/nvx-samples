package com.neeve.samples.dbintegration.receivergateway.gateway;

import com.neeve.aep.annotations.EventHandler;
import com.neeve.cli.annotations.Command;
import com.neeve.server.app.annotations.AppInitializer;
import com.neeve.server.app.annotations.AppFinalizer;
import com.neeve.server.app.annotations.AppStat;
import com.neeve.stats.IStats.Counter;
import com.neeve.stats.StatsFactory;

import com.neeve.samples.dbintegration.receivergateway.app.messages.InventoryEvent;

/**
 * The receiver gateway app 
 *  
 * <p> 
 * This app implements the gateway app portion of sample app that demonstrates 
 * how to copy data from an X app's state repository to an external resource. 
 * In this sample, the X app stores product inventory in its state repository 
 * and dispatches InventoryEvent events as and when it updates inventory. This 
 * gateway app listens for the inventory events and updates the external resource 
 * with data contained in the inventory events. 
 *  
 * The following link discusses integrating external data sources with X app 
 * state repositories and the flow implemented by this sample in more detail 
 *  
 * https://docs.neeveresearch.com/display/KB/Integrating+State+with+External+Sources 
 * </p> 
 */
public class App {
    @AppStat
    final private Counter receivedCount = StatsFactory.createCounterStat("Receive Count");

    final private void connectToExternalResource() {
        /*
         * This is where one would put code to estabish connectivity to 
         * the external resource from where inventory updates are sourced. 
         */
    }

    final private void updateExternalResource(InventoryEvent event) {
        /*
         * This is where one would put code to update the external resource 
         * with data contained in the inventory event 
         */
    }

    final private void disconnectFromExternalResource() {
        /*
         * This is where one would put code to close connectivity to 
         * the external resource from where inventory updates are sourced. 
         */
    }

    @AppInitializer
    final public void init() {
        /*
         * This method is invoked on application startup. This is where 
         * the gateway app initializes itself including connecting to 
         * the external resource 
         */
        connectToExternalResource();
    }

    @EventHandler
    final public void onInventoryEvent(InventoryEvent event) {
        // trace
        System.out.println("Received event " + event.toString()); 

        // increment received count
        receivedCount.increment();

        // update external resource
        updateExternalResource(event);
    }

    @Command
    final public long getReceivedCount() {
        return receivedCount.getCount();
    }

    @AppFinalizer
    final public void fin() {
        /*
         * This method is invoked on application shutdown prior to being 
         * unloaded by the hosting XVM. This is where the gateway app finalizes 
         * itself including disconnecting from the external resource 
         */
        disconnectFromExternalResource();
    }
}
