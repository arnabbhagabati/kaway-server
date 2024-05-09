package com.kaway.main;


import com.couchbase.lite.CouchbaseLiteException;
import com.google.firebase.auth.FirebaseAuthException;
import com.kaway.actions.DashboardActions;
import com.kaway.actions.ExchangeActions;
import com.kaway.beans.Dashboard;
import com.kaway.beans.SecType;
import com.kaway.beans.Security;
import com.kaway.beans.DataPoint;
import com.kaway.service.NasdaqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.ValidationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


@CrossOrigin(origins = {"https://bullcharts.org","https://kaway-og2rb3iodq-as.a.run.app","http://localhost:3000"})
@RestController
class KawayController {

  @Autowired
  ExchangeActions exchangeActions;

  @Autowired
  NasdaqService nasdaqService; // tmp

  @Autowired
  DashboardActions dashboardActions;

  @GetMapping("/")
  String hello() {
    return "Hello magga!";
  }

  @GetMapping("/histData/{exchange}/{secId}")
  Map<String,List<DataPoint>> getDefaultData(@PathVariable(value="exchange") String exchange, @PathVariable(value="secId") String secId, @RequestParam(name = "type") String type,@RequestParam(name = "type") String days) throws IOException, ExecutionException, InterruptedException, CouchbaseLiteException {
    //NasdaqService service = new NasdaqService();
    System.out.println("exchange="+exchange+" secId="+secId+" type ="+type);
    Map<String,List<DataPoint>> data  = exchangeActions.getExchangeData(exchange,secId,type);
    return data;
  }

  @GetMapping("/secList/{exchange}")
  List<Security> getSecData(@PathVariable(value="exchange") String exchange) throws IOException, ExecutionException, InterruptedException, CouchbaseLiteException {
    List<Security> data  = exchangeActions.getSecList(exchange);
    return data;
  }

  @GetMapping("/loadData/{exchange}")
  String loadData(@PathVariable(value="exchange") String exchange,@RequestParam(name = "idxCode") String idxCode) throws IOException, ExecutionException, InterruptedException, CouchbaseLiteException {
    Object secListData  = (Object) exchangeActions.getSecList(exchange);
    List<HashMap<String,Object>> secData = (List<HashMap<String,Object>>) secListData;
    for(HashMap<String,Object> secMap : secData){
        if(secMap.get("type").equals(SecType.INDEX_ALL.toString())){
              if(null==idxCode || idxCode.isEmpty() || idxCode.equals(secMap.get("code"))) {
                List<String> constituents = (List<String>) secMap.get("constituents");
                for (String code : constituents) {
                  exchangeActions.loadExchangeDataForSec(exchange, code, "STOCK");
                }
              }
        }
    }
    return "Completed";
  }

  @PostMapping("/users/{userEmail}/dashboard")
  public String addDashboard(@RequestHeader(value="User-Token") String userToken,
                            @PathVariable(value="userEmail") String email,
                            @RequestBody Dashboard dashboard,
                            @RequestParam(name = "uid") String uid) throws ValidationException, IOException, FirebaseAuthException {
    //System.out.println(dashboard);
    return dashboardActions.saveDashBoard(dashboard,userToken,uid,email);
  }

  @DeleteMapping("/users/{userEmail}/{dashboard}")
  public String deleteDashboard(@RequestHeader(value="User-Token") String userToken,
                            @PathVariable(value="userEmail") String email,
                           @PathVariable (name = "dashboard") String dashboardName,
                           @RequestParam(name = "uid") String uid) throws ValidationException, IOException, FirebaseAuthException, ExecutionException, InterruptedException {
    return dashboardActions.deleteDashboard(userToken,uid,email,dashboardName);
  }


  @GetMapping("/users/{userEmail}/dashboards")
  public List<Dashboard> getDashboards(@RequestHeader(value="User-Token") String userToken,
                                       @PathVariable(value="userEmail") String email,
                                       @RequestParam(name = "uid") String uid) throws ValidationException, IOException, FirebaseAuthException, ExecutionException, InterruptedException, CouchbaseLiteException {
    return dashboardActions.getDashboards(userToken,uid,email);
  }

}
