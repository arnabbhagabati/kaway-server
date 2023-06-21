package com.kaway.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaway.beans.SecType;
import com.kaway.beans.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BSEService {

    @Autowired
    HTTPClient client;

    public Map<String,Security> getSecList(){
        Map<String,Security> secMap = new HashMap<>();
        String url = "https://api.bseindia.com/BseIndiaAPI/api/ListofScripData/w?Group=&Scripcode=&industry=&segment=&status=Active";
        String rawdata = client.getHTTPData(url);

        JsonArray rawJson = new JsonParser().parse(rawdata).getAsJsonArray();

        for(JsonElement data : rawJson){
            JsonObject currData = (JsonObject)data;
            Security security = new Security( currData.get("SCRIP_CD").getAsString(),currData.get("scrip_id").getAsString(),currData.get("Scrip_Name").getAsString(), SecType.STOCK);
            secMap.put(currData.get("SCRIP_CD").getAsString(),(security));
        }

        return secMap;

    }
}
