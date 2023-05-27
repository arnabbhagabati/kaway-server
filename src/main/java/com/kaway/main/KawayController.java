package com.kaway.main;

import com.kaway.beans.NasdaqHistDataPoint;
import com.kaway.service.NasdaqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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

@CrossOrigin(origins = "https://kaway-n3ahptldka-el.a.run.app")
@RestController
class KawayController {

  @Autowired
  NasdaqService nasdaqService;

  @GetMapping("/")
  String hello() {
    return "Hello magga!";
  }

  @GetMapping("/default")
  List<NasdaqHistDataPoint> getDefaultdata() {

    //NasdaqService service = new NasdaqService();
    return nasdaqService.getHistData("BSE","BOM500547");
  }

}
