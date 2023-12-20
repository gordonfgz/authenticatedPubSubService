package com.solace.samples;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class KeyCloakTokenRequester {

    String url;
    String username;
    String password;

    public KeyCloakTokenRequester(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    
    public String[] getTokenArray() {
        
        
        String data = "grant_type=password&scope=openid&username=" + this.username + "&password="+ this.password + "&client_id=solace";

        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }

        try {
        URL apiUrl = new URL(this.url);
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

        // Set the request method to POST
        connection.setRequestMethod("POST");

        // Enable input and output streams
        connection.setDoOutput(true);

        // Set the content type
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        // Write the data to the output stream
        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
            outputStream.writeBytes(data);
            outputStream.flush();
        }

        // Get the response code
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        System.out.println("Logged in as: " + username);

        // Read the response as JSON
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // Parse the JSON response
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

            String[] stringArray = new String[2];
            stringArray[0] = jsonResponse.get("id_token").getAsString();
            stringArray[1] = jsonResponse.get("access_token").getAsString();
            // Close the connection
            connection.disconnect();
            
            return stringArray;
        }

        
      } catch (IOException e) {
        e.printStackTrace();
      }
        return null;
    }
}

