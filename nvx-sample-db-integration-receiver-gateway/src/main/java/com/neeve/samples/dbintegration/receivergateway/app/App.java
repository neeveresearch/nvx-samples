package com.neeve.samples.dbintegration.receivergateway.app;

import com.neeve.sma.MessageView;
import com.neeve.aep.IAepApplicationStateFactory;
import com.neeve.aep.AepEngine;
import com.neeve.aep.AepMessageSender;
import com.neeve.aep.annotations.EventHandler;
import com.neeve.cli.annotations.Command;
import com.neeve.server.app.annotations.AppInjectionPoint;
import com.neeve.server.app.annotations.AppHAPolicy;
import com.neeve.server.app.annotations.AppStat;
import com.neeve.server.app.annotations.AppStateFactoryAccessor;
import com.neeve.stats.StatsFactory;
import com.neeve.stats.IStats.Counter;

import com.neeve.samples.dbintegration.receivergateway.app.messages.InventoryUpdateRequest;
import com.neeve.samples.dbintegration.receivergateway.app.messages.InventoryEvent;
import com.neeve.samples.dbintegration.receivergateway.app.state.Repository;
import com.neeve.samples.dbintegration.receivergateway.app.state.Inventory;

/**
 * The processor app 
 *  
 * <p> 
 * This app implements the processor portion of sample app that demonstrates 
 * how to copy data from an X app's state repository to an external resource 
 * via a gateway app. The gateway app updates an external resource using data 
 * contained in inventory update events sent by this app as and when inventory 
 * updates occur.
 *  
 * The following link discusses integrating external data sources with X app 
 * state repositories and he flow implemented by this sample in more detail 
 *  
 * https://docs.neeveresearch.com/display/KB/Integrating+State+with+External+Sources 
 * </p> 
 */
@AppHAPolicy(value=AepEngine.HAPolicy.StateReplication)
public class App {
    private AepMessageSender _messageSender;
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

    @AppInjectionPoint
    final public void setMessageSender(AepMessageSender messageSender) {
        _messageSender = messageSender;
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

        // send inventory event
        InventoryEvent event = InventoryEvent.create();
        event.setSku(inventory.getSku());
        event.setCount(inventory.getCount());
        _messageSender.sendMessage("events", event);
    }

    @Command
    public long getNumProcessed() {
        return processedCount.getCount();
    }
}

