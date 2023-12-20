package com.solace.samples;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.solace.samples.BftProtoMsg.Bft;
import com.solace.samples.BftProtoMsg.OptionalData;



public class PublisherRunnable implements Runnable{

    MqttClient mqttClient;
    int id;
    int num_msgs;
    CountDownLatch latch;
    static boolean isShutdown = false;
    int frequency;
    int sleepTime;

    public PublisherRunnable(MqttClient mqttClient, int i, CountDownLatch latch, int num_msgs, int frequency, int sleepTime){
        this.mqttClient = mqttClient;
        this.id = i;
        this.latch = latch;
        this.num_msgs = num_msgs;
        this.frequency = frequency;
        this.sleepTime = sleepTime;
    }

    public void run(){
        try{   
            for (int i=0; i<num_msgs; i++) {

	            // Create a Mqtt message
	            //String content = "Hello for the " + Integer.toString(i) + " time! From: " + Thread.currentThread().getName();

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
                    .setId("TESTER " + this.id)
                    .setType("JavaClient")
                    .setLatitude(randomLatInRange)
                    .setLongitude(randomLongInRange)
                    .setAltitude((int)randomAltInRange)
                    .setUpdatedTime(System.currentTimeMillis())
                    .setPubTime(System.currentTimeMillis())
                    .setOnline(true)
                    .setFrequency(this.frequency)
                    .setName("Message count: " + String.valueOf(i+1))
                    .build();
                

                byte[] byteArray = blueforce.toByteArray();

	            MqttMessage message = new MqttMessage(byteArray);
	            // Set the QoS on the Messages - 
	            // Here we are using QoS of 0 (equivalent to Direct Messaging in Solace)
	            message.setQos(0);
	            
	            System.out.println("Publishing protobuf (count: " + (i+1) + ") from: " + Thread.currentThread().getName());
	            
	            // Publish the message
	            //mqttClient.publish("solace/samples/mqtt/direct/pub", message);
                mqttClient.publish("solace/bft", message);
	            try {
	            	Thread.sleep(this.sleepTime);
	            } catch (InterruptedException e) {
	            	isShutdown = true;
	            }
	            if (System.in.available() != 0 || isShutdown) break;
            }
            latch.countDown();
        }catch(Exception err){
            err.printStackTrace();
        }
    }
}