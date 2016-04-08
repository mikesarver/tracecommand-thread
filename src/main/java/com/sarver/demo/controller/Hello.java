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
@JsonDeserialize(builder = Hello.HelloBuilder.class)
public class Hello {
  private final String greeting;
  private final String id;

  @JsonPOJOBuilder(withPrefix = "")
  public static final class HelloBuilder {
  }

}
