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

    public String getHTTPData(String endpoint){
        StringBuilder op = new StringBuilder();
        System.out.println("HTTP call with URL "+endpoint);

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

            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            //System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                //System.out.println(output);
                op.append(output);
            }

            conn.disconnect();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return op.toString();
    }
}
