package com.sarver.demo.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;
import lombok.experimental.Builder;

@Data
@Builder
@JsonInclude(Include.NON_EMPTY)
@JsonDeserialize(builder = Greeting.GreetingBuilder.class)
public class Greeting {
  private final String message;

  @JsonPOJOBuilder(withPrefix = "")
  public static final class GreetingBuilder {
  }

}
