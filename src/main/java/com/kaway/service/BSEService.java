package com.kaway.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaway.beans.SecType;
import com.kaway.beans.Security;
import com.kaway.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import java.io.IOException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.URL;
import java.util.Scanner;

import static com.kaway.main.KawayConstants.GET_INDEX_CONSTITUENTS;

@Service
public class BSEService {

    @Autowired
    HTTPClient client;

    @Autowired
    FileUtil fileUtil;

    /*public Map<String,Security> getSecMap(){
        Map<String,Security> secMap = new HashMap<>();
        String url = "https://api.bseindia.com/BseIndiaAPI/api/ListofScripData/w?Group=&Scripcode=&industry=&segment=&status=Active";
        String rawdata = client.getHTTPData(url);

        JsonArray rawJson = new JsonParser().parse(rawdata).getAsJsonArray();

        for(JsonElement data : rawJson){
            JsonObject currData = (JsonObject) data;
            if((currData.get("Segment").getAsString().equals("Equity") /*|| currData.get("Segment").getAsString().equals("MF")//)
                    &&(currData.get("Mktcap") != null && !currData.get("Mktcap").getAsString().isEmpty()) ) {
                Security security = new Security(currData.get("scrip_id").getAsString(), currData.get("SCRIP_CD").getAsString(), currData.get("Scrip_Name").getAsString(), currData.get("scrip_id").getAsString(), SecType.STOCK);
                secMap.put(currData.get("SCRIP_CD").getAsString(), (security));
            }
        }

        return secMap;

    }*/

    public List<Security> getSecList() throws IOException, InterruptedException {
        Map<String,Security> secMap = new HashMap<>();
        List<Security> secList = new ArrayList<>();
        String url = "https://api.bseindia.com/BseIndiaAPI/api/ListofScripData/w?Group=&Scripcode=&industry=&segment=&status=Active";
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.bseindia.com/BseIndiaAPI/api/ListofScripData/w?Group=&Scripcode=&industry=&segment=&status=Active"))
                .GET()
                .setHeader("accept", "application/json, text/plain, */*")
                .setHeader("accept-language", "en-US,en;q=0.9")
                .setHeader("cache-control", "no-cache")
                .setHeader("origin", "https://www.bseindia.com")
                .setHeader("pragma", "no-cache")
                .setHeader("referer", "https://www.bseindia.com/")
                .setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonArray rawJson = new JsonParser().parse(response.body()).getAsJsonArray();

        for(JsonElement data : rawJson){
            JsonObject currData = (JsonObject)data;
            if((currData.get("Segment").getAsString().equals("Equity") /*|| currData.get("Segment").getAsString().equals("MF")*/)
                    &&(currData.get("Mktcap") != null && !currData.get("Mktcap").getAsString().isEmpty()) ) {
                Security security = new Security(currData.get("scrip_id").getAsString(), currData.get("SCRIP_CD").getAsString(), currData.get("Scrip_Name").getAsString(), currData.get("scrip_id").getAsString(), SecType.STOCK);
                secList.add(security);
                secMap.put(security.getCode(), security);
            }
        }
        secList.addAll(getIndicesList(secMap));
        return secList;

    }


    private List<Security> getIndicesList(Map<String,Security> secMap) throws IOException, InterruptedException {
        List<Security> secList = new ArrayList<>();

        try {
            URL url = new URL("https://query1.finance.yahoo.com/v1/finance/screener?crumb=qi1yBvvC.cQ&lang=en-US®ion=US&formatted=true&corsDomain=finance.yahoo.com");
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://query1.finance.yahoo.com/v1/finance/screener?crumb=qi1yBvvC.cQ&lang=en-US&region=US&formatted=true&corsDomain=finance.yahoo.com"))
                    .POST(BodyPublishers.ofString("{\"size\":100,\"offset\":0,\"sortField\":\"percentchange\",\"sortType\":\"DESC\",\"quoteType\":\"INDEX\",\"topOperator\":\"AND\",\"query\":{\"operator\":\"AND\",\"operands\":[{\"operator\":\"or\",\"operands\":[{\"operator\":\"EQ\",\"operands\":[\"region\",\"in\"]}]},{\"operator\":\"or\",\"operands\":[{\"operator\":\"EQ\",\"operands\":[\"exchange\",\"BSE\"]}]}]},\"userId\":\"SNODVZAKC7RFBBWPKMCNCAHT5I\",\"userIdType\":\"guid\"}"))
                    .setHeader("accept", "*/*")
                    .setHeader("accept-language", "en-US,en;q=0.9")
                    .setHeader("cache-control", "no-cache")
                    .setHeader("content-type", "application/json")
                    .setHeader("cookie", "F=d=7ThV1MI9vODnSdDReVjIFGRHkO1ASSGI0UQLMMBLDm.5Tu4-; PH=l=en-IN; Y=v=1&n=0e6q35pekqga6&l=0hd01.1706010j8/o&p=m2mvvin00000000&r=o7&intl=in; B=97k1rg5gbea6q&b=4&d=nW7xH6dtYFrr0fb3zsRc&s=6r&i=lNXuquCoKh2be0PBrthy; tbla_id=cc419f2b-4268-4c03-b666-c249b8cfc65e-tuct7b0ae6c; gam_id=y-ujNf6dpG2uL5eJ1YRCOVchmN2Qytx4DOzPj7Q5vg609MzJNHzA---A; ucs=tr=1688403197000; OTH=v=2&s=2&d=eyJraWQiOiIwMTY0MGY5MDNhMjRlMWMxZjA5N2ViZGEyZDA5YjE5NmM5ZGUzZWQ5IiwiYWxnIjoiUlMyNTYifQ.eyJjdSI6eyJndWlkIjoiU05PRFZaQUtDN1JGQkJXUEtNQ05DQUhUNUkiLCJwZXJzaXN0ZW50Ijp0cnVlLCJzaWQiOiJYdlF5Nld1ZXJkZ0UifX0.bDhb2ztkJMpxqusFemMOkARq598IorFJizcHv2ct0xb2WSjN_idTp7L6KS_LyQ6IIQ3bUGb864ky77tDRWe5LYLOPoLZmTjoc-NvcT6II6Gy_GRAKUG_L5lKWuVcDXFL-fuKEgEEk2wm8fMGUe4CmNmt2nhJUKx2Tra72FtLQ7Q; T=af=JnRzPTE2ODgzMTY3OTgmcHM9SVlabTNyU0lhMHE1R1hSOG9lNnBrQS0t&d=bnMBeWFob28BZwFTTk9EVlpBS0M3UkZCQldQS01DTkNBSFQ1SQFhYwFBR0xkWmtPNQFhbAFhcm5hYi5iaGFnYWJhdGkBc2MBZGVza3RvcF93ZWIBZnMBRnRpSzlyOWd1R2Z3AXp6AUVnU2xrQmtRSQFhAVFBRQFsYXQBb3RGTmtCAW51ATA-&kt=EAAPyi5O5LDzy3Z9uKu41tthQ--~I&ku=FAAMYv.GvIQLtMEhFrYDQmFd4A7qM_BkSY0Op9FKNdoS.MZqxkC7RK49WHzkuFm7BZLNC_.6f65xZfqhUO3JjlZvH1u6F4wjfqAnaQuq7L.4pR5_yI8FHELgSEtn7dgdNp_24oWw1QbqC6NYPPwak4FdbNTItpqRERXFgxrwzNP9Bk-~E; GUC=AQEACAJkpStk1EIcngRR&s=AQAAAE8G8WXK&g=ZKPjkQ; A1=d=AQABBNoot2ACEKQu2sfzBbeu8trCK4E70JMFEgEACAIrpWTUZFkZyyMA_eMBAAcI2ii3YIE70JMID8LhGQly-uNloFX5qdvwIQkBBwoB1w&S=AQAAAq52IR8mN79pOHw5UundnNw; A3=d=AQABBNoot2ACEKQu2sfzBbeu8trCK4E70JMFEgEACAIrpWTUZFkZyyMA_eMBAAcI2ii3YIE70JMID8LhGQly-uNloFX5qdvwIQkBBwoB1w&S=AQAAAq52IR8mN79pOHw5UundnNw; A1S=d=AQABBNoot2ACEKQu2sfzBbeu8trCK4E70JMFEgEACAIrpWTUZFkZyyMA_eMBAAcI2ii3YIE70JMID8LhGQly-uNloFX5qdvwIQkBBwoB1w&S=AQAAAq52IR8mN79pOHw5UundnNw&j=WORLD; cmp=t=1688624236&j=0&u=1---; PRF=t%3DINDEX%252B%255EBSESN%252B%255ENSEI%252BNTPC.NS%252BNTPC.BO%252BNSE.V%252BBAJAJFINSV.BO%252BBAJAJFINSV.NS%252BNIFTYMIDCAP150.NS%252B%255ECNX100%252BBAJAJELEC.NS%252BICICIINFRA.NS%252B%255ENFTS.REGA%252BBPCL.NS%26newChartbetateaser%3D1")
                    .setHeader("dnt", "1")
                    .setHeader("pragma", "no-cache")
                    .setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonArray dataArray = new JsonParser().parse(response.body())
                                            .getAsJsonObject()
                                            .getAsJsonObject("finance")
                                            .getAsJsonArray("result")
                                            .get(0).getAsJsonObject()
                                            .getAsJsonArray("quotes");

            for(int i=0;i<dataArray.size();i++){
                JsonObject data = dataArray.get(i).getAsJsonObject();
                Security security = new Security( data.get("symbol").getAsString(),
                                                    data.get("symbol").getAsString(),
                                                    data.get("shortName").getAsString(),
                                                    getFormattedIndexName(data.get("shortName").getAsString()),
                                                    SecType.INDEX);
                secList.add(security);

                File f = new File("src/main/resources/"+security.getId()+".csv");
                if(GET_INDEX_CONSTITUENTS && f.exists() && !f.isDirectory()) {
                    List<List<String>> constituentSecs = fileUtil.getCsvRecords(f);
                    List<String> constituents = new ArrayList<>();
                    for(List<String> consSec : constituentSecs){
                        if(consSec.get(0).startsWith("Ticker")) continue;
                        if(secMap.containsKey(consSec.get(0))) constituents.add(secMap.get(consSec.get(0)).getId());
                    }
                    Security allSec = new Security(security.getCode()+"_ALL",
                                                    security.getCode()+"_ALL",
                                                    security.getName()+" ALL",
                                            security.getDisplayName()+" Constituents",SecType.INDEX_ALL);
                    allSec.setConstituents(constituents);
                    secList.add(allSec);
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return secList;
    }


    private String getFormattedIndexName(String originalString){
        String newString = "";
        originalString = originalString.replaceFirst("S&P BSE ","");
        String searchString = "INDEX";
        String replacementString = "";

        String lowerCaseOriginal = originalString.toLowerCase().trim();
        String lowerCaseSearch = searchString.toLowerCase();

        if(lowerCaseOriginal.endsWith(lowerCaseSearch)){
            int lastIndex = lowerCaseOriginal.lastIndexOf(lowerCaseSearch);

            if (lastIndex != -1) {
                newString = originalString.substring(0, lastIndex) +
                        replacementString +
                        originalString.substring(lastIndex + searchString.length());
            }
        }else{
            newString = originalString;
        }

        return newString;

    }
}
