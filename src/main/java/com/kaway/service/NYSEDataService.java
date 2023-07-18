package com.kaway.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaway.beans.SecType;
import com.kaway.beans.Security;
import com.kaway.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static com.kaway.main.KawayConstants.GET_INDEX_CONSTITUENTS;

@Service
public class NYSEDataService {

    @Autowired
    FileUtil fileUtil;

    public List<Security> getSecList() throws IOException {

        List<Security> secList = new ArrayList<>();

        URL url = new URL("https://www.nyse.com/api/quotes/filter");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");

        httpConn.setRequestProperty("Accept", "*/*");
        httpConn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
        httpConn.setRequestProperty("Cache-Control", "no-cache");
        httpConn.setRequestProperty("Connection", "keep-alive");
        httpConn.setRequestProperty("Content-Type", "application/json");

        httpConn.setRequestProperty("Pragma", "no-cache");
        httpConn.setRequestProperty("Referer", "https://www.nyse.com/listings_directory/stock");

        httpConn.setRequestProperty("Sec-Fetch-Site", "same-origin");
        httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");

        httpConn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
        writer.write("{\"instrumentType\":\"EQUITY\",\"pageNumber\":1,\"sortColumn\":\"NORMALIZED_TICKER\",\"sortOrder\":\"ASC\",\"maxResultsPerPage\":10000,\"filterToken\":\"\"}");
        writer.flush();
        writer.close();
        httpConn.getOutputStream().close();

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        Scanner s = new Scanner(responseStream).useDelimiter("\\A");
        String response = s.hasNext() ? s.next() : "";
        System.out.println(response);

        JsonArray data = new JsonParser().parse(response).getAsJsonArray();
        for(JsonElement stock : data){
            JsonObject currData = (JsonObject)stock;
            Security security = new Security(currData.get("symbolTicker").getAsString(), currData.get("exchangeId").getAsString(), currData.get("instrumentName").getAsString(), currData.get("symbolTicker").getAsString(), SecType.STOCK);
            secList.add(security);
        }

        secList.addAll(getIndices());

        return secList;

    }


    private List<Security> getIndices() throws FileNotFoundException {

        List<Security> list = new ArrayList<>();
        /*String url = "https://www.ice.com/publicdocs/data/NYSE_Equity_Index_Ticker_List.xlsx";
        List<String> rawdata = csvHttp.getHTTPData(url);

        int cnt = 0;
        for(String s : rawdata){
            String[] fields = s.split(",");
            if(cnt>0){
                list.add(new Security("^"+fields[0],"^"+fields[0],fields[1], fields[1],SecType.INDEX));
            }
            cnt++;
        }*/


        // Todo : Remove hard code, get from https://www.ice.com/publicdocs/data/NYSE_Equity_Index_Ticker_List.xlsx
        //  or https://www.ice.com/market-data/indices/equity-indices/products other online
        File f = new File("src/main/resources/NYSE_INDICES.csv");
        if(f.exists() && !f.isDirectory()) {
            List<List<String>> constituentSecs = fileUtil.getCsvRecords(f);
            List<String> constituents = new ArrayList<>();
            int cnt =0;
            for(List<String> consSec : constituentSecs){
                if(cnt>0) {
                    Security sec = new Security("^" + consSec.get(0), "^" + consSec.get(0), consSec.get(1), consSec.get(1).replaceFirst("NYSE ",""), SecType.INDEX);
                    list.add(sec);
                }
                cnt++;
            }
        }

        return list;
    }
}
