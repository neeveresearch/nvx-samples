package com.neeve.talon.starter.driver;

import com.neeve.talon.starter.messages.IMessage;
import com.neeve.talon.starter.messages.Message;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.neeve.aep.AepEngine.HAPolicy;
import com.neeve.aep.AepMessageSender;
import com.neeve.cli.annotations.Command;
import com.neeve.cli.annotations.Configured;
import com.neeve.server.app.annotations.AppHAPolicy;
import com.neeve.server.app.annotations.AppInjectionPoint;
import com.neeve.server.app.annotations.AppMain;
import com.neeve.server.app.annotations.AppStat;
import com.neeve.stats.IStats.Counter;
import com.neeve.stats.StatsFactory;
import com.neeve.util.UtlGovernor;

/**
 * A test driver app for the Application.
 */
@AppHAPolicy(HAPolicy.StateReplication)
public class SendDriver {
    @Configured(property = "driver.sendCount")
    private int sendCount;
    @Configured(property = "driver.sendRate")
    private int sendRate;
    @Configured(property = "driver.sendChannel")
    private String sendChannel;
    @Configured(property = "driver.autoSend")
    private boolean autoSend;

    @AppStat
    private final Counter sentCount = StatsFactory.createCounterStat("SendDriver Count");

    private volatile AepMessageSender messageSender;

    @AppInjectionPoint
    final public void setMessageSender(AepMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @Command(name = "send")
    public final void doSend(int count, int rate) throws Exception {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            if (!autoSend) {
                System.out.println("Press Enter to send " + sendCount + " messages...");
                inputReader.readLine();
            }
            UtlGovernor.run(sendCount, sendRate, new Runnable() {
                @Override
                public void run() {
                    IMessage message = Message.create();
                    message.setCount(sentCount.getCount() + 1);
                    sentCount.increment();
                    messageSender.sendMessage(sendChannel, message);
                    if (sentCount.getCount() % 1000 == 0) {
                        System.out.println("Sent " + sentCount.getCount() + " messages");
                    }
                }
            });
            if (autoSend) {
                break;
            }
        }
    }

    /**
     * Gets the number of messages sent by the sender. 
     * 
     * @return The number of messages sent by this sender.
     */
    @Command
    public long getCount() {
        return sentCount.getCount();
    }

    @AppMain
    public void run(String[] args) throws Exception {
        doSend(sendCount, sendRate);
    }
}
