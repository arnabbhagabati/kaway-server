package com.kaway.actions;

import com.couchbase.lite.CouchbaseLiteException;
import com.kaway.beans.DataPoint;
import com.kaway.beans.Security;
import com.kaway.db.BaseDAO;
import com.kaway.db.LocalBaseDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.kaway.main.KawayConstants.DEFAULT_DATE_FORMAT;
import static com.kaway.main.KawayConstants.ONE_DAY;

@Service
public class FilterActions {

    @Autowired
    LocalBaseDao baseDao;

    public List<Security> getStableSecs(List<Security> allSecs) throws CouchbaseLiteException {
        List<Security> op = new ArrayList<>();
        for(Security sec: allSecs){
            String today = new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(new Date());
            Map<String, Object> dailyData = baseDao.getDailySecData(sec.getExchange(),sec.getId()+"_"+ONE_DAY);
            List<DataPoint> dataPoints = (List<DataPoint>) dailyData.get(today);
            Map<LocalDate, DataPoint> sortedDataPoints = getSortedDataPointMap(dataPoints);

            if(sortedDataPoints == null || sortedDataPoints.isEmpty()){
                continue;
            }
            List<Long> averages = new ArrayList<>();
            for(int i=0;i<6;i++){
                int start=i*30;
                int end = (i+1)*30;
                Long avg = getAverageValue(sortedDataPoints,start,end);
                averages.add(avg);
            }

            Long prevAvg = -1L;
            boolean stable = true;
            for(Long avg :averages){
                if(prevAvg == -1L){
                    prevAvg = avg;
                }else{
                    Long diff = Math.abs(prevAvg-avg);
                    Double diffPercent = ((Double) ((double)diff)/avg) *100;
                    if(diffPercent > 25){
                        stable = false;
                        break;
                    }
                }
            }

            if(stable) {
                Long absoluteDiff = Math.abs(averages.get(0) - averages.get(averages.size() - 1));
                Double diffPercent = ((Double) ((double) absoluteDiff) / averages.get(0)) * 100;
                if (diffPercent > 25) {
                    stable = false;
                }
            }

            if(stable){
                op.add(sec);
            }

        }

        return op;
    }

    private Map<LocalDate,DataPoint> getSortedDataPointMap(List<DataPoint> dataPoints){
        Map<LocalDate, DataPoint> sortedDataPoints = new TreeMap<>(Collections.reverseOrder());
        if(dataPoints != null && !dataPoints.isEmpty()){
            for(DataPoint dp : dataPoints){
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
                LocalDate date = LocalDate.parse(dp.getTime(), formatter);
                sortedDataPoints.put(date,dp);
            }
        }

        return sortedDataPoints;
    }

    private Long getAverageValue(Map<LocalDate, DataPoint> sortedDataPoints,int start,int end){
        int i=0;
        Double sum = 0D;
        Double average = 0D;
        for(Map.Entry<LocalDate,DataPoint> e : sortedDataPoints.entrySet()){
            if(i>=start){
                sum=sum+e.getValue().getClose();
            }
            if(i>=end){
                break;
            }
            i++;
        }

        average = (sum/(end-start));

        return Math.round(average);
    }

}
