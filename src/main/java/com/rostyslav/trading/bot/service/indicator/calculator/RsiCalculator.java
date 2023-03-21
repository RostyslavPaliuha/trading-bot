package com.rostyslav.trading.bot.service.indicator.calculator;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

public class RsiCalculator {

    public double[] calculateRSI(double[] prices, int period) {
        double[] output = new double[prices.length];
        double[] tempOutPut = new double[prices.length];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        RetCode retCode = RetCode.InternalError;
        begin.value = -1;
        length.value = -1;
        retCode = new Core().rsi(0, prices.length - 1, prices, period, begin, length, tempOutPut);
        for (int i = 0; i < period; i++) {
            output[i] = 0;
        }
        for (int i = period; 0 < i && i < (prices.length); i++) {
            output[i] = tempOutPut[i - period];
        }
        return output;
    }

}
