package com.sarver.demo.command;

import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.TraceKeys;
import org.springframework.cloud.sleuth.Tracer;

import com.netflix.hystrix.HystrixCommand;


public abstract class TraceCommandParentSpan<R> extends HystrixCommand<R> {

  private static final String HYSTRIX_COMPONENT = "hystrix";

  private final Tracer tracer;
  private final TraceKeys traceKeys;
  private final Span parentSpan;

  protected TraceCommandParentSpan(Tracer tracer, TraceKeys traceKeys, Setter setter) {
    this(tracer, traceKeys, tracer.getCurrentSpan(), setter);
  }

  protected TraceCommandParentSpan(Tracer tracer, TraceKeys traceKeys, Span parentSpan, Setter setter) {
    super(setter);
    this.tracer = tracer;
    this.traceKeys = traceKeys;
    this.parentSpan = parentSpan;

  }

  @Override
  protected R run() throws Exception {
    String commandKeyName = getCommandKey().name();
    Span span = this.tracer.createSpan(commandKeyName, this.parentSpan);
    this.tracer.addTag(Span.SPAN_LOCAL_COMPONENT_TAG_NAME, HYSTRIX_COMPONENT);
    this.tracer.addTag(this.traceKeys.getHystrix().getPrefix() +
        this.traceKeys.getHystrix().getCommandKey(), commandKeyName);
    this.tracer.addTag(this.traceKeys.getHystrix().getPrefix() +
        this.traceKeys.getHystrix().getCommandGroup(), getCommandGroup().name());
    this.tracer.addTag(this.traceKeys.getHystrix().getPrefix() +
        this.traceKeys.getHystrix().getThreadPoolKey(), getThreadPoolKey().name());
    try {
      return doRun();
    }
    finally {
      this.tracer.close(span);
    }
  }

  public abstract R doRun() throws Exception;

}
