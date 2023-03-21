package com.rostyslav.trading.bot.service.indicator.calculator;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

public class StochacticCalculator {

    private final Core ta;

    private final int rsiPeriod;

    private final int fastKPeriod;

    private final int fastDPeriod;

    private double[] rsi;

    private double[] fastK;

    private double[] fastD;

    public StochacticCalculator(int rsiPeriod, int fastKPeriod, int fastDPeriod) {
        ta = new Core();
        this.fastKPeriod = fastKPeriod;
        this.fastDPeriod = fastDPeriod;
        this.rsiPeriod = rsiPeriod;
    }

    public void calculate(double[] prices) {
        int lookback = ta.stochRsiLookback(rsiPeriod, fastKPeriod, fastDPeriod, MAType.Sma);
        int startIdx = lookback - 1;
        int endIdx = prices.length - 1;

        rsi = new double[prices.length - lookback];
        fastK = new double[rsi.length];
        fastD = new double[rsi.length];

        MInteger outBegIdx = new MInteger();
        MInteger outNBElemnt = new MInteger();
        RetCode ret = ta.stochRsi(startIdx, endIdx, prices, rsiPeriod, fastKPeriod, fastDPeriod, MAType.Sma, outBegIdx, outNBElemnt, fastK, fastD);
        if (ret != RetCode.Success) {
            throw new IllegalArgumentException("Could not calculate Stochastic RSI: " + ret);
        }
    }

    public double[] getRSI() {
        return rsi;
    }

    public double[] getFastK() {
        return fastK;
    }

    public double[] getFastD() {
        return fastD;
    }
}

