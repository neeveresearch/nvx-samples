package com.neeve.talon.starter.driver;

import com.neeve.talon.starter.messages.IMessage;
import com.neeve.talon.starter.messages.Message;
import com.neeve.trace.Tracer;
import com.neeve.aep.AepEngine.HAPolicy;

import java.util.concurrent.atomic.AtomicReference;

import com.neeve.aep.AepMessageSender;
import com.neeve.cli.annotations.Argument;
import com.neeve.cli.annotations.Command;
import com.neeve.cli.annotations.Configured;
import com.neeve.server.app.annotations.AppHAPolicy;
import com.neeve.server.app.annotations.AppInjectionPoint;
import com.neeve.server.app.annotations.AppMain;
import com.neeve.server.app.annotations.AppStat;
import com.neeve.stats.IStats.Counter;
import com.neeve.stats.StatsFactory;
import com.neeve.util.UtlGovernor;
import com.neeve.util.UtlThrowable;

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
    private final AtomicReference<Thread> sendingThread = new AtomicReference<Thread>();

    private Tracer tracer = Tracer.create("sender", Tracer.Level.INFO);

    @AppInjectionPoint
    final public void setMessageSender(AepMessageSender messageSender) {
        this.messageSender = messageSender;
    }

    /**
     * Starts sending messages (in a background thread)
     */
    @Command(name = "send", displayName = "Send Messages", description = "Instructs the driver to send messages")
    public final void sendMessages(@Argument(name = "count", position = 1, required = true, description = "The number of messages to send") final int count,
                                   @Argument(name = "rate", position = 2, required = true, description = "The rate at which to send") final int rate,
                                   @Argument(name = "async", position = 3, displayName = "Async", defaultValue = "true", description = "True to send messages in a background thread") final boolean async) throws Exception {

        if (async) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sendMessages(count, rate, false);
                    }
                    catch (Exception e) {
                        System.err.println("Background send failed: " + UtlThrowable.prepareStackTrace(e));
                    }
                }
            }, "Sender Thread").start();
            return;
        }

        tracer.log("Sending " + count + " messages at " + rate + "/sec", Tracer.Level.INFO);

        // Stop existing sender if any:
        stopSending();
        sendingThread.set(Thread.currentThread());
        try {
            // Send at specified rate:
            UtlGovernor sendGoverner = new UtlGovernor(rate);
            int sent = 0;
            while (sent++ < count && sendingThread.get() == Thread.currentThread() && !Thread.interrupted()) {
                IMessage message = Message.create();
                message.setCount(sentCount.getCount() + 1);
                messageSender.sendMessage(sendChannel, message);
                sentCount.increment();
                if (sentCount.getCount() % 1000 == 0) {
                    System.out.println("Sent " + sentCount.getCount() + " messages");
                }
                sendGoverner.blockToNext();
            }
        }
        finally {
            sendingThread.compareAndSet(Thread.currentThread(), null);
        }
    }

    /**
     * Stops the current sending thread (if active_ 
     */
    @Command(name = "stopSending", displayName = "Stop Sending", description = "Stops sending of messages.")
    final public void stopSending() throws Exception {
        Thread oldThread = sendingThread.getAndSet(null);
        if (oldThread != null) {
            tracer.log("Stopping currently running sender thread...", Tracer.Level.INFO);
            oldThread.interrupt();
            oldThread.join();
        }
    }

    /**
     * Gets the number of messages sent by the sender. 
     * 
     * @return The number of messages sent by this sender.
     */
    @Command(name = "getCount", displayName = "Get Sent Count", description = "Returns the count of messages sent by this app.")
    public long getCount() {
        return sentCount.getCount();
    }

    @AppMain
    public void run(String[] args) throws Exception {
        if (!autoSend) {
            while (true) {
                System.out.println("Press Enter to send " + sendCount + " messages...");
                System.in.read();
                sendMessages(sendCount, sendRate, false);
            }
        }
        else {
            sendMessages(sendCount, sendRate, false);
        }
    }
}
