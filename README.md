# The Samples Repository

This repository contains the X Platform sample programs

## Building the samples
1. Ensure Maven is installed
2. Clone this repository
3. Run mvn install

## The Samples
The following lists and describes the samples in this repository

### nvx-app-talon-starter
The starter app used in the [Talon Manual's](https://docs.neeveresearch.com/display/TALONDOC) [Getting Started](https://docs.neeveresearch.com/display/TALONDOC/Get+Started) section. This simple application consists of a simple processor micro app, that is forwards messages on from a sender micro-app to a receiver micro-app. 

### nvx-sample-db-integration-sender-gateway
This sample illustrates how to copy data from an external data source, such as an RDBMS, to an X app's state repository using a gateway app. See [Integrating State with External Sources](https://docs.neeveresearch.com/display/KB/Integrating+State+with+External+Sources#IntegratingStatewithExternalSources-IntegratingExternalSourcetoApp) for more information.

### nvx-sample-db-integration-receiver-gateway
This sample illustrates how to copy data from an X app's state repository to an external data source, such as an RDBMS, using a gateway app. See [Integrating State with External Sources](https://docs.neeveresearch.com/display/KB/Integrating+State+with+External+Sources#IntegratingStatewithExternalSources-IntegratingApptoExternalSource) for more information.

### nvx-sample-db-integration-cdc
This sample illustrates how to copy data from an X app's state repository to an external data source, such as an RDBMS, using CDC (Change Data Capture). 

See [Integrating State with External Sources](https://docs.neeveresearch.com/display/KB/Integrating+State+with+External+Sources#IntegratingStatewithExternalSources-UseCDC) for more information.
