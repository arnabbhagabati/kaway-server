package com.kaway.main;


import com.kaway.actions.ExchangeActions;
import com.kaway.beans.SecType;
import com.kaway.beans.Security;
import com.kaway.beans.DataPoint;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Component
@Order(0)
class AppCdsApplicationListener implements ApplicationListener<ApplicationReadyEvent> {
  private final boolean appcds;
  private final ApplicationContext ctx;
  private final RestTemplate restTemplate = new RestTemplate();

  AppCdsApplicationListener(@Value("${appcds:false}") boolean appcds,
      ApplicationContext ctx) {
    this.appcds = appcds;
    this.ctx = ctx;
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    if (appcds) {
      restTemplate.getForEntity("http://localhost:8080/", String.class);
      SpringApplication.exit(ctx, () -> 0);
    }
  }
}

@CrossOrigin(origins = {"https://kaway-n3ahptldka-el.a.run.app","http://localhost:3000"})
@RestController
class KawayController {

  @Autowired
  ExchangeActions exchangeActions;

  @GetMapping("/")
  String hello() {
    return "Hello magga!";
  }

  @GetMapping("/histData/{exchange}/{secId}")
  List<DataPoint> getDefaultData(@PathVariable(value="exchange") String exchange, @PathVariable(value="secId") String secId, @RequestParam(name = "type") String type,@RequestParam(name = "stDate") String startDate, @RequestParam String endDate) throws IOException, ExecutionException, InterruptedException {
    //NasdaqService service = new NasdaqService();
    System.out.println("exchange="+exchange+" secId="+secId+" stDate ="+startDate+"  endDate="+endDate);
    List<DataPoint> data  = exchangeActions.getExchangeData(exchange,secId,type);
    return data;
  }

  @GetMapping("/secList/{exchange}")
  List<Security> getSecData(@PathVariable(value="exchange") String exchange) throws IOException, ExecutionException, InterruptedException {
    List<Security> data  = exchangeActions.getSecList(exchange);
    return data;
  }

  @GetMapping("/loadData/{exchange}")
  String loadData(@PathVariable(value="exchange") String exchange) throws IOException, ExecutionException, InterruptedException {
    Object secListData  = (Object) exchangeActions.getSecList(exchange);
    List<HashMap<String,Object>> secData = (List<HashMap<String,Object>>) secListData;
    for(HashMap<String,Object> secMap : secData){
        if(secMap.get("type").equals(SecType.INDEX_ALL.toString())){
              List<String> constituents = (List<String>) secMap.get("constituents");
              for(String code : constituents){
                  exchangeActions.getExchangeData(exchange,code,"STOCK");
                  Thread.sleep(20000);
              }
        }
    }
    return "Completed";
  }

}
