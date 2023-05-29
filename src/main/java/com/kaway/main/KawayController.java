package com.kaway.main;


import com.kaway.beans.NasdaqHistDataPoint;
import com.kaway.db.BaseDAO;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.kaway.main.KawayConstants.*;

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
  NasdaqService nasdaqService;

  @Autowired
  BaseDAO baseDao;

  @GetMapping("/")
  String hello() {
    return "Hello magga!";
  }

  @GetMapping("/histData/{exchange}/{secId}")
  List<NasdaqHistDataPoint> getDefaultData(@PathVariable(value="exchange") String exchange, @PathVariable(value="secId") String secId,@RequestParam(name = "stDate") String startDate, @RequestParam String endDate) throws IOException, ExecutionException, InterruptedException {
    //NasdaqService service = new NasdaqService();
    System.out.println("exchange="+exchange+" secId="+secId+" stDate ="+startDate+"  endDate="+endDate);
    Map<String, Object> data = baseDao.getDailySecData(exchange,secId);
    List<NasdaqHistDataPoint> freshdata = null;
    String today = new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(new Date());
    if(data==null || !data.containsKey(today)){
      freshdata = nasdaqService.getHistData(exchange,secId);
      Map<String,List<NasdaqHistDataPoint>> dbData = new HashMap<>();
      dbData.put(today,freshdata);
      baseDao.setDailySecData(exchange,secId,dbData);
    }else{
      freshdata = (List<NasdaqHistDataPoint>) data.get(today);
    }
    return freshdata;
  }

}
