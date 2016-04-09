package com.sarver.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.TraceKeys;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import com.sarver.demo.command.GreetingCommand;

import rx.Observable;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

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

  /**
   * Simple controller using TraceCommand in main thread
   * the sleuth traceId match across threads
   */
  @RequestMapping(value = "hello/{id}", method = RequestMethod.GET, produces = "application/json")
  public Hello getHello(@PathVariable("id") String id) {
    LOGGER.info("getHello id {}:", id);
    Greeting greeting = new GreetingCommand(tracer, traceKeys, restTemplate).execute();
    return Hello.builder().greeting(greeting.getMessage()).id(id).build();
  }


  /**
   * Controller where the TraceCommand is created is a separate thread.
   * the sleuth traceId do not match across threads
   *
   * In this trivial example creating the command can be done in the main thread and passed into the calling thread
   * A real world example, the first command returns master with collection of ids.  then we have to make a second
   * call to get the information for each id in the collection.
   * creating the command for the second call would cause the problem since it is already running in another thread
   */
  @RequestMapping(value = "hellothread/{id}", method = RequestMethod.GET, produces = "application/json")
  public DeferredResult<Hello> getHelloThread(@PathVariable("id") String id) throws Exception {
    LOGGER.info("getHelloThread id {}:", id);

    Observable<Hello> obs =
        callDefer()
        .map(g -> Hello.builder().greeting(g.getMessage()).id(id).build());

    DeferredResult<Hello> dr = new DeferredResult<>(500L);

    obs.subscribeOn(Schedulers.io())
       .subscribe(dr::setResult,
                  dr::setErrorResult,
                  setNotFoundWhenResultNotSet(dr));
    return dr;

  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static Action0 setNotFoundWhenResultNotSet(final DeferredResult result) {
    return () -> {
      if (!result.isSetOrExpired()) {
        result.setResult(new ResponseEntity<>(HttpStatus.NOT_FOUND));
      }
    };
  }

  Observable<Greeting> callDefer() {
    return Observable.defer(() -> {
      try {
        Greeting g = new GreetingCommand(tracer, traceKeys, restTemplate).execute();
        return g == null ? Observable.empty() : Observable.just(g);
      } catch (RuntimeException e) {
        return Observable.error(e);
      }
    });

  }


  /**
   *
   * second controller being called
   */
  @RequestMapping(value = "greeting", method = RequestMethod.GET, produces = "application/json")
  public Greeting getGreeting() {
    LOGGER.info("getGreeting");
    return Greeting.builder().message("hello").build();
  }


}
