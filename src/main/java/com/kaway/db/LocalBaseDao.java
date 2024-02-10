package com.kaway.db;

import com.couchbase.lite.*;

import com.couchbase.lite.Collection;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaway.beans.Dashboard;
import com.kaway.beans.DataPoint;
import com.kaway.beans.SecType;
import com.kaway.beans.Security;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.kaway.main.KawayConstants.SEC_LIST;
import static com.kaway.main.KawayConstants.USER_COLLECTION;

@Service
public class LocalBaseDao {

    Database database;
    private static String SEC_PRICE_DATA = "priceData";
    private static String SEC_DATA = "secData";

    public LocalBaseDao() {
        CouchbaseLite.init();
        try {
            DatabaseConfiguration config = new DatabaseConfiguration();
            config.setDirectory("src/main/resources/");
            database = new Database("secDb", config);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public void setDailySecData(String exchange, String secId, Map<String, List<DataPoint>> data) throws CouchbaseLiteException {
            Collection collection = database.getCollection(exchange);

            if (collection == null) {
                collection = database.createCollection(exchange);
            }

            JsonObject jsonData = new JsonObject();

            for (Map.Entry<String, List<DataPoint>> e : data.entrySet()) {
                JsonArray array = new JsonArray();
                for(DataPoint dp : e.getValue()){
                    array.add(getJsonObject(dp));
                }
                jsonData.add(e.getKey(),array);
            }

            MutableDocument secDoc = new MutableDocument(SEC_PRICE_DATA).setString(secId,jsonData.toString());
            collection.save(secDoc);

    }


    public Map<String, Object> getDailySecData(String exchange, String secId) throws CouchbaseLiteException {
        Collection collection = database.getCollection(exchange);
        JsonObject jsonData = null;
        Map<String, Object> op = new HashMap<>();

        Document document = database.getCollection(exchange).getDocument(SEC_PRICE_DATA);
        if (document != null) {
            String jsonStr = document.getString(secId);
            if(jsonStr != null && !jsonStr.isEmpty()){
                jsonData = new JsonParser().parse(jsonStr).getAsJsonObject();
            }
        }

        if(jsonData != null){
            for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {
                List<DataPoint> dpList = new ArrayList<>();
                JsonArray dpJsonArray = (JsonArray) entry.getValue();
                for(JsonElement dpJson : dpJsonArray){
                    JsonObject dpJsonObject =  (JsonObject)  dpJson;
                    DataPoint dp = new DataPoint(dpJsonObject.get("time").getAsString(),
                                                    dpJsonObject.get("utcTimestamp").getAsLong(),
                                                    dpJsonObject.get("open").getAsFloat(),
                                                    dpJsonObject.get("close").getAsFloat(),
                                                    dpJsonObject.get("high").getAsFloat(),
                                                    dpJsonObject.get("low").getAsFloat(),
                                                    dpJsonObject.get("volume").getAsInt());
                    dpList.add(dp);
                }
                op.put(entry.getKey(),dpList);
            }
        }
        return op;
    }


    public Map<String, Object> getSecList(String exchange) throws CouchbaseLiteException {
        Collection collection = database.getCollection(exchange);
        Map<String, Object> op = new HashMap<>();

        if(collection == null){
            return  null;
        }

        JsonObject jsonData = null;
        Document document = database.getCollection(exchange).getDocument(SEC_DATA);
        if (document != null) {
            System.out.println("Document ID :: " + document.getId());
            String jsonStr = document.getString("SecList");
            jsonData = new JsonParser().parse(jsonStr).getAsJsonObject();
        }

        if(jsonData != null){
            for (Map.Entry<String, JsonElement> entry : jsonData.entrySet()) {
                List<Security> secList = new ArrayList<>();
                JsonArray dpJsonArray = (JsonArray) entry.getValue();
                for(JsonElement dpJson : dpJsonArray){
                    JsonObject dpJsonObject =  (JsonObject)  dpJson;
                    SecType secType = SecType.valueOf(dpJsonObject.get("type").getAsString());
                    List<String> constituents = new ArrayList<>();
                    for(JsonElement je  : dpJsonObject.get("constituents").getAsJsonArray()){
                        String cons = je.getAsString();
                        constituents.add(cons);
                    }
                    Security sec = new Security(
                            dpJsonObject.get("id").getAsString(),
                            dpJsonObject.get("code").getAsString(),
                            dpJsonObject.get("name").getAsString(),
                            dpJsonObject.get("displayName").getAsString(),
                            secType);
                    sec.setConstituents(constituents);
                    sec.setExchange(exchange);
                    secList.add(sec);
                }
                op.put(entry.getKey(),secList);
            }
        }
        return op;
    }

    public void setSecList(String exchange, Map<String, List<Security>> data) throws CouchbaseLiteException {
        Collection collection = database.getCollection(exchange);

        if (collection == null) {
            collection = database.createCollection(exchange);
        }

        JsonObject jsonData = new JsonObject();

        for (Map.Entry<String, List<Security>> e : data.entrySet()) {
            JsonArray array = new JsonArray();
            for(Security sec : e.getValue()){
                array.add(getJsonObject(sec));
            }
            jsonData.add(e.getKey(),array);
        }

        MutableDocument secDoc = new MutableDocument(SEC_DATA).setString("SecList",jsonData.toString());
        collection.save(secDoc);
    }


    public Map<String, Object> getUserDashboards(String uid, String email) throws IOException, ExecutionException, InterruptedException {
        String key=email+"_"+uid;
        return null;
    }

    public void saveDashboard(String uid, String email, Map<String, Dashboard> data) throws IOException{
        // Add a new document (asynchronously) in collection "cities" with id "LA"
        String key=email+"_"+uid;
    }

    public void deleteDashboard(String uid, String email, String dashboard) throws IOException, ExecutionException, InterruptedException {

        String key=email+"_"+uid;

    }

    private Collection getCollection(String name) throws CouchbaseLiteException {
        Collection exchangeCollection = null;
        final Set<Scope> scopes = database.getScopes();
        for (Scope scope: scopes) {
            System.out.println("Scope :: " + scope.getName());
            final Set<Collection> collections = scope.getCollections();
            for (Collection collection: collections) {
                System.out.println("    Collection :: " + collection.getName());
                if(collection.getName().equals(name)){
                    exchangeCollection = collection;
                }
            }
        }

        if(exchangeCollection == null){
            exchangeCollection = database.createCollection(name);
        }

        return exchangeCollection;

    }

    public JsonObject getJsonObject(Security sec){
        JsonObject obj = new JsonObject();
        obj.addProperty("id",sec.getId());
        obj.addProperty("code",sec.getCode());
        obj.addProperty("name",sec.getName());
        obj.addProperty("displayName",sec.getDisplayName());
        obj.addProperty("type",sec.getType().toString());
        JsonArray consArray = new JsonArray();
        if(sec.getConstituents() != null && !sec.getConstituents().isEmpty()){
            for(String cons : sec.getConstituents()){
                consArray.add(cons);
            }
        }

        obj.add("constituents",consArray);
        obj.addProperty("exchange",sec.getExchange());

        return obj;
    }

    public JsonObject getJsonObject(DataPoint dp){
        JsonObject obj = new JsonObject();
        obj.addProperty("time",dp.getTime());
        obj.addProperty("utcTimestamp",dp.getUtcTimestamp());
        obj.addProperty("open",dp.getOpen());
        obj.addProperty("close",dp.getClose());
        obj.addProperty("high",dp.getHigh());
        obj.addProperty("low",dp.getLow());
        obj.addProperty("volume",dp.getVolume());

        return obj;
    }
}
