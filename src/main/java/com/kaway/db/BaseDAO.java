package com.kaway.db;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.WriteResult;
import com.kaway.beans.DataPoint;
import com.kaway.beans.NasdaqHistDataPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class BaseDAO {

    @Autowired
    FireStoreConfig fireStoreConfig;

    public void setDailySecData(String exchange, String secId, Map<String,List<DataPoint>> data) throws IOException, ExecutionException, InterruptedException {

        // Add a new document (asynchronously) in collection "cities" with id "LA"
        ApiFuture<WriteResult> future = fireStoreConfig.dbContainer().db.collection(exchange).document(secId).set(data);
        // ...
        // future.get() blocks on response
        System.out.println("Update time : " + future.get().getUpdateTime());
    }


    public Map<String, Object> getDailySecData(String exchange, String secId) throws IOException, ExecutionException, InterruptedException {
        DocumentReference docRef = fireStoreConfig.dbContainer().db.collection(exchange).document(secId);
        // asynchronously retrieve the document
        ApiFuture<DocumentSnapshot> future = docRef.get();
        // block on response
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            return document.getData();
        } else {
            System.out.println("No such document!");
        }

        return null;
    }
}
