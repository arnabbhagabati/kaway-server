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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static com.kaway.main.KawayConstants.GET_INDEX_CONSTITUENTS;

@Service
public class NSEService {


    @Autowired
    HTTPClientForCSV client;

    @Autowired
    FileUtil fileUtil;

    public List<Security> getSecList() throws FileNotFoundException {
        List<Security> list = new ArrayList<>();
        String url = "https://archives.nseindia.com/content/equities/EQUITY_L.csv";
        List<String> rawdata = client.getHTTPData(url);

        int cnt = 0;
        for(String s : rawdata){
            String[] fields = s.split(",");
            if(cnt>0){
                list.add(new Security(fields[0],fields[0],fields[1], fields[0],SecType.STOCK));
            }
            cnt++;
        }

        list.addAll(getIndices());

        return list;

    }

    private List<Security> getIndices() throws FileNotFoundException {
        List<Security> list = new ArrayList<>();
        list.add(new Security("^NSEI","NIFTY 50","NIFTY 50","NIFTY 50", SecType.INDEX));

        list.add(new Security("^NSMIDCP","NIFTY NEXT 50","NIFTY NEXT 50","NIFTY NEXT 50", SecType.INDEX));
        list.add(new Security("^CNX100","NIFTY 100","NIFTY 100","NIFTY 100", SecType.INDEX));
        list.add(new Security("^CNX200","NIFTY 200","NIFTY 200","NIFTY 200", SecType.INDEX));
        list.add(new Security("^CRSLDX","NIFTY 500","NIFTY 500", "NIFTY 500",SecType.INDEX));
        list.add(new Security("^NSEMDCP50","NIFTY MIDCAP 50","NIFTY MIDCAP 50", "NIFTY MIDCAP 50",SecType.INDEX));
        list.add(new Security("NIFTY_MIDCAP_100.NS","NIFTY_MIDCAP_100.NS","NIFTY MIDCAP 100", "NIFTY MIDCAP 100",SecType.INDEX));
        list.add(new Security("^CNXSC","NIFTY SMALLCAP 100","NIFTY SMALLCAP 100","NIFTY SMALLCAP 100", SecType.INDEX));
        list.add(new Security("NIFTYMIDCAP150.NS","NIFTY MIDCAP 150","NIFTY MIDCAP 150","NIFTY MIDCAP 150", SecType.INDEX));
        list.add(new Security("NIFTYSMLCAP50.NS","NIFTY SMALLCAP 50","NIFTY SMALLCAP 50", "NIFTY SMALLCAP 50",SecType.INDEX));
        list.add(new Security("NIFTYSMLCAP250.NS","NIFTY SMALLCAP 250","NIFTY SMALLCAP 250", "NIFTY SMALLCAP 250",SecType.INDEX));
        list.add(new Security("NIFTYMIDSML400.NS","NIFTY MIDSMALLCAP 400","NIFTY MIDSMALLCAP 400", "NIFTY MIDSMALLCAP 400", SecType.INDEX));
        list.add(new Security("NIFTY500_MULTICAP.NS","NIFTY500 MULTICAP","NIFTY500 MULTICAP", "NIFTY500 MULTICAP",SecType.INDEX));
        list.add(new Security("NIFTY_LARGEMID250.NS","NIFTY LARGEMIDCAP 250","NIFTY LARGEMIDCAP 250","NIFTY LARGEMIDCAP 250", SecType.INDEX));
        list.add(new Security("NIFTY_MID_SELECT.NS","NIFTY MIDCAP SELECT","NIFTY MIDCAP SELECT", "NIFTY MIDCAP SELECT",SecType.INDEX));

        list.add(new Security("NIFTY_TOTAL_MKT.NS","NIFTY TOTAL MARKET","NIFTY TOTAL MARKET","NIFTY TOTAL MARKET", SecType.INDEX));
        list.add(new Security("NIFTY_MICROCAP250.NS","NIFTY MICROCAP 250","NIFTY MICROCAP 250","NIFTY MICROCAP 250", SecType.INDEX));

        /*list.add(new Security("^NSEI","^NSEI","NIFTY 50", SecType.INDEX));
        list.add(new Security("^NSEI","^NSEI","NIFTY 50", SecType.INDEX));
        list.add(new Security("^NSEI","^NSEI","NIFTY 50", SecType.INDEX));
        list.add(new Security("^NSEI","^NSEI","NIFTY 50", SecType.INDEX));
        list.add(new Security("^NSEI","^NSEI","NIFTY 50", SecType.INDEX));
        list.add(new Security("^NSEI","^NSEI","NIFTY 50", SecType.INDEX));
        list.add(new Security("^NSEI","^NSEI","NIFTY 50", SecType.INDEX));
        list.add(new Security("^NSEI","^NSEI","NIFTY 50", SecType.INDEX));
        list.add(new Security("^NSEI","^NSEI","NIFTY 50", SecType.INDEX));
        list.add(new Security("^NSEI","^NSEI","NIFTY 50", SecType.INDEX));
        list.add(new Security("^NSEI","^NSEI","NIFTY 50", SecType.INDEX));
        list.add(new Security("^NSEI","^NSEI","NIFTY 50", SecType.INDEX));*/
        addAllIndices(list);
        return list;
    }


    private List<Security> addAllIndices(List<Security> secs) throws FileNotFoundException {

        System.out.println("NSEService addAllIndices start");
        List<Security> allSecs = new ArrayList<>();
        for(Security sec : secs){
            String secId = sec.getId();
            System.out.println("NSEService addAllIndices secId "+secId);
            File f = new File("src/main/resources/"+secId+".csv");
            System.out.println("NSEService addAllIndices secId "+secId+" f.exists() "+f.exists());
            if(GET_INDEX_CONSTITUENTS && f.exists() && !f.isDirectory()) {
                System.out.println("NSEService addAllIndices secId "+secId+" found "+f.getName());
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
                System.out.println("NSEService addAllIndices secId "+secId+" constituents.size() "+constituents.size());
                allSec.setConstituents(constituents);
                allSecs.add(allSec);
            }
        }

        secs.addAll(allSecs);
        return secs;
    }

}
