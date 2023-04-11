package com.rostyslav.trading.bot.model.input.socket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
  @JsonProperty("E")
  private Long eventTime;
  @JsonProperty("k")
  private EventCandle candle;
}
