[![Build Status](https://travis-ci.org/SolaceSamples/solace-samples-mqtt.svg?branch=master)](https://travis-ci.org/SolaceSamples/solace-samples-mqtt)

# Getting Started Examples

## Disclaimer

This is a heavily modified repo from Solace Samples, made for me to document what I've learnt and also serves as a refresher for me to come back for reference.

## Contents

This repository contains code and matching tutorial walk throughs for different basic scenarios. It is best to view the associated [tutorials home page](https://dev.solace.com/samples/solace-samples-mqtt/).

## Prerequisites

There are no prerequisites.

## Build the Samples

    ./gradlew build

## Quick Play

2 Windows Batch files have been done up for you to execute a protobuf publisher and subscriber respectively. If you have Keycloak and Solace set up correctly according to the tutorial, you should observe both clients working correctly.

## Running the Samples in an IDE

To try individual samples, build the project from source and then run samples like the following:

    Samples I have wrote:

    ** JSON clients **
    ./build/staged/bin/jsonTopicSubscriber tcp://localhost:1883
    ./build/staged/bin/jsonConcurrentPublisher tcp://localhost:1883 1500 1000 0


    ** PROTOBUF clients **
    ./build/staged/bin/protobufTopicSubscriber tcp://localhost:1883
    ./build/staged/bin/protobufConcurrentPublisher tcp://localhost:1883 1500 1000 0


    Samples provided by Solace:

    ./build/staged/bin/topicPublisher tcp://localhost:1883 <id-token> <access-token>
    ./build/staged/bin/topicSubscriber tcp://localhost:1883 <id-token> <access-token>
    ./build/staged/bin/topicPublisherConcurrent tcp://localhost:1883 <id-token> <access-token>
    ./build/staged/bin/topicSubscriberForExpire tcp://localhost:1883 <id-token> <access-token>
    ./build/staged/bin/topicSubscriberForPublish tcp://localhost:1883 <id-token> <access-token>


    



