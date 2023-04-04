package com.rostyslav.trading.bot.service.indicator.calculator.ma;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmaSma {

  private Double ema;

  private Double sma;
}