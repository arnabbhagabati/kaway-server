package com.kaway.service;

import com.google.gson.*;
import com.kaway.beans.NasdaqHistDataPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NasdaqService {

    @Autowired
    HTTPClient client;

    private static String NASDAQ_HIST_DATA_BASE = "https://data.nasdaq.com/api/v3/datasets/";
    private static int GAP_BETWEEN_CALLS = 5000;
    private static long LAST_CALL_TIME = System.currentTimeMillis();

    //Todo : move this to more secure loc
    private static String API_KEY = "-oTncyawbkcCWCAn_Jqx";

    public List<NasdaqHistDataPoint> getHistData(String exchngCode,String stockCode) throws InterruptedException {

        if((System.currentTimeMillis() - LAST_CALL_TIME) < GAP_BETWEEN_CALLS){
            System.out.println("Too frequent calls time is "+System.currentTimeMillis()+" LAST_CALL_TIME "+LAST_CALL_TIME);
            Thread.sleep(GAP_BETWEEN_CALLS);
        }
        LAST_CALL_TIME = System.currentTimeMillis();
        System.out.println("LAST call time is "+LAST_CALL_TIME);

        String url =NASDAQ_HIST_DATA_BASE+"/"+exchngCode+"/"+stockCode+".json?API_KEY="+API_KEY;
        String rawdata = client.getHTTPData(url);

        List<NasdaqHistDataPoint> op = new ArrayList<>();

        Gson g = new Gson();

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

        List<NasdaqHistDataPoint> op1 = op.stream().sorted(new Comparator<NasdaqHistDataPoint>() {
            @Override
            public int compare(NasdaqHistDataPoint o1, NasdaqHistDataPoint o2) {
                try {
                    Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(o1.getTime());
                    Date date2 = new SimpleDateFormat("yyyy-MM-dd").parse(o2.getTime());

                    return date1.compareTo(date2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        }).collect(Collectors.toList());

        return op1;

    }
}
