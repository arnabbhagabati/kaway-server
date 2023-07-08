package com.kaway.service;

import com.google.gson.*;
import com.kaway.beans.DataPoint;
import com.kaway.beans.NasdaqHistDataPoint;
import com.kaway.beans.SecType;
import com.kaway.beans.Security;
import com.kaway.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.kaway.main.KawayConstants.GET_INDEX_CONSTITUENTS;

@Component
public class NasdaqService {

    @Autowired
    HTTPClient client;

    @Autowired
    ZipHttpClient zipClient;

    @Autowired
    FileUtil fileUtil;

    @Autowired
    BSEService bseService;

    private static String NASDAQ_HIST_DATA_BASE = "https://data.nasdaq.com/api/v3/datasets";
    private static int GAP_BETWEEN_CALLS = 5000;
    private static AtomicLong LAST_CALL_TIME = new AtomicLong(System.currentTimeMillis());

    //Todo : move this to more secure loc
    private static String API_KEY = "-oTncyawbkcCWCAn_Jqx";

    public synchronized List<DataPoint> getHistData(String exchngCode, String stockCode,String type) throws InterruptedException {

        System.out.println("LAST call time is "+LAST_CALL_TIME);
        if((System.currentTimeMillis() - LAST_CALL_TIME.get()) < GAP_BETWEEN_CALLS){
            System.out.println("Too frequent calls. Time is "+System.currentTimeMillis()+" LAST_CALL_TIME "+LAST_CALL_TIME);
            Thread.sleep(GAP_BETWEEN_CALLS);
        }
        LAST_CALL_TIME.set(System.currentTimeMillis());
        System.out.println("Calling  "+NASDAQ_HIST_DATA_BASE+" for "+stockCode+" at "+LAST_CALL_TIME );

        String url = NASDAQ_HIST_DATA_BASE+"/"+exchngCode+"/"+stockCode+".json?API_KEY="+API_KEY;

        /*switch(SecType.valueOf(type)){
            case STOCK:
                url = url+stockCode+".json?API_KEY="+API_KEY;
                break;
            case INDEX:
                url = url+stockCode+".json?API_KEY="+API_KEY;
                break;
        }*/

        String rawdata = client.getHTTPData(url);

        List<DataPoint> op = new ArrayList<>();

        JsonObject rawJson = new JsonParser().parse(rawdata).getAsJsonObject().getAsJsonObject("dataset");
        JsonArray columnList = rawJson.getAsJsonArray("column_names");

        int dateIdx = -1;
        int openIdx = -1;
        int closeIdx = -1;
        int highIdx = -1;
        int lowIdx = -1;
        int volumeIdx = -1;

        for(int i=0;i<columnList.size();i++){
            if(columnList.get(i).getAsString().equals("Date")){
                dateIdx = i;
            }else if(columnList.get(i).getAsString().equals("Open")){
                openIdx = i;
            }else if(columnList.get(i).getAsString().equals("Close")){
                closeIdx = i;
            }else if(columnList.get(i).getAsString().equals("High")){
                highIdx = i;
            }else if(columnList.get(i).getAsString().equals("Low")){
                lowIdx = i;
            }else if(columnList.get(i).getAsString().equals("No. of Shares")){
                volumeIdx = i;
            }
        }
        JsonArray dataArr = rawJson.getAsJsonArray("data");

        for(JsonElement data : dataArr){
            try {
                JsonArray currData = (JsonArray) data;
                DataPoint dp = new NasdaqHistDataPoint(currData.get(dateIdx).getAsString(),
                        currData.get(openIdx).getAsFloat(),
                        currData.get(closeIdx).getAsFloat(),
                        currData.get(highIdx).getAsFloat(),
                        currData.get(lowIdx).getAsFloat(),
                        type.equals(SecType.STOCK) ? currData.get(volumeIdx).getAsInt() : -99
                );
                op.add(dp);
            }catch(Exception e){
                System.out.println(e);
            }
        }

        return op;

    }


    /* Works with Nasdaq sec list API

       public List<Security> getSecList() throws IOException {
        String zipFile = "BSESecList.zip";
        zipClient.getHTTPData("https://data.nasdaq.com/api/v3/databases/BSE/metadata?api_key="+API_KEY,zipFile);
        List<List<String>> records = fileUtil.readCsvFromZip(zipFile);

        Map<String,Security> secFromBSe = bseService.getSecMap();
        List<Security> op = new ArrayList<>();

        boolean header = true;
        for(List<String> strs : records){
            if(header){
                header = false;
            }else{
                Security bseDataSec = secFromBSe.get(strs.get(0).substring(3));
                Security sec = null;
                if(bseDataSec != null) {
                    if(strs.get(0).startsWith("BOM")){
                        sec = new Security(strs.get(0), bseDataSec.getId(), bseDataSec.getName(), SecType.STOCK);
                    }else{
                        sec = new Security(strs.get(0), bseDataSec.getId(), bseDataSec.getName(), SecType.INDEX);
                        File f = new File("src/main/resources/"+strs.get(0)+".csv");
                        if(GET_INDEX_CONSTITUENTS && f.exists() && !f.isDirectory()) {
                            List<List<String>> constituentSecs = fileUtil.getCsvRecords(f);
                            List<String> constituents = new ArrayList<>();
                            for(List<String> consSec : constituentSecs){
                                if(consSec.get(0).startsWith("Scrip")) continue;
                                constituents.add("BOM"+consSec.get(0));
                            }
                            Security allSec = new Security(strs.get(0)+" ALL", bseDataSec.getId()+" ALL", bseDataSec.getName()+" ALL", SecType.INDEX_ALL);
                            allSec.setConstituents(constituents);
                            op.add(allSec);
                        }
                    }
                }else{
                    if(strs.get(0).startsWith("BOM")) {
                        sec = new Security(strs.get(0), strs.get(1), strs.get(1),SecType.STOCK);
                    }else{
                        sec = new Security(strs.get(0), strs.get(1).replaceFirst("BSE ",""), strs.get(1),SecType.INDEX);
                        File f = new File("src/main/resources/"+strs.get(0)+".csv");
                        if(GET_INDEX_CONSTITUENTS && f.exists() && !f.isDirectory()) {
                            List<List<String>> constituentSecs = fileUtil.getCsvRecords(f);
                            List<String> constituents = new ArrayList<>();
                            for(List<String> consSec : constituentSecs){
                                if(consSec.get(0).startsWith("Scrip")) continue;
                                constituents.add("BOM"+consSec.get(0));
                            }
                            Security allSec = new Security(strs.get(0)+" ALL", strs.get(1).replaceFirst("BSE ","")+" ALL", strs.get(1)+" ALL", SecType.INDEX_ALL);
                            allSec.setConstituents(constituents);
                            op.add(allSec);
                        }
                    }
                }
                op.add(sec);
            }
        }
        new File(zipFile).delete();
        return op;
    }*/

}
