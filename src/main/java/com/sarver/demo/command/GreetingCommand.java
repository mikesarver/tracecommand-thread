package com.sarver.demo.command;

import org.springframework.cloud.sleuth.TraceKeys;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.instrument.hystrix.TraceCommand;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.sarver.demo.controller.Greeting;

public class GreetingCommand extends TraceCommand<Greeting> {

  private static final Setter SETTER = Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("GreetingCommand"))
      .andCommandKey(HystrixCommandKey.Factory.asKey("getGreeting"));


  private final RestTemplate restTemplate;

  public GreetingCommand(Tracer tracer, TraceKeys traceKeys, RestTemplate restTemplate) {
    super(tracer, traceKeys, SETTER);
    this.restTemplate = restTemplate;
  }

  @Override
  public Greeting doRun() throws Exception {
    return restTemplate.exchange("http://localhost:8080/greeting",HttpMethod.GET, null, Greeting.class).getBody();
  }





}
