package com.kaway.actions;

import com.google.cloud.BaseService;
import com.kaway.beans.BSESec;
import com.kaway.beans.DataPoint;
import com.kaway.beans.NasdaqHistDataPoint;
import com.kaway.db.BaseDAO;
import com.kaway.service.BSEService;
import com.kaway.service.NSEDataService;
import com.kaway.service.NasdaqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    BSEService bseService;

    public List<DataPoint> getExchangeData(String exchange,String secId) throws IOException, ExecutionException, InterruptedException {
        Map<String, Object> data = baseDao.getDailySecData(exchange,secId);
        List<DataPoint> sortedData = null;
        List<DataPoint> freshData = null;
        String today = new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(new Date());
        if(data==null || !data.containsKey(today)){

            switch(exchange){
                case BSE_EXCHANGE:
                    freshData = nasdaqService.getHistData(exchange,secId);
                    break;
                case NSE_EXCHANGE:
                    freshData = nseDataService.getHistData(exchange,secId);
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

            Map<String,List<DataPoint>> dbData = new HashMap<>();
            dbData.put(today,sortedData);
            baseDao.setDailySecData(exchange,secId,dbData);
        }else{
            sortedData = (List<DataPoint>) data.get(today);
        }
        return sortedData;
    }

    public List<BSESec> getSecList(String exchange) throws IOException, ExecutionException, InterruptedException {

        List<BSESec> op = new ArrayList<>();
        String today = new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(new Date());
        long daysBetween = 0;

        Map<String, Object> data = baseDao.getSecList(exchange);

        if(data != null) {
            Map.Entry<String, Object> firstEntry = data.entrySet().iterator().next();
            String dataDate = firstEntry.getKey();

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
            LocalDate date1 = LocalDate.parse(dataDate, dtf);
            LocalDate date2 = LocalDate.parse(today, dtf);
            daysBetween = Duration.between(date1.atStartOfDay(), date2.atStartOfDay()).toDays();
        }

        if (data == null || daysBetween > 7) {

            switch (exchange) {
                case BSE_EXCHANGE:
                    op = bseService.getSecList();
                    break;
                case NSE_EXCHANGE:
                    op = null;
                    break;
            }

            Map<String, List<BSESec>> secList = new HashMap<>();
            secList.put(today, op);
            baseDao.setSecList(exchange, secList);

        } else {
            op = (List<BSESec>) data.get(today);
        }

        return op;
    }

}
