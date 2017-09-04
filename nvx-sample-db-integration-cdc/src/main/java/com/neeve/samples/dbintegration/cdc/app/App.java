package com.neeve.samples.dbintegration.cdc.app;

import com.neeve.sma.MessageView;
import com.neeve.aep.IAepApplicationStateFactory;
import com.neeve.aep.AepEngine;
import com.neeve.aep.annotations.EventHandler;
import com.neeve.cli.annotations.Command;
import com.neeve.server.app.annotations.AppHAPolicy;
import com.neeve.server.app.annotations.AppStat;
import com.neeve.server.app.annotations.AppStateFactoryAccessor;
import com.neeve.stats.StatsFactory;
import com.neeve.stats.IStats.Counter;

import com.neeve.samples.dbintegration.cdc.app.messages.InventoryUpdateRequest;
import com.neeve.samples.dbintegration.cdc.app.state.Repository;
import com.neeve.samples.dbintegration.cdc.app.state.Inventory;

/**
 * The processor app 
 *  
 * <p> 
 * This app implements the processor portion of sample app that demonstrates 
 * how to copy data from an external resource to an X app's state repository 
 * using CDC. The CDC runner app "tails" this app's transaction log using 
 * CDC and updates an external resource using data contained in the state 
 * change events it received from the CDC process.
 *  
 * The following link discusses integrating external data sources with X app 
 * state repositories and he flow implemented by this sample in more detail 
 *  
 * https://docs.neeveresearch.com/display/KB/Integrating+State+with+External+Sources 
 * </p> 
 */
@AppHAPolicy(value = AepEngine.HAPolicy.StateReplication)
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
