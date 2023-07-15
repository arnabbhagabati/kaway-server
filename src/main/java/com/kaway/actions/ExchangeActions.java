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

    public Map<String,List<DataPoint>> getSecDataFromDb(String exchange,String secId,String type) throws IOException, ExecutionException, InterruptedException {
        Map<String,List<DataPoint>> dataMap = new HashMap<>();
        Map<String, Object> dailyData = baseDao.getDailySecData(exchange,secId+"_"+ONE_DAY);
        Map<String, Object> fifData = baseDao.getDailySecData(exchange,secId+"_"+FIFTEEN_MIN);
        String today = new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(new Date());
        String today_hh = new SimpleDateFormat(DEFAULT_DATE_FORMAT_HH).format(new Date());

        if(dailyData!=null && dailyData.containsKey(today)){
            dataMap.put(today,(List<DataPoint>) dailyData.get(today));
        }
        if(fifData!=null && fifData.containsKey(today_hh)){
            dataMap.put(today_hh,(List<DataPoint>) fifData.get(today_hh));
        }
        return dataMap;
    }

    public List<DataPoint> getSecHistDataFromSource(String exchange,String secId,String type) throws IOException, ExecutionException, InterruptedException {
        List<DataPoint> histData = null;

        String today = new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(new Date());

            switch(exchange){
                case BSE_EXCHANGE:
                    histData = mboumDataService.getHistData(exchange,secId,type);
                    break;
                case NSE_EXCHANGE:
                    histData = mboumDataService.getHistData(exchange,secId,type);
                    break;
                case NASDAQ_EXCHANGE:
                    histData = mboumDataService.getHistData(exchange,secId,type);
                    break;
                case NYSE_EXCHANGE:
                    histData = mboumDataService.getHistData(exchange,secId,type);
                    break;
                case LSE_EXCHANGE:
                    histData = mboumDataService.getHistData(exchange,secId,type);
                    break;
            }

            Map<String,List<DataPoint>> dbData = new HashMap<>();
            dbData.put(today,histData);
            //baseDao.setDailySecData(exchange,secId,dbData);

        return histData;
    }

    public List<DataPoint> getSec15mDataFromSource(String exchange,String secId,String type) throws IOException, ExecutionException, InterruptedException {
        List<DataPoint> fifteenMinData = null;

        String today_hh = new SimpleDateFormat(DEFAULT_DATE_FORMAT_HH).format(new Date());

        fifteenMinData = mboumDataService.get15mData(exchange,secId,type);

        Map<String,List<DataPoint>> dbData = new HashMap<>();
        dbData.put(today_hh,fifteenMinData);
        //baseDao.setDailySecData(exchange,secId,dbData);

        return fifteenMinData;
    }

    public  Map<String,List<DataPoint>> getExchangeData(String exchange,String secId,String type) throws IOException, ExecutionException, InterruptedException {
        Map<String,List<DataPoint>> dbData = getSecDataFromDb(exchange, secId, type);
        Map<String,List<DataPoint>> retData = new HashMap<>();
        String today = new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(new Date());
        String today_hh = new SimpleDateFormat(DEFAULT_DATE_FORMAT_HH).format(new Date());

        List<DataPoint> data15min = new ArrayList<>();
        List<DataPoint> histData = new ArrayList<>();

        if(dbData == null || !dbData.containsKey(today_hh)){
            data15min = getSec15mDataFromSource(exchange, secId, type);
            dbData.put(today_hh,data15min);
            Map<String,List<DataPoint>> freshData = new HashMap<>();
            freshData.put(today_hh,data15min);
            baseDao.setDailySecData(exchange,secId+"_"+FIFTEEN_MIN,freshData);
        }

        //if(true){
        if(dbData == null || !dbData.containsKey(today)){
            histData = getSecHistDataFromSource(exchange, secId, type);
            if(histData.size()<10){
                histData = data15min;
            }
            dbData.put(today,histData);
            Map<String,List<DataPoint>> freshData = new HashMap<>();
            freshData.put(today,histData);
            baseDao.setDailySecData(exchange,secId+"_"+ONE_DAY,freshData);
        }

        retData.put(ONE_DAY,dbData.get(today));
        retData.put(FIFTEEN_MIN,dbData.get(today_hh));

        return retData;
    }

    @Cacheable(value = "secListCache")
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
        //copy from getExchangeData when ready
        try {
            getExchangeData(exchange, secId, type);
            Thread.sleep(2000);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

}
