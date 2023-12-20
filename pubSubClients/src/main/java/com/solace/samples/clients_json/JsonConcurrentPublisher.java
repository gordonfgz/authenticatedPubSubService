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

package com.solace.samples.clients_json;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.solace.samples.network.KeycloakTokenRequester;



/**
 * A Mqtt topic publisher 
 *
 */
public class JsonConcurrentPublisher {
	
	static boolean isShutdown = false;
    
    public void run(String... args) throws IOException {
        System.out.println("JsonConcurrentPublisher initializing...");

        // Initialising variables for Keycloak token retrieval
        String keycloakTokenEndpoint = "https://localhost:7778/auth/realms/master/protocol/openid-connect/token";
        String keycloakUsername = "testuser";
        String keycloakPassword = "password";

        KeycloakTokenRequester tokenRequester = new KeycloakTokenRequester(keycloakTokenEndpoint, keycloakUsername, keycloakPassword);
        String[] tokenArray = tokenRequester.getTokenArray();

        String host = args[0];
        String idToken = tokenArray[0];
        String accessToken = tokenArray[1];
        String SolaceOAuthProfile = "JavaClientToKeyCloak";

        int num_threads = Integer.parseInt(args[1]);
        int message_rate = Integer.parseInt(args[2]);
        int running_duration = 1;
        if (args.length > 3) running_duration = Integer.parseInt(args[3]);

        try {
            // Create an Mqtt client
            MqttClient mqttClient = new MqttClient(host, "HelloWorldPub_" + UUID.randomUUID().toString().substring(0,8));
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName("doesntmatter");
            String password = "OPENID~" + SolaceOAuthProfile + "~" + idToken + "~" + accessToken;
            connOpts.setPassword(password.toCharArray());
            
            // Connect the client
            System.out.println("Connecting to Solace messaging at " + host);
            mqttClient.connect(connOpts);
            System.out.println("Connected.  Press [ENTER] to quit.");
            
            System.out.println("RUNNING CONCURRENT SYSTEM.");

            // 
            ExecutorService executor= Executors.newFixedThreadPool(num_threads);
            CountDownLatch latch = new CountDownLatch(num_threads);
            try{
                for ( int i=0; i < num_threads; i++){
                    executor.execute(new JsonPublisherRunnable(mqttClient, i, latch, running_duration, message_rate, message_rate));                
                }
            }catch(Exception err){
                err.printStackTrace();
            }
            executor.shutdown(); // once you are done with ExecutorService


            // Wait for threads to finish executing
            try {
                latch.await();
            } catch (InterruptedException E) {
                // handle
            }
            // Disconnect the client
            mqttClient.disconnect();
            
            System.out.println("Messages published. Exiting");

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
        if (args.length != 4) {
            System.out.println("Usage: JsonConcurrentPublisher tcp://<host:port> [num_threads] [message_rate] [running_duration]");
            System.out.println();
            System.exit(-1);
        }
        new JsonConcurrentPublisher().run(args);
    }
}
