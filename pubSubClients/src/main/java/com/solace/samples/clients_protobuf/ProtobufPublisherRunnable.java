package com.solace.samples.clients_protobuf;

import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;
import java.util.Random;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.solace.samples.protocgenerated.BftProtoMsg.Bft;
import java.util.Date;



public class ProtobufPublisherRunnable implements Runnable{

    MqttClient mqttClient;
    int id;
    int running_duration;
    CountDownLatch latch;
    static boolean isShutdown = false;
    int message_rate;
    int sleepTime;

    public ProtobufPublisherRunnable(MqttClient mqttClient, int i, CountDownLatch latch, int running_duration, int message_rate, int sleepTime){
        this.mqttClient = mqttClient;
        this.id = i;
        this.latch = latch;
        this.running_duration = running_duration;
        this.message_rate = message_rate;
        this.sleepTime = sleepTime;
    }

    public void run(){
        try{   
            if (running_duration != 0) {
                int num_msgs = (int) running_duration/sleepTime;
                for (int i=0; i<num_msgs; i++) {
                    if (System.in.available() != 0 || isShutdown) break;
                    publishARandomMessage(i);
                }
                latch.countDown();
            } else {
                int counter = 0;
                while(true){
                    publishARandomMessage(counter);
                    counter += 1;
                }
            }

            
        }catch(Exception err){
            err.printStackTrace();
        }
    }




    public void publishARandomMessage(int i) {
        try {
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
            long currentTime = System.currentTimeMillis();
            // creating protobuf 
            Bft blueforce = Bft.newBuilder()
                .setId("TESTER " + this.id)
                .setType("JavaClient")
                .setLatitude(randomLatInRange)
                .setLongitude(randomLongInRange)
                .setAltitude(randomAltInRange)
                .setUpdatedTime(currentTime)
                .setPubTime(currentTime)
                .setOnline(true)
                .setFrequency(this.message_rate+100)
                .setName("Message count: " + String.valueOf(i+1))
                .build();
            

            byte[] byteArray = blueforce.toByteArray();

            MqttMessage message = new MqttMessage(byteArray);
            // Set the QoS on the Messages - 
            // Here we are using QoS of 0 (equivalent to Direct Messaging in Solace)
            message.setQos(0);
  
            // Publish the message
            mqttClient.publish("solace/protobuf", message);
            Date date = new Date(System.currentTimeMillis());

            // Create a SimpleDateFormat for formatting
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // Format the Date object as a string
            String formattedDateTime = dateFormat.format(date);
            System.out.println("Published protobuf (count: " + (i+1) + ") from: " + Thread.currentThread().getName() + " @ " + formattedDateTime);
            try {
                Thread.sleep(this.sleepTime);
            } catch (InterruptedException e) {
                isShutdown = true;
            }
            
        }catch(Exception err){
            err.printStackTrace();
        }

    }

}

