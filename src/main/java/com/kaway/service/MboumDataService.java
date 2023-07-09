package com.kaway.service;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import com.kaway.beans.BSEHistDataPoint;
import com.kaway.beans.DataPoint;
import com.kaway.beans.MboumDataPoint;
import com.kaway.beans.SecType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static com.kaway.main.KawayConstants.LSE_EXCHANGE;
import static com.kaway.main.KawayConstants.US_DATE_FORMAT;

@Component
public class MboumDataService {

    @Autowired
    HTTPClient client;

    private static String NSE_HIST_DATA_BASE = "https://mboum.com/api/v1/hi/history/?symbol=";
    private static int GAP_BETWEEN_CALLS = 5000;
    private static AtomicLong LAST_CALL_TIME = new AtomicLong(System.currentTimeMillis());
    private static final DecimalFormat df = new DecimalFormat("0.00");

    //Todo : move this to more secure loc
    private static String API_KEY = "YpChip3KKgvcSmkSCqPRHquVfuH5qdBQzZb5aldJGogr3CGQfFP10UVkWOx2";

    public synchronized List<DataPoint> getHistData(String exchngCode, String stockCode, String type) throws InterruptedException {

        System.out.println("LAST call time is "+LAST_CALL_TIME);
        if((System.currentTimeMillis() - LAST_CALL_TIME.get()) < GAP_BETWEEN_CALLS){
            System.out.println("Too frequent calls. Time is "+System.currentTimeMillis()+" LAST_CALL_TIME "+LAST_CALL_TIME);
            Thread.sleep(GAP_BETWEEN_CALLS);
        }
        LAST_CALL_TIME.set(System.currentTimeMillis());
        System.out.println("Calling  "+NSE_HIST_DATA_BASE+" for "+stockCode+" at "+LAST_CALL_TIME );

        String url =NSE_HIST_DATA_BASE+stockCode;
        switch(exchngCode){
            case "NSE":
                if(type.equals(SecType.INDEX.toString())){
                    //url = url+"&interval=1d&diffandsplits=true&apikey="+API_KEY;
                }else{
                    url =url+".NS";
                }
                break;
            case "BSE":
                if(type.equals(SecType.INDEX.toString())){
                    //url = url+"&interval=1d&diffandsplits=true&apikey="+API_KEY;
                }else{
                    url =url+".BO";
                }
                break;
            case LSE_EXCHANGE:
                if(type.equals(SecType.INDEX.toString())){
                    //url = url+"&interval=1d&diffandsplits=true&apikey="+API_KEY;
                }else{
                    url =url+".L";
                }
        }

        url = url+"&interval=1d&diffandsplits=true";

        String rawdata = client.getHTTPData(url);
        List<DataPoint> op = new ArrayList<>();

        JsonObject json = new JsonParser().parse(rawdata).getAsJsonObject().getAsJsonObject("data");

        GsonBuilder builder = new GsonBuilder();
        LinkedTreeMap<String,LinkedTreeMap> mapdata = (LinkedTreeMap) builder.create().fromJson(json, Object.class);


        for(Map.Entry<String,LinkedTreeMap> e : mapdata.entrySet()){
            LinkedTreeMap<String,Object> data = e.getValue();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate date = LocalDate.parse((String) data.get("date"), formatter);
            DateTimeFormatter us_format = DateTimeFormatter.ofPattern(US_DATE_FORMAT);

            float open = round((Double) data.get("open"),2);
            float close = round((Double) data.get("close"),2);
            float high = round((Double) data.get("high"),2);
            float low = round((Double) data.get("low"),2);
            int vol = ((Double) data.get("volume")).intValue();
            Long utcTime = ((Double) data.get("date_utc")).longValue();
            DataPoint dp = new MboumDataPoint(us_format.format(date),utcTime, open, close, high, low, vol);
            op.add(dp);
        }

        return op;

    }


    private static float round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }
}
