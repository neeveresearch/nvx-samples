package com.neeve.talon.starter;

import com.neeve.sma.MessageView;
import com.neeve.aep.IAepApplicationStateFactory;
import com.neeve.aep.AepEngine;
import com.neeve.aep.AepMessageSender;
import com.neeve.aep.annotations.EventHandler;
import com.neeve.server.app.annotations.AppInjectionPoint;
import com.neeve.server.app.annotations.AppStat;
import com.neeve.server.app.annotations.AppHAPolicy;
import com.neeve.server.app.annotations.AppStateFactoryAccessor;

import com.neeve.talon.starter.messages.Message;
import com.neeve.talon.starter.messages.Event;
import com.neeve.talon.starter.state.Repository;

@AppHAPolicy(value = AepEngine.HAPolicy.StateReplication)
public class Application {
    private AepMessageSender _messageSender;

    @AppStat(name = "Processed")
    private volatile long numProcessed;

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
    final public void onMessage(Message message, Repository repository) {
        // update state
        repository.setCounter(repository.getCounter() + 1);
        numProcessed = repository.getCounter();

        // trace
        if (repository.getCounter() % 1000 == 0) {
            System.out.println("Processed " + repository.getCounter() + " messages");
        }

        // send event
        Event event = Event.create();
        event.setComment("Hi There");
        event.setCount(repository.getCounter());
        _messageSender.sendMessage("events", event);
    }
}
