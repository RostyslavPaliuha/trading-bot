package com.rostyslav.trading.bot.model.input.socket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventCandle {
  @JsonProperty("t")
  private Long openTime;
  @JsonProperty("c")
  private Double closedPrice;
  @JsonProperty("x")
  private Boolean isClosed;
}
