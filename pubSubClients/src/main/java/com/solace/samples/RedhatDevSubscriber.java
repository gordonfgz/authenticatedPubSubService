/*
 * Copyright 2016-2022 Solace Corporation. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.solace.samples;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.protobuf.util.JsonFormat;
import com.solace.samples.BftProtoMsg.Bft;



/**
 * A Mqtt topic subscriber
 *
 */
public class RedhatDevSubscriber {

    static boolean isShutdown = false;
    int incomingMsgCounter = 0;

    public void incrementMsgCounter() {
        this.incomingMsgCounter += 1;
    }

    public void run(String... args) throws IOException {
        System.out.println("RedhatDevSubscriber initializing...");

        //AUTOMATED TOKEN RETRIEVAL
        String kc_url = "https://keycloak-keycloak.apps.cluster-76wwd.76wwd.sandbox51.opentlc.com/auth/realms/basic/protocol/openid-connect/token";
        String kc_username = "haowei";
        String kc_password = "password";
        KeyCloakTokenRequester requester = new KeyCloakTokenRequester(kc_url, kc_username, kc_password);
        String[] tokenArray = requester.getTokenArray();
        String idToken = tokenArray[0];
        String accessToken = tokenArray[1];
        

        String host = args[0];
        

        try {
            // Create an Mqtt client
            MqttClient mqttClient = new MqttClient(host, "HelloWorldSub_" + UUID.randomUUID().toString().substring(0,8));
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName("DOESNTMATTER");
            String password = "OPENID~RedHatKeyCloakProd~" + idToken + "~" + accessToken;
            connOpts.setPassword(password.toCharArray());
            
            // Connect the client
            System.out.println("Connecting to Solace messaging at "+host);
            mqttClient.connect(connOpts);
            System.out.println("Connected");

            // Topic filter the client will subscribe to
            //final String subTopic = "solace/samples/+/direct/#";
            final String subTopic = "solace/publish/json";
            //final String subTopic = "solace/bft";
            
            // Callback - Anonymous inner-class for receiving messages
            mqttClient.setCallback(new MqttCallback() {

                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // Called when a message arrives from the server that
                    // matches any subscription made by the client
                    String time = new Timestamp(System.currentTimeMillis()).toString();
                    
                    System.out.println("\nReceived a Message!" +
                            "\n\tTime:    " + time + 
                            "\n\tTopic:   " + topic + 
                            // for receiving Protobuf
                            //"\n\tMessage: " + new String(JsonFormat.printer().print(Bft.parseFrom(message.getPayload()))) + 
                            // for receiving Json
                            "\n\tMessage: " + new String(message.getPayload()) + 
                            "\n\tQoS:     " + message.getQos() + "\n");
                    incrementMsgCounter();
                }

                public void connectionLost(Throwable cause) {
                    System.out.println("Connection to Solace messaging lost!" + cause.getMessage());
                    isShutdown = true;
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                }

            });
            
            // Subscribe client to the topic filter and a QoS level of 0
            System.out.println("Subscribing client to topic: " + subTopic);
            mqttClient.subscribe(subTopic, 0);
            System.out.println("Subscribed. Press [ENTER] to quit.");

            // Wait for the message to be received
            
            try {
                while (System.in.available() == 0 && !isShutdown) {
                    Thread.sleep(1000);  // wait 1 second
                }
            } catch (InterruptedException e) {
                // Thread.sleep() interrupted... probably getting shut down
            }
            
            // Disconnect the client
            mqttClient.disconnect();
            System.out.println("Exiting");
            System.out.println("Received: " + Integer.toString(incomingMsgCounter) + " messages");

            System.exit(0);
        } catch (MqttException me) {
            System.out.println("Exception:   " + me);
            System.out.println("Reason Code: " + me.getReasonCode());
            System.out.println("Message:     " + me.getMessage());
            if (me.getCause() != null) System.out.println("Cause:       " + me.getCause());
            me.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        // Check command line arguments
        if (args.length != 1) {
            System.out.println("Usage: RedhatDevSubscriber <mqtt-host-name>");
            System.out.println();
            System.exit(-1);
        }
        new RedhatDevSubscriber().run(args);
    }
}
