package com.sarver.demo.controller;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.TraceKeys;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.sarver.demo.command.GreetingCommand;
import com.sarver.demo.command.GreetingCommandParentSpan;

@RestController
public class HelloController {

  @Autowired
  RestTemplate restTemplate;
  @Autowired
  Tracer tracer;
  @Autowired
  TraceKeys traceKeys;
  @Autowired
  ThreadPoolTaskExecutor executor;

  private static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);

  @RequestMapping(value = "hello/{id}", method = RequestMethod.GET, produces = "application/json")
  public Hello getHello(@PathVariable("id") String id) {
    LOGGER.info("getHello id {}:", id);
    Greeting greeting = new GreetingCommand(tracer, traceKeys, restTemplate).execute();
    return Hello.builder().greeting(greeting.getMessage()).id(id).build();
  }


  @RequestMapping(value = "hellothread/{id}", method = RequestMethod.GET, produces = "application/json")
  public Hello getHelloThread(@PathVariable("id") String id) throws Exception {
    LOGGER.info("getHelloThread id {}:", id);

    CompletableFuture<Greeting> g = CompletableFuture.supplyAsync(() -> new GreetingCommand(tracer, traceKeys, restTemplate).execute(), executor);
    Greeting greeting = g.get();
    return Hello.builder().greeting(greeting.getMessage()).id(id).build();
  }

  @RequestMapping(value = "hellothreadps/{id}", method = RequestMethod.GET, produces = "application/json")
  public Hello getHelloThreadps(@PathVariable("id") String id) throws Exception {
    LOGGER.info("getHelloThread id {}:", id);
    Span parentSpan = tracer.getCurrentSpan();
    CompletableFuture<Greeting> g = CompletableFuture.supplyAsync(() -> new GreetingCommandParentSpan(tracer, traceKeys, parentSpan, restTemplate).execute(), executor);
    Greeting greeting = g.get();
    return Hello.builder().greeting(greeting.getMessage()).id(id).build();
  }

  @RequestMapping(value = "greeting", method = RequestMethod.GET, produces = "application/json")
  public Greeting getGreeting() {
    LOGGER.info("getGreeting");
    return Greeting.builder().message("hello").build();
  }


}
