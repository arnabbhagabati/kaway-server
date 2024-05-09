package com.kaway.actions;

import com.couchbase.lite.CouchbaseLiteException;
import com.google.firebase.auth.FirebaseAuthException;
import com.kaway.beans.Dashboard;
import com.kaway.db.FireStoreConfig;
import com.kaway.main.FireBaseConfig;
import com.kaway.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.kaway.main.KawayConstants.SUCCESS;
import static com.kaway.main.KawayConstants.FAILURE;

@Service
public class DashboardActions {

    @Autowired
    DashboardService dashboardService;

    @Autowired
    FireBaseConfig fireBaseConfig;

    @Autowired
    FireStoreConfig fireStoreConfig;

    public String saveDashBoard(Dashboard dashboard, String userToken, String userId, String email) throws FirebaseAuthException, ValidationException, IOException {
        String valMsg = validateUserLocal(userToken,userId);
        //if(valMsg.equals(SUCCESS)) {
        if(true) {
            return dashboardService.saveDashBoard(dashboard, userId, email);
        }else{
            return valMsg;
        }
    }


    public List<Dashboard> getDashboards(String userToken,String userId,String email) throws IOException, ExecutionException, InterruptedException, CouchbaseLiteException {
        String valMsg = validateUserLocal(userToken,userId);
        List<Dashboard> dashboards = new ArrayList<>();
        if(valMsg.equals(SUCCESS)) {
            Map<String,Dashboard>  dashboardsMap =  dashboardService.getDashboards(userId, email);
            dashboards =  dashboardsMap.values().stream().collect(Collectors.toList());
        }
        return dashboards;
    }

    public String deleteDashboard(String userToken,String userId,String email,String dashboardName) throws IOException, ExecutionException, InterruptedException {
        String valMsg = validateUserLocal(userToken,userId);
        if(valMsg.equals(SUCCESS)) {
            dashboardService.deleteDashboard(userId, email, dashboardName);
            return SUCCESS;
        }
        return valMsg;
    }

    private String validateUserFireBase(String userToken, String userId) throws IOException {
        /*String uid = "";
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
        }*/

        return FAILURE;
    }


    private String validateUserLocal(String userToken, String userId) throws IOException {
        return SUCCESS;
    }
}
