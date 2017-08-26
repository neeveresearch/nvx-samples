package com.neeve.samples.dbintegration.cdc.driver;

import java.util.Random;

import com.neeve.aep.AepMessageSender;
import com.neeve.cli.annotations.Command;
import com.neeve.cli.annotations.Configured;
import com.neeve.server.app.annotations.AppInjectionPoint;
import com.neeve.server.app.annotations.AppMain;
import com.neeve.server.app.annotations.AppStat;
import com.neeve.stats.IStats.Counter;
import com.neeve.stats.StatsFactory;

import com.neeve.samples.dbintegration.cdc.app.messages.InventoryUpdateRequest;

/**
 * The driver app 
 *  
 * <p> 
 * This app implements the sender driver portion of sample app that demonstrates 
 * how to copy data from an X app's state repository to an external data source 
 * using CDC. This app sends inventory update requests to the processor app. The 
 * processor app updates its inventory. The CDC runner app "tails" the processor 
 * app's transaction log and updates the external resource with inventory data 
 * contained in the transaction log entries. 
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

    @AppInjectionPoint
    final public void setMessageSender(AepMessageSender messageSender) {
        _messageSender = messageSender;
    }

    @AppMain
    final public void main(final String args[]) {
        for (int i = 0 ; (sendCount == 0 ? true : i < sendCount); i++) {
            InventoryUpdateRequest request = InventoryUpdateRequest.create();
            request.setSku(_random.nextInt(10));
            request.setCount(_random.nextInt(100000));
            _messageSender.sendMessage("requests", request);
            sentCounter.increment();
        }
    }

    @Command
    final public long getSentCount() {
        return sentCounter.getCount();
    }
}
