package com.kaway.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaway.beans.SecType;
import com.kaway.beans.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.kaway.main.KawayConstants.BSE_EXCHANGE;
import static com.kaway.main.KawayConstants.LSE_EXCHANGE;

@Service
public class LSEService {


    @Autowired
    HTTPClient client;

    private String EOD_Hist_Data_API_KEY = "64aaa015a80121.74386960";

    public List<Security> getSecList() throws IOException {

        List<Security> secList = new ArrayList<>();
        String url = "https://eodhistoricaldata.com/api/exchange-symbol-list/LSE?fmt=json&api_token=" + EOD_Hist_Data_API_KEY;

        String rawdata = client.getHTTPData(url);

        JsonArray arrData = new JsonParser().parse(rawdata).getAsJsonArray();
        for (JsonElement stock : arrData) {
            JsonObject currData = (JsonObject) stock;
            Security security = new Security(currData.get("Code").getAsString(), currData.get("Name").getAsString(), currData.get("Name").getAsString(), currData.get("Code").getAsString(), SecType.STOCK);
            secList.add(security);
        }

        secList.addAll(getLSEIndices());
        return secList;

    }

    //Todo Find better way to add indices
    private List<Security> getLSEIndices() throws FileNotFoundException {

        List<Security> list = new ArrayList<>();
        List<Security> allSecList = new ArrayList<>();
        list.add(new Security("^FTSE","^FTSE","ftse-100","FTSE 100", SecType.INDEX));
        list.add(new Security("^IXIC","COMP","ftse-aim-uk-50-index","FTSE AIM UK 50 Index", SecType.INDEX));
        list.add(new Security("^NDX","NDX","ftse-aim-100-index","FTSE AIM 100 Index", SecType.INDEX));

        for(Security sec : list){
            allSecList.add(getConstituents(sec.getName(),sec));
        }

        list.add(new Security("^NDX","NDX","FTSE 250","FTSE 250", SecType.INDEX));
        list.add(new Security("^IXIC","COMP","FTSE 350","FTSE 350", SecType.INDEX));
        list.add(new Security("^NDX","NDX","FTSE All-Share","FTSE All-Share", SecType.INDEX));
        list.add(new Security("^IXIC","COMP","FTSE AIM All-Share","FTSE AIM All-Share", SecType.INDEX));
        //list.add(new Security("^NDX","NDX","Nasdaq 100","100", SecType.INDEX));

        list.addAll(allSecList);
        return list;

    }

    private Security getConstituents(String idxName,Security sec) {
        int pageNo = 0;
        int retrievedCnt = 0;
        int total = 101;

        Set<String> constituents = new HashSet<>();

        while(pageNo<total) {
            try {
                URL url = new URL("https://api.londonstockexchange.com/api/v1/components/refresh");
                HttpClient client = HttpClient.newHttpClient();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.londonstockexchange.com/api/v1/components/refresh"))
                        .POST(HttpRequest.BodyPublishers.ofString("{\"path\":\"ftse-constituents\",\"parameters\":\"indexname%3D"+idxName+"%26tab%3Dtable%26tabId%3D1602cf04-c25b-4ea0-a9d6-64040d217877\",\"components\":[{\"componentId\":\"block_content%3Aafe540a2-2a0c-46af-8497-407dc4c7fd71\",\"parameters\":\"page="+pageNo+"&size=20&sort=description,asc\"}]}"))
                        .setHeader("accept", "*/*")
                        .setHeader("accept-language", "en-US,en;q=0.9")
                        .setHeader("cache-control", "no-cache")
                        .setHeader("content-type", "application/json")
                        //.setHeader("cookie", "F=d=7ThV1MI9vODnSdDReVjIFGRHkO1ASSGI0UQLMMBLDm.5Tu4-; PH=l=en-IN; Y=v=1&n=0e6q35pekqga6&l=0hd01.1706010j8/o&p=m2mvvin00000000&r=o7&intl=in; B=97k1rg5gbea6q&b=4&d=nW7xH6dtYFrr0fb3zsRc&s=6r&i=lNXuquCoKh2be0PBrthy; tbla_id=cc419f2b-4268-4c03-b666-c249b8cfc65e-tuct7b0ae6c; gam_id=y-ujNf6dpG2uL5eJ1YRCOVchmN2Qytx4DOzPj7Q5vg609MzJNHzA---A; ucs=tr=1688403197000; OTH=v=2&s=2&d=eyJraWQiOiIwMTY0MGY5MDNhMjRlMWMxZjA5N2ViZGEyZDA5YjE5NmM5ZGUzZWQ5IiwiYWxnIjoiUlMyNTYifQ.eyJjdSI6eyJndWlkIjoiU05PRFZaQUtDN1JGQkJXUEtNQ05DQUhUNUkiLCJwZXJzaXN0ZW50Ijp0cnVlLCJzaWQiOiJYdlF5Nld1ZXJkZ0UifX0.bDhb2ztkJMpxqusFemMOkARq598IorFJizcHv2ct0xb2WSjN_idTp7L6KS_LyQ6IIQ3bUGb864ky77tDRWe5LYLOPoLZmTjoc-NvcT6II6Gy_GRAKUG_L5lKWuVcDXFL-fuKEgEEk2wm8fMGUe4CmNmt2nhJUKx2Tra72FtLQ7Q; T=af=JnRzPTE2ODgzMTY3OTgmcHM9SVlabTNyU0lhMHE1R1hSOG9lNnBrQS0t&d=bnMBeWFob28BZwFTTk9EVlpBS0M3UkZCQldQS01DTkNBSFQ1SQFhYwFBR0xkWmtPNQFhbAFhcm5hYi5iaGFnYWJhdGkBc2MBZGVza3RvcF93ZWIBZnMBRnRpSzlyOWd1R2Z3AXp6AUVnU2xrQmtRSQFhAVFBRQFsYXQBb3RGTmtCAW51ATA-&kt=EAAPyi5O5LDzy3Z9uKu41tthQ--~I&ku=FAAMYv.GvIQLtMEhFrYDQmFd4A7qM_BkSY0Op9FKNdoS.MZqxkC7RK49WHzkuFm7BZLNC_.6f65xZfqhUO3JjlZvH1u6F4wjfqAnaQuq7L.4pR5_yI8FHELgSEtn7dgdNp_24oWw1QbqC6NYPPwak4FdbNTItpqRERXFgxrwzNP9Bk-~E; GUC=AQEACAJkpStk1EIcngRR&s=AQAAAE8G8WXK&g=ZKPjkQ; A1=d=AQABBNoot2ACEKQu2sfzBbeu8trCK4E70JMFEgEACAIrpWTUZFkZyyMA_eMBAAcI2ii3YIE70JMID8LhGQly-uNloFX5qdvwIQkBBwoB1w&S=AQAAAq52IR8mN79pOHw5UundnNw; A3=d=AQABBNoot2ACEKQu2sfzBbeu8trCK4E70JMFEgEACAIrpWTUZFkZyyMA_eMBAAcI2ii3YIE70JMID8LhGQly-uNloFX5qdvwIQkBBwoB1w&S=AQAAAq52IR8mN79pOHw5UundnNw; A1S=d=AQABBNoot2ACEKQu2sfzBbeu8trCK4E70JMFEgEACAIrpWTUZFkZyyMA_eMBAAcI2ii3YIE70JMID8LhGQly-uNloFX5qdvwIQkBBwoB1w&S=AQAAAq52IR8mN79pOHw5UundnNw&j=WORLD; cmp=t=1688886122&j=0&u=1---; PRF=t%3D%255EIXIC%252B%255ENDX%252B%255EBSESN%252B%255ENSEI%252BINDEX%252BNTPC.NS%252BNTPC.BO%252BNSE.V%252BBAJAJFINSV.BO%252BBAJAJFINSV.NS%252BNIFTYMIDCAP150.NS%252B%255ECNX100%252BBAJAJELEC.NS%252BICICIINFRA.NS%252B%255ENFTS.REGA%26newChartbetateaser%3D1")
                        .setHeader("dnt", "1")
                        .setHeader("pragma", "no-cache")
                        .setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                JsonObject res = new JsonParser().parse(response.body())
                        .getAsJsonArray()
                        .get(0).getAsJsonObject()
                        .getAsJsonArray("content")
                        .get(0).getAsJsonObject().getAsJsonObject("value");

                total = (res.get("totalElements").getAsInt())/20+((res.get("totalElements").getAsInt() % 20 == 0) ? 0 : 1);

                JsonArray dataArray = res.getAsJsonArray("content");


                for (int i = 0; i < dataArray.size(); i++) {
                    JsonObject data = dataArray.get(i).getAsJsonObject();
                    constituents.add(data.get("tidm").getAsString());
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("{\"path\":\"ftse-constituents\",\"parameters\":\"indexname%3D"+idxName+"%26tab%3Dtable%26page%3D1%26tabId%3D1602cf04-c25b-4ea0-a9d6-64040d217877\",\"components\":[{\"componentId\":\"block_content%3Aafe540a2-2a0c-46af-8497-407dc4c7fd71\",\"parameters\":\"page="+pageNo+"+&size=100&sort=percentualchange,desc\"}]}");
            }
            retrievedCnt =retrievedCnt+20;
            pageNo = pageNo+1;

        }
        Security allSec = new Security(sec.getCode()+"_ALL", sec.getId()+"_ALL", sec.getName()+" ALL",sec.getDisplayName()+" Constituents", SecType.INDEX_ALL);
        List<String> consList = new ArrayList<String>();
        consList.addAll(constituents);
        allSec.setExchange(LSE_EXCHANGE);
        allSec.setConstituents(consList);

        //System.out.println(op);
        return allSec;
    }

}
