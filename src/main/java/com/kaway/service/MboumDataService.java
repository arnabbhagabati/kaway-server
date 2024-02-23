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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.kaway.main.KawayConstants.*;

@Component
public class MboumDataService {

    @Autowired
    HTTPClient client;

    private static String NSE_HIST_DATA_BASE = "https://mboum.com/api/v1/hi/history/?symbol=";
    private static int GAP_BETWEEN_CALLS = 200;
    private static AtomicLong LAST_CALL_TIME = new AtomicLong(System.currentTimeMillis());
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public synchronized List<DataPoint> getHistData(String exchngCode, String stockCode, String type) throws InterruptedException {

        System.out.println("LAST call time is "+LAST_CALL_TIME);
        if((System.currentTimeMillis() - LAST_CALL_TIME.get()) < GAP_BETWEEN_CALLS){
            System.out.println("Too frequent calls. Time is "+System.currentTimeMillis()+" LAST_CALL_TIME "+LAST_CALL_TIME);
            Thread.sleep(GAP_BETWEEN_CALLS);
        }
        LAST_CALL_TIME.set(System.currentTimeMillis());
        System.out.println("Calling  "+NSE_HIST_DATA_BASE+" for "+stockCode+" at "+LAST_CALL_TIME );

        Map<String,List<DataPoint>> opMap = new HashMap<>();
        String url =NSE_HIST_DATA_BASE+getASCIIStockCode(stockCode);
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

        String url1 = url+"&interval=1d&diffandsplits=true";
        String rawdata1d = client.getHTTPData(url1);

        List<DataPoint> op = new ArrayList<>();
        JsonObject json1d = new JsonParser().parse(rawdata1d).getAsJsonObject().getAsJsonObject("data");
        GsonBuilder builder = new GsonBuilder();
        LinkedTreeMap<String,LinkedTreeMap> mapdata1d = (LinkedTreeMap) builder.create().fromJson(json1d, Object.class);

        for(Map.Entry<String,LinkedTreeMap> e : mapdata1d.entrySet()){
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
        List<DataPoint> cleandata = cleandata(op);
        opMap.put(ONE_DAY,cleandata);

        return cleandata;

    }


    public synchronized List<DataPoint> get15mData(String exchngCode, String stockCode, String type) throws InterruptedException {

        System.out.println("LAST call time is "+LAST_CALL_TIME);
        if((System.currentTimeMillis() - LAST_CALL_TIME.get()) < GAP_BETWEEN_CALLS){
            System.out.println("Too frequent calls. Time is "+System.currentTimeMillis()+" LAST_CALL_TIME "+LAST_CALL_TIME);
            Thread.sleep(GAP_BETWEEN_CALLS);
        }
        LAST_CALL_TIME.set(System.currentTimeMillis());
        System.out.println("Calling  "+NSE_HIST_DATA_BASE+" for "+stockCode+" at "+LAST_CALL_TIME );

        Map<String,List<DataPoint>> opMap = new HashMap<>();
        String url =NSE_HIST_DATA_BASE+getASCIIStockCode(stockCode);
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


        String url2 = url+"&interval=15m&diffandsplits=true";
        String rawdata15m = client.getHTTPData(url2);

        List<DataPoint> op = new ArrayList<>();
        JsonObject json15m = new JsonParser().parse(rawdata15m).getAsJsonObject().getAsJsonObject("data");
        GsonBuilder builder = new GsonBuilder();
        LinkedTreeMap<String,LinkedTreeMap> mapdata15m = (LinkedTreeMap) builder.create().fromJson(json15m, Object.class);

        for(Map.Entry<String,LinkedTreeMap> e : mapdata15m.entrySet()){
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
        List<DataPoint>  cleandata = cleandata(op);
        opMap.put(FIFTEEN_MIN,cleandata);

        return cleandata;

    }


    private static float round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }


    private List<DataPoint> cleandata(List<DataPoint> data){
        List<DataPoint> sortedData = data.stream().sorted(new Comparator<DataPoint>() {
            @Override
            public int compare(DataPoint o1, DataPoint o2) {
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

        List<DataPoint> freshData =  new ArrayList<>();
        DataPoint prev = null;
        for(DataPoint dp : sortedData){
            if((prev != null && dp.getUtcTimestamp() != prev.getUtcTimestamp()) && dp.getClose() > 0){
                freshData.add(dp);
                prev=dp;
            }else if(prev == null && dp.getClose() > 0){
                freshData.add(dp);
                prev=dp;
            }
        }

        return freshData;
    }

    private String getASCIIStockCode(String stock){
        return stock.replace("&","%26");
    }
}
