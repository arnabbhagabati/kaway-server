package com.kaway.actions;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.kaway.beans.Dashboard;
import com.kaway.db.FireStoreConfig;
import com.kaway.main.FireBaseConfig;
import com.kaway.main.KawayConstants;
import com.kaway.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.kaway.main.KawayConstants.SUCCESS;

@Service
public class DashboardActions {

    @Autowired
    DashboardService dashboardService;

    @Autowired
    FireBaseConfig fireBaseConfig;

    @Autowired
    FireStoreConfig fireStoreConfig;

    public String saveDashBoard(Dashboard dashboard, String userToken, String userId, String email) throws FirebaseAuthException, ValidationException, IOException {
        String valMsg = validateUser(userToken,userId);
        if(valMsg.equals(SUCCESS)) {
            return dashboardService.saveDashBoard(dashboard, userId, email);
        }else{
            return valMsg;
        }
    }


    public List<Dashboard> getDashboards(String userToken,String userId,String email) throws IOException, ExecutionException, InterruptedException {
        String valMsg = validateUser(userToken,userId);
        List<Dashboard> dashboards = new ArrayList<>();
        if(valMsg.equals(SUCCESS)) {
            dashboards =  dashboardService.getDashboards(userId, email);
        }
        return dashboards;
    }

    public String deleteDashboard(String userToken,String userId,String email,String dashboardName) throws IOException, ExecutionException, InterruptedException {
        String valMsg = validateUser(userToken,userId);
        if(valMsg.equals(SUCCESS)) {
            dashboardService.deleteDashboard(userId, email, dashboardName);
            return SUCCESS;
        }
        return valMsg;
    }

    private String validateUser(String userToken, String userId) throws IOException {
        String uid = "";
        try {
            fireBaseConfig.getMyFirebaseApp().getFirebaseApp();
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(userToken);
            uid = decodedToken.getUid();
        }catch(FirebaseAuthException e){
            e.printStackTrace();
            return "Error validating user";
        }
        if(!uid.equals(userId)){
            return "User could not be validated";
        }

        return SUCCESS;
    }
}
