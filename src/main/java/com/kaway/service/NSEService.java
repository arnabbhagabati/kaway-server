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
import java.util.List;

@Service
public class NSEService {


    @Autowired
    HTTPClientForCSV client;

    public List<Security> getSecList(){
        List<Security> list = new ArrayList<>();
        String url = "https://archives.nseindia.com/content/equities/EQUITY_L.csv";
        List<String> rawdata = client.getHTTPData(url);

        int cnt = 0;
        for(String s : rawdata){
            String[] fields = s.split(",");
            if(cnt>0){
                list.add(new Security(fields[0],fields[0],fields[1], SecType.UNKNOWN));
            }
            cnt++;
        }

        return list;

    }

}
