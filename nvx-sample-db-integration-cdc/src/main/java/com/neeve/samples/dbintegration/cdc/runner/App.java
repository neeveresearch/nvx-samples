package com.neeve.samples.dbintegration.cdc.runner;

import java.util.List;
import java.util.Properties;

import com.eaio.uuid.UUID;

import com.neeve.cli.annotations.Command;
import com.neeve.rog.IRogChangeDataCaptureHandler;
import com.neeve.rog.IRogNode;
import com.neeve.rog.log.RogLog;
import com.neeve.rog.log.RogLogCdcProcessor;
import com.neeve.server.app.annotations.AppInitializer;
import com.neeve.server.app.annotations.AppFinalizer;
import com.neeve.server.app.annotations.AppMain;
import com.neeve.server.app.annotations.AppStat;
import com.neeve.stats.IStats.Counter;
import com.neeve.stats.StatsFactory;

import com.neeve.samples.dbintegration.cdc.app.state.Inventory;

/**
 * The runner app 
 *  
 * <p> 
 *  
 * This app implements the CDC runner portion of sample app that demonstrates 
 * how to copy data from an X app's state repository to an external data source 
 * using CDC. This app uses the X Platform transaction log CDC API to "tail" 
 * the processor app's transaction log and updates the external resource with 
 * inventory data contained in the transaction log entries. 
 *  
 * The following link discusses integrating external data sources with X app 
 * state repositories and the flow implemented by this sample in more detail 
 *  
 * https://docs.neeveresearch.com/display/KB/Integrating+State+with+External+Sources 
 * </p> 
 */
public class App {
    final private class CDCHandler implements IRogChangeDataCaptureHandler {
        public CDCHandler() throws Exception {}

        @Override
        final public void onLogStart(int logNumber) {}

        @Override
        final public void onCheckpointStart(final long version) {}

        @Override
        final public boolean handleChange(final UUID id, final ChangeType ct, final List<IRogNode> list) {
            // increment received count
            for (IRogNode node : list) {
                if (node instanceof Inventory) {
                    receivedCount.increment(list.size());
                }
            }

            // get latest version of the object (with one or more changes conflated)
            final IRogNode node = list.get(list.size() - 1);

            // update external resource
            if ( node instanceof Inventory ) {
                if ( ct.equals(ChangeType.Put) )
                    insertExternalResource(node);
                else if ( ct.equals(ChangeType.Update) )
                    updateExternalResource(node);
                else if ( ct.equals(ChangeType.Remove) )
                    removeExternalResource(node);
            }
            
            // done
            return true;
        }

        @Override
        final public boolean onCheckpointComplete(final long version) {
            return true;
        }

        @Override
        final public void onWait() {}

        @Override
        final public void onLogComplete(int logNumber, LogCompletionReason reason, Throwable errorCause) {}
    }

    @AppStat
    final private Counter receivedCount = StatsFactory.createCounterStat("Receive Count");
    private RogLog _log;
    private RogLogCdcProcessor _cdcProcessor;

    final private void connectToExternalResource() {
        /*
         * This is where one would put code to estabish connectivity to 
         * the external resource from where inventory updates are sourced. 
         */
    }

    final private void insertExternalResource(IRogNode object) {
        /*
         * This is where one would add the object into the external 
         * resource
         */
        Inventory inv = (Inventory)object;
        System.out.println("INSERT:" + inv.getSku());
    }

    final private void updateExternalResource(IRogNode object) {
        /*
         * This is where one would put code to update the external resource 
         * with data contained in the state object
         */
        Inventory inv = (Inventory)object;
        System.out.println("UPDATE:" + inv.getSku());
    }

    final private void removeExternalResource(IRogNode object) {
        /*
         * This is where one would put code to remove the object from the
         * external resource
         */
        Inventory inv = (Inventory)object;
        System.out.println("REMOVE:" + inv.getSku());
    }
    
    final private void disconnectFromExternalResource() {
        /*
         * This is where one would put code to close connectivity to 
         * the external resource from where inventory updates are sourced. 
         */
    }

    @AppInitializer
    final public void init() throws Exception {
        /*
         * This method is invoked on application startup. This is where 
         * the gateway app initializes itself including connecting to 
         * the external resource 
         */

        // connect to external resource
        connectToExternalResource();

        // create and open the log CDC processor
        final Properties props = new Properties();
        props.setProperty("cdcEnabled", "true");
        (_log = RogLog.create("processor", props)).open();
        _cdcProcessor = _log.createCdcProcessor(new CDCHandler());
    }

    @AppMain
    final public void main(final String args[]) throws Exception {
        // run CDC on the log
        // ..this call will block and CDC events including log entries will be dispatched to the CDCHandler
        _cdcProcessor.run();
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

        // close the CDC processor
        if (_cdcProcessor != null) {
            _cdcProcessor.close();
        }

        // close the log
        if (_log != null) {
            _log.close();
        }

        // disconnect from external source
        disconnectFromExternalResource();
    }
}
