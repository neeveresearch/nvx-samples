package com.neeve.samples.dbintegration.sendergateway.app;

import com.neeve.sma.MessageView;
import com.neeve.aep.IAepApplicationStateFactory;
import com.neeve.aep.AepEngine;
import com.neeve.aep.annotations.EventHandler;
import com.neeve.cli.annotations.Command;
import com.neeve.server.app.annotations.AppInjectionPoint;
import com.neeve.server.app.annotations.AppHAPolicy;
import com.neeve.server.app.annotations.AppStat;
import com.neeve.server.app.annotations.AppStateFactoryAccessor;
import com.neeve.stats.StatsFactory;
import com.neeve.stats.IStats.Counter;

import com.neeve.samples.dbintegration.sendergateway.app.messages.InventoryUpdateRequest;
import com.neeve.samples.dbintegration.sendergateway.app.state.Repository;
import com.neeve.samples.dbintegration.sendergateway.app.state.Inventory;

/**
 * The processor app 
 *  
 * <p> 
 * This app implements the processor portion of sample app that demonstrates 
 * how to copy data from an external resource to an X app's state repository 
 * via a gateway app. The gateway app sources inventory updates from an external 
 * resource and sends the updates to this app via messaging. 
 *  
 * The following link discusses integrating external data sources with X app 
 * state repositories and he flow implemented by this sample in more detail 
 *  
 * https://docs.neeveresearch.com/display/KB/Integrating+State+with+External+Sources 
 * </p> 
 */
@AppHAPolicy(value=AepEngine.HAPolicy.StateReplication)
public class App {
    @AppStat
    final private Counter processedCount = StatsFactory.createCounterStat("Processed Count");

    @AppStateFactoryAccessor
    final public IAepApplicationStateFactory getStateFactory() {
        return new IAepApplicationStateFactory() {
            @Override
            final public Repository createState(MessageView view) {
                return Repository.create();
            }
        };
    }

    @EventHandler
    final public void onInventoryUpdateRequest(InventoryUpdateRequest request, Repository repository) {
        // trace
        System.out.println("Received request " + request.toString()); 

        // update the number of requests processed
        processedCount.increment();

        // update inventory state
        Inventory inventory = repository.getInventories().get(request.getSku());
        if (inventory == null) {
            inventory = Inventory.create();
            inventory.setSku(request.getSku());
        }
        inventory.setCount(request.getCount());
        repository.getInventories().put(request.getSku(), inventory);
    }

    @Command
    public long getNumProcessed() {
        return processedCount.getCount();
    }
}
