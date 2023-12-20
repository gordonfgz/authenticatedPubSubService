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
import java.util.Random;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.protobuf.util.JsonFormat;
import com.solace.samples.BftProtoMsg.Bft;

/**
 * A Mqtt topic publisher 
 *
 */
public class JsonTestPublisher1 {
	
	static boolean isShutdown = false;
    
    public void run(String... args) throws IOException {
        System.out.println("JsonTestPublisher1 initializing...");

        String host = args[0];
        String idToken = args[1];
        String accessToken = "";
        if (args.length > 2) accessToken = args[2];

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
            
            for (int i=0; i<10; i++) {            
	            // Create a Mqtt message
	            // Randomising lat and long
                Random random = new Random();

                // Define the range (exclusive upper bound)
                double longMinValue = -180;
                double longMaxValue = 180;
                double latMinValue = -90;
                double latMaxValue = 90;
                double altMinValue = 0;
                double altMaxValue = 80000;

                // Generate a random long within the specified range
                double randomLongInRange = longMinValue + random.nextDouble(longMaxValue - longMinValue);
                double randomLatInRange = latMinValue + random.nextDouble(latMaxValue - latMinValue);
                double randomAltInRange = altMinValue + random.nextDouble(altMaxValue - altMinValue);

                // creating protobuf 
                Bft blueforce = Bft.newBuilder()
                    .setId("TESTER")
                    .setType("JavaClient")
                    .setLatitude(randomLatInRange)
                    .setLongitude(randomLongInRange)
                    .setAltitude((int)randomAltInRange)
                    .setUpdatedTime(System.currentTimeMillis())
                    .setPubTime(System.currentTimeMillis())
                    .setOnline(true)
                    .setFrequency(10000)
                    .setName(String.valueOf(i+1))
                    .build();
                

                String jsonBft = JsonFormat.printer().print(blueforce);
                byte[] byteArray = jsonBft.getBytes();

	            MqttMessage message = new MqttMessage(byteArray);
	            // Set the QoS on the Messages - 
	            // Here we are using QoS of 0 (equivalent to Direct Messaging in Solace)
	            message.setQos(1);
	            
	            System.out.println("Publishing message, msg count: " + String.valueOf(i+1));
	            
	            // Publish the message
	            //mqttClient.publish("solace/samples/mqtt/direct/pub", message);
                mqttClient.publish("solace/bft", message);
	            try {
	            	Thread.sleep(9000);
	            } catch (InterruptedException e) {
	            	isShutdown = true;
	            }
	            if (System.in.available() != 0 || isShutdown) break;
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
        if (args.length < 2) {
            System.out.println("Usage: JsonTestPublisher1 tcp://<host:port> [id_token] [access_token]");
            System.out.println();
            System.exit(-1);
        }
        new JsonTestPublisher1().run(args);
    }
}
