package com.kaway.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.kaway.beans.Dashboard;
import com.kaway.beans.Dashboards;
import com.kaway.beans.Security;
import com.kaway.db.BaseDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.kaway.main.KawayConstants.DASHBORADS;
import static com.kaway.main.KawayConstants.SUCCESS;

@Service
public class DashboardService {

    @Autowired
    BaseDAO baseDao;

    public String saveDashBoard(Dashboard dashboard,String userId,String email) {
        String retMessage = "Dashboard saved successfully";

        try {
            String validationMsg = "Error Saving dashboard";
            Map<String,Dashboard> existingDashboardMap =  getDashboards(userId,email);
            validationMsg = validateNewDashboard(dashboard,userId,email,existingDashboardMap);
            if(validationMsg.equals(SUCCESS)){
                    existingDashboardMap.put(dashboard.getName(),dashboard);
                    baseDao.saveDashboard(userId, email, existingDashboardMap);
            }else{
                return retMessage;
            }

        }catch(ExecutionException | IOException | InterruptedException e){
            e.printStackTrace();
            retMessage = "An error ocurred saving your dashboard";
        }

        return retMessage;
    }

    public void deleteDashboard(String userId,String email,String dashboardName) throws IOException, ExecutionException, InterruptedException {
        baseDao.deleteDashboard(userId,email,dashboardName);
    }

    public Map<String,Dashboard> getDashboards(String userId,String email) throws IOException, ExecutionException, InterruptedException {
        Map<String,Dashboard> op = new HashMap<>();
        Map<String,Object> dashboardsMap = baseDao.getUserDashboards(userId,email);
        List<Dashboard> dashboardList =  new ArrayList<>();
        if(dashboardsMap != null && !dashboardsMap.isEmpty()){
            //List<Map<String,Object>> listofDbMaps = (List<Map<String,Object>>) dashboardsMap.get(DASHBORADS);
            for(Map.Entry<String,Object> e: dashboardsMap.entrySet()){
                String name = (String) e.getKey();
                Map<String,Object> map = (Map<String,Object>) e.getValue();
                List<Security> secList = (List<Security>) map.get("securityList");
                Dashboard db = new Dashboard(name,secList);
                op.put(name,db);
            }
        }
        return op;
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
