package com.kaway.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaway.beans.BSESec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BSEService {

    @Autowired
    HTTPClient client;

    public List<BSESec> getSecList(){
        List<BSESec> list = new ArrayList<>();
        String url = "https://api.bseindia.com/BseIndiaAPI/api/ListofScripData/w?Group=&Scripcode=&industry=&segment=&status=Active";
        String rawdata = client.getHTTPData(url);

        JsonArray rawJson = new JsonParser().parse(rawdata).getAsJsonArray();

        for(JsonElement data : rawJson){
            JsonObject currData = (JsonObject)data;
            BSESec bseSec = new BSESec( currData.get("SCRIP_CD").getAsString(),currData.get("scrip_id").getAsString(),currData.get("Scrip_Name").getAsString());
            list.add(bseSec);
        }

        return list;

    }
}
