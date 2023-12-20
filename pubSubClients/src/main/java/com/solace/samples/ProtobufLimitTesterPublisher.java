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
public class ProtobufLimitTesterPublisher {
	
	static boolean isShutdown = false;
    
    public void run(String... args) throws IOException {
        System.out.println("ProtobufLimitTesterPublisher initializing...");

        LocalhostKeycloakTokenRequester tokenRequester = new LocalhostKeycloakTokenRequester();
        String[] tokenArray = tokenRequester.getTokenArray();

        String host = args[0];
        String idToken = tokenArray[0];
        String accessToken = tokenArray[1];
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
            String password = "OPENID~JavaClientToKeyCloak~" + idToken + "~" + accessToken;
            //String password = "OPENID~JavaClientToRedhatKeyCloak~" + idToken + "~" + accessToken;
            //String password = "OPENID~AndroidClientToKeyCloak~eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJpVzYtRXZreXhwSU1wVHpfZ3NlSndpLWRvWmdIYTBoaHliLXRGZGZJZzBZIn0.eyJleHAiOjE3MDAxNTUzODcsImlhdCI6MTcwMDExOTM4NywiYXV0aF90aW1lIjowLCJqdGkiOiJhMmUyODE5OC0zMDZmLTRlZDAtYmMwYS1jOTNjOTkxMjM5NjUiLCJpc3MiOiJodHRwczovLzEwLjAuMi4yOjc3NzgvYXV0aC9yZWFsbXMvbWFzdGVyIiwiYXVkIjoic29sYWNlIiwic3ViIjoiZTVkNWQyYWItMThlMS00OWQ3LTllMDYtYzY2NzljNTliZDY2IiwidHlwIjoiSUQiLCJhenAiOiJzb2xhY2UiLCJzZXNzaW9uX3N0YXRlIjoiMzQ4MGE0NDktZDkzMC00NWY0LTg3ZjYtZTZlMzEwYmM1YWFjIiwiYXRfaGFzaCI6IkZ5Y013Um1mdTdaQVBHaEtKcEMwbGciLCJhY3IiOiIxIiwic2lkIjoiMzQ4MGE0NDktZDkzMC00NWY0LTg3ZjYtZTZlMzEwYmM1YWFjIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImdyb3VwcyI6WyJ0ZXN0Z3JvdXAiXSwicHJlZmVycmVkX3VzZXJuYW1lIjoiMTM4c3FuX3B1Ymxpc2hlcl9zdWJzY3JpYmVyIn0.KBZaYBc9s-xouPXUrvfDQfRKlc5y9q5nJZmI0hPpuolEx2i9GTYcyVM9upKXgEabhCCTUnwwTH2dbp4vm6m62ricpdJwvHuE8hLAupXdasUgB7XaNgKITFRo1y-7ZLugbRp5q0yr9z1Lxm9h0MXPhobVVsIxGCBn0x6m6AbUHNBqSRNONQLMQsEDWc-xneFaxQCP6Nyj_72VeV4Re4eFWgCvX_woGFumQcKoEAnNUb2EVjifjYU7xkCON_SSLFLIcL7kE8_ebq83X-5ZUmdhknELJUppujU6Tmi3pD6vPdQCb3QB2axD3Y8Flhi256FVV8EN-rtuSoBtczuQQaNzlA~eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJpVzYtRXZreXhwSU1wVHpfZ3NlSndpLWRvWmdIYTBoaHliLXRGZGZJZzBZIn0.eyJleHAiOjE3MDAxNTUzODcsImlhdCI6MTcwMDExOTM4NywianRpIjoiYzg3ZjU1MTEtMDUzYy00OTZhLWExNGMtYjhlMmJhMzYxOTFkIiwiaXNzIjoiaHR0cHM6Ly8xMC4wLjIuMjo3Nzc4L2F1dGgvcmVhbG1zL21hc3RlciIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJlNWQ1ZDJhYi0xOGUxLTQ5ZDctOWUwNi1jNjY3OWM1OWJkNjYiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJzb2xhY2UiLCJzZXNzaW9uX3N0YXRlIjoiMzQ4MGE0NDktZDkzMC00NWY0LTg3ZjYtZTZlMzEwYmM1YWFjIiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLW1hc3RlciIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBlbWFpbCBwcm9maWxlIHNvbGFjZV9zY29wZSIsInNpZCI6IjM0ODBhNDQ5LWQ5MzAtNDVmNC04N2Y2LWU2ZTMxMGJjNWFhYyIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJncm91cHMiOlsidGVzdGdyb3VwIl0sInByZWZlcnJlZF91c2VybmFtZSI6IjEzOHNxbl9wdWJsaXNoZXJfc3Vic2NyaWJlciJ9.RrpZ-0wQoJKcmZji4I9FrZrbb6Q7MaeUQLFXUuknYK_Xk3WYsMGEAJc-xGetY03XThS54iH0MKy0YL2Ye24nKGuNiP6LR9bcfz8G_bsH1NQy11flKPOsBni_JN8nkOfBqAi1-apN_vPTX6vZkblKdIR55pF6HBkMIL6bl4YpN79CWWF6cxTrE1zxCenQSQCDhNh646EVlfEzwwgw0hV08C3FGQYJnhjQBQll2ZWCWvyOu9p71w8MMtn761_xs6tvKPbqpfrVDskq22pB7Ca24t7QgmImDIEMp5XxV57K91door0l04pVmkqYcLqHUvtHqmhC382dSxyOb30CCaxKLg";
            //String password = "admin";
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
                for ( int i=0; i < num_threads; i++){
                    executor.execute(new ProtobufTimerPublisherRunnable(mqttClient, i, latch, running_duration, message_rate, message_rate));                
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
        if (args.length < 4) {
            System.out.println("Usage: ProtobufLimitTesterPublisher tcp://<host:port> [num_threads] [message_rate] [running_duration]");
            System.out.println();
            System.exit(-1);
        }
        new ProtobufLimitTesterPublisher().run(args);
    }
}
