package com.kaway.service;

import org.apache.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

@Component
public class HTTPClient {

    private static String MBM_API_KEY = "7reqMumOszd0fN3SvIH4JizL40IO22R7oEaoVvBxRu4Id8VrKL101ZIGqsde";

    public String getHTTPData(String endpoint){
        StringBuilder op = new StringBuilder();
        System.out.println("HTTP call with URL "+endpoint+ " at "+System.currentTimeMillis());

        try {

            //String encodedUrl = URLEncoder.encode(endpoint,"UTF-8");
            URL url = new URL(endpoint);

            /*WebClient webClient = WebClient.create();

            WebClient.ResponseSpec responseSpec = webClient.get()
                    .uri(endpoint)
                    .header("accept", "*//*")
                    .header("accept-language", "en-US,en;q=0.9")
                    .header("Content-Type", "application/json; utf-8")
                    .header("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36")
                    .retrieve();

            responseBody = responseSpec.bodyToMono(String.class).block();*/



            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            //conn.setRequestProperty("Content-Type", "application/json; utf-8");  // Note - yahho fin api does not work with utf 8
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
            conn.setRequestProperty("X-Mboum-Secret",MBM_API_KEY);

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode()+ " for "+url);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            //System.out.println("Output from Server .... \n");

            int BUFFER_SIZE=1024;
            char[] buffer = new char[BUFFER_SIZE]; // or some other size,
            int charsRead = 0;
            while ( (charsRead  = br.read(buffer, 0, BUFFER_SIZE)) != -1) {
                op.append(buffer, 0, charsRead);
            }
          /*  while ((output = br.readLine()) != null) {
                //System.out.println(output);
                op.append(output);
            }*/

            conn.disconnect();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return op.toString();
    }
}
