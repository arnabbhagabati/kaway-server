package com.kaway.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaway.beans.SecType;
import com.kaway.beans.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class LSEService {


    @Autowired
    HTTPClient client;

    private String EOD_Hist_Data_API_KEY = "64aaa015a80121.74386960";

    public List<Security> getSecList() throws IOException {

        List<Security> secList = new ArrayList<>();
        String url = "https://eodhistoricaldata.com/api/exchange-symbol-list/LSE?fmt=json&api_token=" + EOD_Hist_Data_API_KEY;

        String rawdata = client.getHTTPData(url);

        JsonArray arrData = new JsonParser().parse(rawdata).getAsJsonArray();
        for (JsonElement stock : arrData) {
            JsonObject currData = (JsonObject) stock;
            Security security = new Security(currData.get("Code").getAsString(), currData.get("Name").getAsString(), currData.get("Name").getAsString(), currData.get("Code").getAsString(), SecType.STOCK);
            secList.add(security);
        }

        return secList;

    }
}
