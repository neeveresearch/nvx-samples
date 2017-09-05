package com.neeve.talon.starter.driver;

import com.neeve.talon.starter.messages.Event;

import com.neeve.aep.AepEngine.HAPolicy;
import com.neeve.aep.annotations.EventHandler;
import com.neeve.cli.annotations.Command;
import com.neeve.server.app.annotations.AppHAPolicy;
import com.neeve.server.app.annotations.AppStat;
import com.neeve.stats.StatsFactory;
import com.neeve.stats.IStats.Counter;
import com.neeve.stats.IStats.Latencies;
import com.neeve.util.UtlTime;

/**
 * A test driver app for the Application
 */
@AppHAPolicy(HAPolicy.StateReplication)
public class ReceiveDriver {
    @AppStat
    private final Counter receivedCount = StatsFactory.createCounterStat("ReceiveDriver Count");

    @AppStat(name = "ReceiveDriver Event Latency")
    private volatile Latencies receiveLatencies;

    @EventHandler
    public final void onEvent(Event event) {
        receivedCount.increment();
        if (event.getOriginTs() > 0) {
            receiveLatencies.add(UtlTime.now() - event.getOriginTs());
        }
        if (receivedCount.getCount() % 1000 == 0) {
            System.out.println("Received " + receivedCount.getCount() + " messages");
        }
    }

    @Command
    public long getCount() {
        return receivedCount.getCount();
    }
}
