package com.kaway.service;

import com.google.gson.*;
import com.kaway.beans.BSEHistDataPoint;
import com.kaway.beans.DataPoint;
import com.kaway.beans.NasdaqHistDataPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.kaway.main.KawayConstants.*;

@Component
public class NSEDataService {

    @Autowired
    HTTPClient client;

    private static String NSE_HIST_DATA_BASE = "https://query2.finance.yahoo.com/v8/finance/chart/";
    private static int GAP_BETWEEN_CALLS = 5000;
    private static AtomicLong LAST_CALL_TIME = new AtomicLong(System.currentTimeMillis());

    //Todo : move this to more secure loc
    private static String API_KEY = "-oTncyawbkcCWCAn_Jqx";

    public synchronized List<DataPoint> getHistData(String exchngCode, String stockCode) throws InterruptedException {

        System.out.println("LAST call time is "+LAST_CALL_TIME);
        if((System.currentTimeMillis() - LAST_CALL_TIME.get()) < GAP_BETWEEN_CALLS){
            System.out.println("Too frequent calls. Time is "+System.currentTimeMillis()+" LAST_CALL_TIME "+LAST_CALL_TIME);
            Thread.sleep(GAP_BETWEEN_CALLS);
        }
        LAST_CALL_TIME.set(System.currentTimeMillis());
        System.out.println("Calling  "+NSE_HIST_DATA_BASE+" for "+stockCode+" at "+LAST_CALL_TIME );

        String url =NSE_HIST_DATA_BASE+stockCode+".NS?formatted=true&interval=1d&range=2y";
        String rawdata = client.getHTTPData(url);

        List<DataPoint> op = new ArrayList<>();

        Gson g = new Gson();

        // Read a single attribute
        JsonObject rawJson = new JsonParser().parse(rawdata).getAsJsonObject().getAsJsonObject("chart");
        JsonObject dataArr = rawJson.getAsJsonArray("result").get(0).getAsJsonObject();
        JsonArray timeStamps = dataArr.getAsJsonArray("timestamp");
        JsonArray openData = dataArr.getAsJsonObject("indicators").getAsJsonArray("quote").get(0).getAsJsonObject().getAsJsonArray("open");
        JsonArray closeData = dataArr.getAsJsonObject("indicators").getAsJsonArray("quote").get(0).getAsJsonObject().getAsJsonArray("close");
        JsonArray highData = dataArr.getAsJsonObject("indicators").getAsJsonArray("quote").get(0).getAsJsonObject().getAsJsonArray("high");
        JsonArray lowData = dataArr.getAsJsonObject("indicators").getAsJsonArray("quote").get(0).getAsJsonObject().getAsJsonArray("low");
        JsonArray volumeData = dataArr.getAsJsonObject("indicators").getAsJsonArray("quote").get(0).getAsJsonObject().getAsJsonArray("volume");
        //System.out.println("c=urr milli time "+System.currentTimeMillis());
        for(int i=0;i< timeStamps.size();i++){
            long timeStamp = timeStamps.get(i).getAsLong();
            Date d = new Date(timeStamp*1000);
            String date = new SimpleDateFormat(US_DATE_FORMAT).format(d);
            float open = openData.get(i).getAsFloat();
            float close = closeData.get(i).getAsFloat();
            float high = highData.get(i).getAsFloat();
            float low = lowData.get(i).getAsFloat();
            int vol = volumeData.get(i).getAsInt();
            DataPoint dp = new BSEHistDataPoint(date,open,close,high,low,vol);
            op.add(dp);
        }

        return op;

    }
}
