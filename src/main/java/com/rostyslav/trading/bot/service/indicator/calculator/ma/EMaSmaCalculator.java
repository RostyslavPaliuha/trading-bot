package com.rostyslav.trading.bot.service.indicator.calculator.ma;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class EMaSmaCalculator {

  public static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("#0.00");
  private final Core ta;

  public EMaSmaCalculator() {
    this.ta = new Core();
  }

  public EmaSma[] calculate(double[] closedPrices) {
    int startIdx = 0;
    int endIdx = closedPrices.length - 1;
    int period = 10;
    double[] outEMA = new double[closedPrices.length];
    double[] outSMA = new double[closedPrices.length];
    MInteger outBegIdx = new MInteger();
    MInteger outNBElemnt = new MInteger();
    ta.ema(startIdx, endIdx, closedPrices, 7, outBegIdx, outNBElemnt, outEMA);
    ta.sma(startIdx, endIdx, closedPrices, 9, outBegIdx, outNBElemnt, outSMA);
    EmaSma[] points = new EmaSma[closedPrices.length];
    for (int i = 0; i < closedPrices.length; i++) {
      points[i] = EmaSma.builder().ema(Double.parseDouble(getTruncatedDouble(outEMA[i])))
          .sma(Double.parseDouble(getTruncatedDouble(outSMA[i]))).build();
    }
    return points;
  }

  private String getTruncatedDouble(double outEMA) {
    DECIMAL_FORMAT.setRoundingMode(RoundingMode.DOWN);
    String format = DECIMAL_FORMAT.format(outEMA);
    return format;
  }


}
