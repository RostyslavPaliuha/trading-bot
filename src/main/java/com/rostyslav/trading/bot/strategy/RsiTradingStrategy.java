package com.rostyslav.trading.bot.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rostyslav.trading.bot.eventHandler.CandleEventHandler;
import com.rostyslav.trading.bot.service.ClosedCandlesQueue;
import com.rostyslav.trading.bot.service.OrderService;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class RsiTradingStrategy implements TradingStrategy {

    private static final Integer OVERBOUGHT_RSI = 70;

    private static final Integer OVERSELL_RSI = 30;

    private final String symbol;

    private final Integer rsiPeriod;

    private final AtomicBoolean isInPosition;

    private final OrderService orderService;

    private ClosedCandlesQueue closedCandlesQueue;

    private ObjectMapper objectMapper;

    public RsiTradingStrategy(String symbol,
                              ClosedCandlesQueue closedCandlesQueue,
                              ObjectMapper objectMapper,
                              Integer rsiPeriod,
                              OrderService orderService,
                              AtomicBoolean isInPosition) {
        this.symbol = symbol;
        this.closedCandlesQueue = closedCandlesQueue;
        this.objectMapper = objectMapper;
        this.rsiPeriod = rsiPeriod;
        this.orderService = orderService;
        this.isInPosition = isInPosition;
    }

    @Override
    public void apply(String event) {
        log.debug("Applied {} for event: {}", this.getClass().getName(), event);
        CandleEventHandler candleEventHandler = new CandleEventHandler(event, objectMapper);
        Map<String, Object> candleEvent = candleEventHandler.getCandleEvent();
        LinkedHashMap candle = candleEventHandler.getCandle(candleEvent);
        closedCandlesQueue.add(candle);
        LinkedList<Double> closedCandlePrices = closedCandlesQueue.getClosedCandlePrices();
        if (closedCandlePrices.size() > rsiPeriod) {

            log.debug("Closed candles passed rsi period threshold.");
            double[] rsi = calculateRSI(getClosedCandlePrisesArray(closedCandlePrices), rsiPeriod);
            log.info("RSI calculations: {}, time: {}", rsi, LocalTime.now());
            Double lastRsi = rsi[rsi.length - 1];

            Double lastClosedCandlePrise = closedCandlePrices.getLast();
            if (lastRsi != null && lastRsi >= OVERBOUGHT_RSI && !isInPosition.get()) {
                log.info("OVERBOUGHT RSI position, rsi: {}, closed candle {} ", lastRsi, lastClosedCandlePrise);
                try {
                    orderService.sell(symbol, "BTC",lastClosedCandlePrise.toString());
                } catch (Exception e) {
                    log.error("Exception during selling asset: {}", e.getMessage());
                }
            }
            if (lastRsi != null && lastRsi <= OVERSELL_RSI && !isInPosition.get()) {
                log.info("OVERSELL RSI position, rsi: {}, closed candle {} ", lastRsi, lastClosedCandlePrise);
                try {
                    orderService.buy(symbol,"BTC", lastClosedCandlePrise.toString());
                } catch (Exception e) {
                    log.error("Exception during buying asset: {}", e.getMessage());
                }
            }
        }
    }

    private double[] getClosedCandlePrisesArray(List<Double> closedCandlePrices) {
        return closedCandlePrices.stream().mapToDouble(value -> value).toArray();
    }

    private double[] calculateRSI(double[] prices, int period) {
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
