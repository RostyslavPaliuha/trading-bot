package com.rostyslav.trading.bot.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.LinkedList;

@Slf4j
@Data
public class ClosedCandlesQueue {

    private LinkedList<Double> closedCandlePrices = new LinkedList<>();

    private int queueCapacity;

    public ClosedCandlesQueue(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public void add(LinkedHashMap candle) {
        boolean isCandleClosed = isCandleClosed(candle);
        Double closedCandlePrise = Double.valueOf((String) candle.get("c"));
        if (isCandleClosed) {
            log.debug("Added closed candle: {}", candle);

            if (closedCandlePrices.size() > queueCapacity) {
                closedCandlePrices.pollFirst();
                closedCandlePrices.offerLast(closedCandlePrise);
            } else {
                closedCandlePrices.add(closedCandlePrise);
            }
        }
    }

    private boolean isCandleClosed(LinkedHashMap candle) {
        return (boolean) candle.get("x");
    }

    public double[] getClosedCandlePrisesArray() {
        return closedCandlePrices.stream().mapToDouble(value -> value).toArray();
    }
}
