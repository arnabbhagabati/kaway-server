package com.kaway.service;

import com.couchbase.lite.CouchbaseLiteException;
import com.kaway.beans.Dashboard;
import com.kaway.db.BaseDAO;
import com.kaway.db.LocalBaseDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.kaway.main.KawayConstants.SUCCESS;

@Service
public class DashboardService {

    @Autowired
    BaseDAO baseDao;

    public String saveDashBoard(Dashboard dashboard,String userId,String email) throws Exception {
        String retMessage = "Dashboard saved successfully";

        try {
            String validationMsg = "Error Saving dashboard";
            Map<String,Dashboard> existingDashboardMap =  getDashboards(userId,email);
            validationMsg = validateNewDashboard(dashboard,userId,email,existingDashboardMap);
            if(validationMsg.equals(SUCCESS)){
                    existingDashboardMap.put(dashboard.getName(),dashboard);
                    baseDao.saveDashboard(userId, email, existingDashboardMap);
            }else{
                return validationMsg;
            }

        }catch(ExecutionException | IOException | InterruptedException | CouchbaseLiteException e){
            e.printStackTrace();
            retMessage = "An error ocurred saving your dashboard";
        }

        return retMessage;
    }

    public void deleteDashboard(String userId,String email,String dashboardName) throws Exception {
        baseDao.deleteDashboard(userId,email,dashboardName);
    }

    public Map<String,Dashboard> getDashboards(String userId,String email) throws Exception {

        Map<String,Dashboard> dashboardsMap = baseDao.getUserDashboards(userId,email);
        return dashboardsMap;

    }


    private String validateNewDashboard(Dashboard newDashboard, String uid, String email,Map<String,Dashboard> existingDashboardMap) throws IOException, ExecutionException, InterruptedException {

        if(newDashboard.getSecurityList().size() >100){
            return "Cannot save a dashboard with more than 100 symbols";
        }

        if(existingDashboardMap != null && !existingDashboardMap.isEmpty()) {
            if (existingDashboardMap.size() == 10) {
                return "Maximun limit for dashboard reached! Can only save up to 10 Dashboards per user!";
            }

            for (Map.Entry<String,Dashboard> e : existingDashboardMap.entrySet()) {
                String name = e.getKey();
                if (name.equals(newDashboard.getName())) {
                    return "Dashboard with same name exists!";
                }
            }
        }

        return SUCCESS;

    }
}
