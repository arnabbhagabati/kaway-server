package com.kaway.service;

import com.google.gson.*;
import com.kaway.beans.NasdaqHistDataPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NasdaqService {

    @Autowired
    HTTPClient client;

    private static String NASDAQ_HIST_DATA_BASE = "https://data.nasdaq.com/api/v3/datasets/";

    //Todo : move this to more secure loc
    private static String API_KEY = "-oTncyawbkcCWCAn_Jqx";

    public List<NasdaqHistDataPoint> getHistData(String exchngCode,String stockCode){
        String url =NASDAQ_HIST_DATA_BASE+"/"+exchngCode+"/"+stockCode+".json?API_KEY="+API_KEY;
        String rawdata = client.getHTTPData(url);

        List<NasdaqHistDataPoint> op = new ArrayList<>();


        Gson g = new Gson();

        // De-serialize to an object
        //Person person = g.fromJson("{\"name\": \"John\"}", Person.class);
        //System.out.println(person.name); //John

        // Read a single attribute
        JsonObject rawJson = new JsonParser().parse(rawdata).getAsJsonObject().getAsJsonObject("dataset");
        JsonArray columnList = rawJson.getAsJsonArray("column_names");

        int dateIdx = -1;
        int closeIdx = -1;
        for(int i=0;i<columnList.size();i++){
            if(columnList.get(i).getAsString().equals("Date")){
                dateIdx = i;
            }else if(columnList.get(i).getAsString().equals("Close")){
                closeIdx = i;
            }
        }
        JsonArray dataArr = rawJson.getAsJsonArray("data");
        for(JsonElement data : dataArr){
            JsonArray currData = (JsonArray)data;
            NasdaqHistDataPoint dp = new NasdaqHistDataPoint(currData.get(dateIdx).getAsString(),currData.get(closeIdx).getAsFloat());
            op.add(dp);
        }

        return op;

    }
}
