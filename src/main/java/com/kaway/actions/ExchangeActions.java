package com.kaway.actions;

import com.kaway.beans.Security;
import com.kaway.beans.DataPoint;
import com.kaway.db.BaseDAO;
import com.kaway.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.kaway.main.KawayConstants.*;

@Service
public class ExchangeActions {

    @Autowired
    BaseDAO baseDao;

    @Autowired
    NasdaqService nasdaqService;

    @Autowired
    NSEDataService nseDataService;

    @Autowired
    MboumDataService mboumDataService;

    @Autowired
    BSEService bseService;

    @Autowired
    NSEService nseService;

    @Autowired
    NYSEDataService nyseDataService;

    @Autowired
    LSEService lseService;

    public List<DataPoint> getSecDataFromDb(String exchange,String secId,String type) throws IOException, ExecutionException, InterruptedException {
        Map<String, Object> data = baseDao.getDailySecData(exchange,secId);
        String today = new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(new Date());
        List<DataPoint> freshData = null;
        if(data!=null && data.containsKey(today)){
            freshData = (List<DataPoint>) data.get(today);
        }
        return freshData;
    }

    public List<DataPoint> getSecDataFromSource(String exchange,String secId,String type) throws IOException, ExecutionException, InterruptedException {
        List<DataPoint> sortedData = null;
        List<DataPoint> freshData = null;
        String today = new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(new Date());


            switch(exchange){
                case BSE_EXCHANGE:
                    freshData = mboumDataService.getHistData(exchange,secId,type);
                    break;
                case NSE_EXCHANGE:
                    freshData = mboumDataService.getHistData(exchange,secId,type);
                    break;
                case NASDAQ_EXCHANGE:
                    freshData = mboumDataService.getHistData(exchange,secId,type);
                    break;
                case NYSE_EXCHANGE:
                    freshData = mboumDataService.getHistData(exchange,secId,type);
                    break;
                case LSE_EXCHANGE:
                    freshData = mboumDataService.getHistData(exchange,secId,type);
                    break;
            }

            sortedData = freshData.stream().sorted(new Comparator<DataPoint>() {
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

            freshData =  new ArrayList<>();
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

            Map<String,List<DataPoint>> dbData = new HashMap<>();
            dbData.put(today,freshData);
            baseDao.setDailySecData(exchange,secId,dbData);

        return freshData;
    }

    public List<DataPoint> getExchangeData(String exchange,String secId,String type) throws IOException, ExecutionException, InterruptedException {
        List<DataPoint> data = getSecDataFromDb(exchange, secId, type);
        if(data == null || data.isEmpty()){
            data = getSecDataFromSource(exchange, secId, type);
        }
        return data;
    }

    //@Cacheable(value = "secListCache")
    public List<Security> getSecList(String exchange) throws IOException, ExecutionException, InterruptedException {

        List<Security> op = new ArrayList<>();

        String today = new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(new Date());
        long daysBetween = 0;
        String dataDate = "";

        Map<String, Object> data = baseDao.getSecList(exchange);

        if(data != null) {

            Map.Entry<String, Object> firstEntry = data.entrySet().iterator().next();
            dataDate = firstEntry.getKey();

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
            LocalDate date1 = LocalDate.parse(dataDate, dtf);
            LocalDate date2 = LocalDate.parse(today, dtf);
            daysBetween = Duration.between(date1.atStartOfDay(), date2.atStartOfDay()).toDays();

            op = (List<Security>) data.get(dataDate);
        }

        //if(true){
        if (data == null || daysBetween > 7) {

            switch (exchange) {
                case BSE_EXCHANGE:
                    op = bseService.getSecList();
                    break;
                case NSE_EXCHANGE:
                    op = nseService.getSecList();
                    break;
                case NASDAQ_EXCHANGE:
                    op = nasdaqService.getSecList();
                    break;
                case NYSE_EXCHANGE:
                    op = nyseDataService.getSecList();
                    break;
                case LSE_EXCHANGE:
                    op = lseService.getSecList();
                    break;
            }

            Map<String, List<Security>> secList = new HashMap<>();
            secList.put(today, op);
            baseDao.setSecList(exchange, secList);

        } else {
            op = (List<Security>) data.get(dataDate);
        }

        return op;
    }

    public void loadExchangeDataForSec(String exchange,String secId,String type) throws IOException, ExecutionException, InterruptedException {
        List<DataPoint> data = getSecDataFromDb(exchange, secId, type);
        if(data == null || data.isEmpty()){
            data = getSecDataFromSource(exchange, secId, type);
            Thread.sleep(20000);
        }
    }

}
