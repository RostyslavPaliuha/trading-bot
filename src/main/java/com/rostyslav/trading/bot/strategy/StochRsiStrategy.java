package com.rostyslav.trading.bot.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rostyslav.trading.bot.eventHandler.CandleEventHandler;
import com.rostyslav.trading.bot.service.ClosedCandlesQueue;
import com.rostyslav.trading.bot.service.OrderService;
import com.rostyslav.trading.bot.service.indicator.calculator.StochacticCalculator;
import com.rostyslav.trading.bot.service.order.HistoricalOrder;
import com.rostyslav.trading.bot.service.order.LastOrderSide;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class StochRsiStrategy implements TradingStrategy {

    private static final Integer OVERBOUGHT_RSI = 70;

    private static final Integer OVERSELL_RSI = 30;

    private final String symbol;

    private final Integer closedCandlesSizeThreshold;

    private final AtomicBoolean isInPosition;

    private final OrderService orderService;

    private final ObjectMapper objectMapper;

    private final StochacticCalculator stochacticCalculator;

    AtomicReference<Double> atomicLastBuyPrice = new AtomicReference<>(-1D);

    private ClosedCandlesQueue closedCandlesQueue;

    private boolean coldStart = true;

    private LastOrderSide lastOrderSide;

    public StochRsiStrategy(String symbol,
                            ClosedCandlesQueue closedCandlesQueue,
                            ObjectMapper objectMapper,
                            Integer closedCandlesSizeThreshold,
                            OrderService orderService,
                            AtomicBoolean isInPosition,
                            StochacticCalculator stochacticCalculator) {
        this.symbol = symbol;
        this.closedCandlesQueue = closedCandlesQueue;
        this.objectMapper = objectMapper;
        this.closedCandlesSizeThreshold = closedCandlesSizeThreshold;
        this.orderService = orderService;
        this.isInPosition = isInPosition;
        this.stochacticCalculator = stochacticCalculator;
    }

    @Override
    public void apply(String event) {
        /*
        *apply bollinger bands, ma, rsi
        check if we have open orders
        check last buy price is last order was buy
        if we have last buy price create chank order for sell with 1% price difference
        make new orders only with 25% of balance
        max order amount 4
        check if there are free assets and order amount
        if we buy on high then wait for 1 min if price don`t go beck create new buy on current price if
        * */
        syncLastMadeOrder();
        log.debug("Applied {} for event: {}", this.getClass().getName(), event);
        CandleEventHandler candleEventHandler = new CandleEventHandler(event, objectMapper);
        Map<String, Object> candleEvent = candleEventHandler.getCandleEvent();
        LinkedHashMap candle = candleEventHandler.getCandle(candleEvent);
        closedCandlesQueue.add(candle);
        LinkedList<Double> closedCandlePrices = closedCandlesQueue.getClosedCandlePrices();
        if (closedCandlePrices.size() > closedCandlesSizeThreshold) {
            log.debug("Closed candles passed rsi period threshold.");
            stochacticCalculator.calculate(getClosedCandlePrisesArray(closedCandlePrices));
            double[] rsi = stochacticCalculator.getFastK();
            log.info("RSI calculations: {}, time: {}", rsi, LocalTime.now());
            Double lastRsi = rsi[rsi.length - 1];
            Double lastClosedCandlePrise = closedCandlePrices.getLast();
            Double lastBuyPrice = atomicLastBuyPrice.get();
            double profitPercentage = getProfitPercentage(lastClosedCandlePrise, lastBuyPrice);
            if (lastRsi != null && lastRsi >= OVERBOUGHT_RSI && !isInPosition.get() && profitPercentage >= 0.01 && lastOrderSide != LastOrderSide.SELL) {
                log.debug("OVERBOUGHT RSI position, rsi: {}, closed candle {} , lastBuyPrise {}", lastRsi, lastClosedCandlePrise, lastBuyPrice);
                try {
                    orderService.sell(symbol, "BTC", lastClosedCandlePrise.toString());
                    log.info("Sell with profit percentage {}, with price {}", profitPercentage, lastClosedCandlePrise);
                    atomicLastBuyPrice.set(0D);
                    lastOrderSide = LastOrderSide.SELL;
                } catch (Exception e) {
                    log.error("Exception during selling asset: {}", e.getMessage());
                }
            }
            if (lastRsi != null && lastRsi <= OVERSELL_RSI && !isInPosition.get() && lastOrderSide != LastOrderSide.BUY) {
                log.debug("OVERSELL RSI position, rsi: {}, closed candle {} ", lastRsi, lastClosedCandlePrise);
                try {
                    orderService.buy(symbol, "USDT", lastClosedCandlePrise.toString());
                    log.info("Buy with price {}", lastClosedCandlePrise.toString());
                    atomicLastBuyPrice.set(lastClosedCandlePrise);
                    lastOrderSide = LastOrderSide.BUY;
                } catch (Exception e) {
                    log.error("Exception during buying asset: {}", e.getMessage());
                }
            }
        }
    }

    private void syncLastMadeOrder() {
        if (coldStart) {
            HistoricalOrder lastOrder = orderService.getLastOrder("BTCUSDT");
            String side = lastOrder.getSide();
            String filled = lastOrder.getStatus();
            if ("BUY".equals(side) && "FILLED".equals(filled)) {
                atomicLastBuyPrice.set(lastOrder.getPrice());
                lastOrderSide = LastOrderSide.BUY;
            } else if ("SELL".equals(side) && "FILLED".equals(filled)) {
                lastOrderSide = LastOrderSide.SELL;
            }
            coldStart = false;
        }
    }

    private double getProfitPercentage(Double lastClosedCandlePrise, Double lastBuyPrice) {
        if (lastClosedCandlePrise - lastBuyPrice < 0) {
            return 0D;
        }
        return (Math.abs(lastBuyPrice - lastClosedCandlePrise) / lastClosedCandlePrise) * 100;
    }


    private double[] getClosedCandlePrisesArray(List<Double> closedCandlePrices) {
        return closedCandlePrices.stream().mapToDouble(value -> value).toArray();
    }
}
