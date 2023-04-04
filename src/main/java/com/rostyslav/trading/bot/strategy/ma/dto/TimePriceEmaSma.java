package com.rostyslav.trading.bot.strategy.ma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimePriceEmaSma {

  private Long time;

  private Double price;

  private Double ema;

  private Double sma;
}
