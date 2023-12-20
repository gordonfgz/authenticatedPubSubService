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
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * A Mqtt topic publisher 
 *
 */
public class JsonTestPublisher2 {
	
	static boolean isShutdown = false;
    
    public void run(String... args) throws IOException {
        System.out.println("JsonTestPublisher2 initializing...");

        String host = args[0];
        String idToken = args[1];
        String accessToken = args[2];
        int num_threads = Integer.parseInt(args[3]);
        int num_msgs = 1;
        if (args.length > 4) num_msgs = Integer.parseInt(args[4]);

        try {
            // Create an Mqtt client
            MqttClient mqttClient = new MqttClient(host, "HelloWorldPub_" + UUID.randomUUID().toString().substring(0,8));
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName("DOESNTMATTER");
            String password = "OPENID~KeyCloak~" + idToken + "~" + accessToken;
            if (args.length > 2) connOpts.setPassword(password.toCharArray());
            
            // Connect the client
            System.out.println("Connecting to Solace messaging at " + host);
            mqttClient.connect(connOpts);
            System.out.println("Connected.  Press [ENTER] to quit.");
            
            System.out.println("RUNNING CONCURRENT SYSTEM.");

            //ExecutorService executor= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            ExecutorService executor= Executors.newFixedThreadPool(num_threads);
            CountDownLatch latch = new CountDownLatch(num_threads);
            try{
                executor.execute(new JsonPublisherRunnable(mqttClient, 1, latch, num_msgs, 10000, 9000));
                executor.execute(new JsonPublisherRunnable(mqttClient, 2, latch, num_msgs, 3000, 2000));
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
        if (args.length < 5) {
            System.out.println("Usage: JsonTestPublisher2 tcp://<host:port> [id_token] [access_token] [num_threads] [num_msgs per thread]");
            System.out.println();
            System.exit(-1);
        }
        new JsonTestPublisher2().run(args);
    }
}
