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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    public List<Security> getSecList() throws IOException, InterruptedException {
        List<Security> secList = new ArrayList<>();
        //String url = "https://api.nasdaq.com/api/screener/stocks?tableonly=true&limit=25&offset=0&download=true";

        
        URL url = new URL("https://api.nasdaq.com/api/screener/stocks?tableonly=true&limit=25&offset=0&download=true");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("GET");

        httpConn.setRequestProperty("accept", "application/json, text/plain, */*");
        httpConn.setRequestProperty("accept-language", "en-US,en;q=0.9");
        httpConn.setRequestProperty("cache-control", "no-cache");
        httpConn.setRequestProperty("dnt", "1");

        httpConn.setRequestProperty("pragma", "no-cache");
        //httpConn.setRequestProperty("referer", "https://www.nasdaq.com/");

        httpConn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        Scanner s = new Scanner(responseStream).useDelimiter("\\A");
        String response = s.hasNext() ? s.next() : "";
        //System.out.println(response);

        String rawdata = response;
        JsonArray rawJson = new JsonParser().parse(rawdata).getAsJsonObject().getAsJsonObject("data").getAsJsonArray("rows");
        for(JsonElement data : rawJson){
            JsonObject currData = (JsonObject)data;
            Security security = new Security(currData.get("symbol").getAsString(), currData.get("symbol").getAsString(), currData.get("name").getAsString().replaceFirst(" Common Stock",""), currData.get("symbol").getAsString(), SecType.STOCK);
            secList.add(security);
        }
        secList.addAll(getNasdaQIndicesList());
        return secList;

    }

    private List<Security> getNasdaQIndicesList() throws FileNotFoundException {
        List<Security> list = new ArrayList<>();
        list.add(new Security("^IXIC","COMP","NASDAQ Composite","Composite", SecType.INDEX));
        list.add(new Security("^NDX","NDX","Nasdaq 100","100", SecType.INDEX));
        list.add(new Security("^NQGI","NQGI","Nasdaq Global Equity Index","Global Equity Index", SecType.INDEX));

        list.add(new Security("^ABAQ","ABAQ","ABA Community Bank NASDAQ Index","Community Bank NASDAQ Index", SecType.INDEX));
        list.add(new Security("^BXN","BXN","CBOE NASDAQ-100 BuyWrite Index","CBOE NASDAQ-100 BuyWrite Index", SecType.INDEX));
        list.add(new Security("^TRAN","TRAN","Dow Transportation","Dow Transportation", SecType.INDEX));


        /*list.add(new Security("^NQGI","NQGI","NQGI","Nasdaq Global Equity Index", SecType.INDEX));
        list.add(new Security("^NQGI","NQGI","NQGI","Nasdaq Global Equity Index", SecType.INDEX));

        list.add(new Security("^NQGI","NQGI","NQGI","Nasdaq Global Equity Index", SecType.INDEX));
        list.add(new Security("^NQGI","NQGI","NQGI","Nasdaq Global Equity Index", SecType.INDEX));
        list.add(new Security("^NQGI","NQGI","NQGI","Nasdaq Global Equity Index", SecType.INDEX));
        list.add(new Security("^NQGI","NQGI","NQGI","Nasdaq Global Equity Index", SecType.INDEX));
        list.add(new Security("^NQGI","NQGI","NQGI","Nasdaq Global Equity Index", SecType.INDEX));*/

        addAllIndices(list);

        return list;
    }


    private List<Security> addAllIndices(List<Security> secs) throws FileNotFoundException {

        List<Security> allSecs = new ArrayList<>();
        for(Security sec : secs){
            String secId = sec.getId();
            File f = new File("src/main/resources/"+secId+".csv");
            if(GET_INDEX_CONSTITUENTS && f.exists() && !f.isDirectory()) {
                List<List<String>> constituentSecs = fileUtil.getCsvRecords(f);
                List<String> constituents = new ArrayList<>();
                int cnt = 0;
                for(List<String> consSec : constituentSecs){
                    if(cnt==0){
                        cnt++;
                    }else{
                        constituents.add(consSec.get(0));
                    }

                }
                Security allSec = new Security(sec.getCode()+"_ALL", sec.getId()+"_ALL", sec.getName()+" ALL",sec.getDisplayName()+" Constituents", SecType.INDEX_ALL);
                allSec.setConstituents(constituents);
                allSecs.add(allSec);
            }
        }

        secs.addAll(allSecs);
        return secs;
    }


    /* Result is too huge
        Maybe add as a separate exchange like Nasdaq Indices later?
        check https://indexes.nasdaqomx.com/index/directory
     */
    private List<Security> getIndicesList(String region,String exchange) throws IOException, InterruptedException {
        List<Security> secList = new ArrayList<>();
        int offset = 0;
        int retrievedCnt = 0;
        int total = 101;

        JsonArray op = new JsonArray();

        while(retrievedCnt<total) {
            try {
                URL url = new URL("https://query1.finance.yahoo.com/v1/finance/screener?crumb=qi1yBvvC.cQ&lang=en-US®ion=US&formatted=true&corsDomain=finance.yahoo.com");
                HttpClient client = HttpClient.newHttpClient();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://query1.finance.yahoo.com/v1/finance/screener?crumb=qi1yBvvC.cQ&lang=en-US&region=US&formatted=true&corsDomain=finance.yahoo.com"))
                        .POST(HttpRequest.BodyPublishers.ofString("{\"size\":100,\"offset\":"+offset+",\"sortField\":\"percentchange\",\"sortType\":\"DESC\",\"quoteType\":\"INDEX\",\"topOperator\":\"AND\",\"query\":{\"operator\":\"AND\",\"operands\":[{\"operator\":\"or\",\"operands\":[{\"operator\":\"EQ\",\"operands\":[\"region\",\""+region+"\"]}]},{\"operator\":\"or\",\"operands\":[{\"operator\":\"EQ\",\"operands\":[\"exchange\",\""+exchange+"\"]}]}]},\"userId\":\"SNODVZAKC7RFBBWPKMCNCAHT5I\",\"userIdType\":\"guid\"}"))
                        .setHeader("accept", "*/*")
                        .setHeader("accept-language", "en-US,en;q=0.9")
                        .setHeader("cache-control", "no-cache")
                        .setHeader("content-type", "application/json")
                        .setHeader("cookie", "F=d=7ThV1MI9vODnSdDReVjIFGRHkO1ASSGI0UQLMMBLDm.5Tu4-; PH=l=en-IN; Y=v=1&n=0e6q35pekqga6&l=0hd01.1706010j8/o&p=m2mvvin00000000&r=o7&intl=in; B=97k1rg5gbea6q&b=4&d=nW7xH6dtYFrr0fb3zsRc&s=6r&i=lNXuquCoKh2be0PBrthy; tbla_id=cc419f2b-4268-4c03-b666-c249b8cfc65e-tuct7b0ae6c; gam_id=y-ujNf6dpG2uL5eJ1YRCOVchmN2Qytx4DOzPj7Q5vg609MzJNHzA---A; ucs=tr=1688403197000; OTH=v=2&s=2&d=eyJraWQiOiIwMTY0MGY5MDNhMjRlMWMxZjA5N2ViZGEyZDA5YjE5NmM5ZGUzZWQ5IiwiYWxnIjoiUlMyNTYifQ.eyJjdSI6eyJndWlkIjoiU05PRFZaQUtDN1JGQkJXUEtNQ05DQUhUNUkiLCJwZXJzaXN0ZW50Ijp0cnVlLCJzaWQiOiJYdlF5Nld1ZXJkZ0UifX0.bDhb2ztkJMpxqusFemMOkARq598IorFJizcHv2ct0xb2WSjN_idTp7L6KS_LyQ6IIQ3bUGb864ky77tDRWe5LYLOPoLZmTjoc-NvcT6II6Gy_GRAKUG_L5lKWuVcDXFL-fuKEgEEk2wm8fMGUe4CmNmt2nhJUKx2Tra72FtLQ7Q; T=af=JnRzPTE2ODgzMTY3OTgmcHM9SVlabTNyU0lhMHE1R1hSOG9lNnBrQS0t&d=bnMBeWFob28BZwFTTk9EVlpBS0M3UkZCQldQS01DTkNBSFQ1SQFhYwFBR0xkWmtPNQFhbAFhcm5hYi5iaGFnYWJhdGkBc2MBZGVza3RvcF93ZWIBZnMBRnRpSzlyOWd1R2Z3AXp6AUVnU2xrQmtRSQFhAVFBRQFsYXQBb3RGTmtCAW51ATA-&kt=EAAPyi5O5LDzy3Z9uKu41tthQ--~I&ku=FAAMYv.GvIQLtMEhFrYDQmFd4A7qM_BkSY0Op9FKNdoS.MZqxkC7RK49WHzkuFm7BZLNC_.6f65xZfqhUO3JjlZvH1u6F4wjfqAnaQuq7L.4pR5_yI8FHELgSEtn7dgdNp_24oWw1QbqC6NYPPwak4FdbNTItpqRERXFgxrwzNP9Bk-~E; GUC=AQEACAJkpStk1EIcngRR&s=AQAAAE8G8WXK&g=ZKPjkQ; A1=d=AQABBNoot2ACEKQu2sfzBbeu8trCK4E70JMFEgEACAIrpWTUZFkZyyMA_eMBAAcI2ii3YIE70JMID8LhGQly-uNloFX5qdvwIQkBBwoB1w&S=AQAAAq52IR8mN79pOHw5UundnNw; A3=d=AQABBNoot2ACEKQu2sfzBbeu8trCK4E70JMFEgEACAIrpWTUZFkZyyMA_eMBAAcI2ii3YIE70JMID8LhGQly-uNloFX5qdvwIQkBBwoB1w&S=AQAAAq52IR8mN79pOHw5UundnNw; A1S=d=AQABBNoot2ACEKQu2sfzBbeu8trCK4E70JMFEgEACAIrpWTUZFkZyyMA_eMBAAcI2ii3YIE70JMID8LhGQly-uNloFX5qdvwIQkBBwoB1w&S=AQAAAq52IR8mN79pOHw5UundnNw&j=WORLD; cmp=t=1688886122&j=0&u=1---; PRF=t%3D%255EIXIC%252B%255ENDX%252B%255EBSESN%252B%255ENSEI%252BINDEX%252BNTPC.NS%252BNTPC.BO%252BNSE.V%252BBAJAJFINSV.BO%252BBAJAJFINSV.NS%252BNIFTYMIDCAP150.NS%252B%255ECNX100%252BBAJAJELEC.NS%252BICICIINFRA.NS%252B%255ENFTS.REGA%26newChartbetateaser%3D1")
                        .setHeader("dnt", "1")
                        .setHeader("pragma", "no-cache")
                        .setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                JsonObject res = new JsonParser().parse(response.body())
                        .getAsJsonObject()
                        .getAsJsonObject("finance")
                        .getAsJsonArray("result")
                        .get(0).getAsJsonObject();
                total = res.get("total").getAsInt();

                JsonArray dataArray = res
                        .getAsJsonArray("quotes");

                for (int i = 0; i < dataArray.size(); i++) {
                    JsonObject data = dataArray.get(i).getAsJsonObject();
                    JsonObject idx = new JsonObject();


                    //JsonObject regularMarketVolume = data.get("regularMarketVolume").getAsJsonObject();
                    idx.addProperty("fullExchangeName",data.get("fullExchangeName").getAsString());
                    idx.addProperty("symbol",data.get("symbol").getAsString());
                    idx.addProperty("exchange",data.get("exchange").getAsString());
                    idx.addProperty("shortName",data.get("shortName").getAsString());

                    if(data.has("regularMarketVolume")){
                        JsonObject regularMarketVolume = data.get("regularMarketVolume").getAsJsonObject();
                        idx.add("regularMarketVolume",regularMarketVolume);
                    }

                    //idx.addProperty("regularMarketVolume",regularMarketVolume);

                    op.add(idx);

                    Security sec = new Security(data.get("symbol").getAsString(),data.get("symbol").getAsString(),data.get("shortName").getAsString(),data.get("shortName").getAsString(),SecType.INDEX);
                    secList.add(sec);

                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Errrneous result is for "+"{\"size\":100,\"offset\":"+offset+",\"sortField\":\"percentchange\",\"sortType\":\"DESC\",\"quoteType\":\"INDEX\",\"topOperator\":\"AND\",\"query\":{\"operator\":\"AND\",\"operands\":[{\"operator\":\"or\",\"operands\":[{\"operator\":\"EQ\",\"operands\":[\"region\",\""+region+"\"]}]},{\"operator\":\"or\",\"operands\":[{\"operator\":\"EQ\",\"operands\":[\"exchange\",\""+exchange+"\"]}]}]},\"userId\":\"SNODVZAKC7RFBBWPKMCNCAHT5I\",\"userIdType\":\"guid\"}");
            }
            retrievedCnt =retrievedCnt+100;
            offset = offset+100;

        }
        //System.out.println(op);
        return secList;
    }

}
