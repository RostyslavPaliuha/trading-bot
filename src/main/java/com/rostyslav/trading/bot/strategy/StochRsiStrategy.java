package com.rostyslav.trading.bot.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rostyslav.trading.bot.eventHandler.CandleEventHandler;
import com.rostyslav.trading.bot.notifier.TelegramNotifier;
import com.rostyslav.trading.bot.service.ClosedCandlesQueue;
import com.rostyslav.trading.bot.service.OrderService;
import com.rostyslav.trading.bot.service.PriceProfitCalculator;
import com.rostyslav.trading.bot.service.indicator.calculator.StochacticCalculator;
import com.rostyslav.trading.bot.service.order.LastOrderSide;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.rostyslav.trading.bot.service.order.LastOrderSide.*;

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

    private final PriceProfitCalculator priceProfitCalculator = new PriceProfitCalculator();

    AtomicReference<Double> atomicLastBuyPrice = new AtomicReference<>(-1D);

    AtomicReference<Double> atomicLastSellPrice = new AtomicReference<>(-1D);

    private ClosedCandlesQueue closedCandlesQueue;

    private boolean coldStart = true;

    private LastOrderSide lastOrderSide;

    private final TelegramNotifier telegramNotifier;

    public StochRsiStrategy(String symbol,
                            ClosedCandlesQueue closedCandlesQueue,
                            ObjectMapper objectMapper,
                            Integer closedCandlesSizeThreshold,
                            OrderService orderService,
                            AtomicBoolean isInPosition,
                            StochacticCalculator stochacticCalculator,
                            TelegramNotifier telegramNotifier) {
        this.symbol = symbol;
        this.closedCandlesQueue = closedCandlesQueue;
        this.objectMapper = objectMapper;
        this.closedCandlesSizeThreshold = closedCandlesSizeThreshold;
        this.orderService = orderService;
        this.isInPosition = isInPosition;
        this.stochacticCalculator = stochacticCalculator;
        this.telegramNotifier = telegramNotifier;
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
        orderService.syncLastMadeOrder(coldStart, symbol, atomicLastBuyPrice, atomicLastSellPrice, lastOrderSide);
        log.debug("Applied {} for event: {}", this.getClass().getName(), event);
        CandleEventHandler candleEventHandler = new CandleEventHandler(event, objectMapper);
        Map<String, Object> candleEvent = candleEventHandler.getCandleEvent();
        LinkedHashMap candle = candleEventHandler.getCandle(candleEvent);
        closedCandlesQueue.add(candle);
        LinkedList<Double> closedCandlePrices = closedCandlesQueue.getClosedCandlePrices();
        // todo for minute and bigger time frames use warm up strategy to generate closed price candles data


        if (closedCandlePrices.size() > closedCandlesSizeThreshold) {
            log.debug("Closed candles passed rsi period threshold.");
            stochacticCalculator.calculate(closedCandlesQueue.getClosedCandlePrisesArray());
            double[] rsi = stochacticCalculator.getFastK();
            log.debug("RSI calculations: {}, time: {}", rsi, LocalTime.now());
            Double lastRsi = rsi[rsi.length - 1];
            Double lastClosedCandlePrise = closedCandlePrices.getLast();
            double sellProfitPercentage = priceProfitCalculator.getSellProfitPercentage(lastClosedCandlePrise, atomicLastBuyPrice.get());
            if (SELL.equals(lastOrderSide) && sellProfitPercentage > 5) {
                CompletableFuture.runAsync(() -> telegramNotifier.notify(String.format("Price {} grows for {} from the last sell {} operation.",
                        lastClosedCandlePrise,
                        sellProfitPercentage,
                        atomicLastSellPrice.get())));
            }
            if (lastRsi != null && lastRsi >= OVERBOUGHT_RSI && !isInPosition.get() && sellProfitPercentage >= 1 && lastOrderSide != SELL) {
                log.debug("OVERBOUGHT RSI position, rsi: {}, closed candle {}", lastRsi, lastClosedCandlePrise);
                try {
                    orderService.sell(symbol, "BTC", lastClosedCandlePrise.toString());
                    CompletableFuture.runAsync(() -> telegramNotifier.notify(String.format("Sold for {}", lastClosedCandlePrise)));
                    log.debug("Sell with profit percentage {}, with price {}", sellProfitPercentage, lastClosedCandlePrise);
                    atomicLastBuyPrice.set(0D);
                    atomicLastSellPrice.set(lastClosedCandlePrise);
                    lastOrderSide = SELL;
                } catch (Exception e) {
                    log.error("Exception during selling asset: {}", e.getMessage());
                }
            }
            double buyProfitPercentage = priceProfitCalculator.getBuyProfitPercentage(lastClosedCandlePrise, atomicLastSellPrice.get());
            if (BUY.equals(lastOrderSide) && buyProfitPercentage > 1) {
                CompletableFuture.runAsync(() -> telegramNotifier.notify(String.format("Price {} falls for {} from the last buy {} operation.",
                        lastClosedCandlePrise,
                        buyProfitPercentage,
                        atomicLastBuyPrice.get())));
            }
            if (lastRsi != null && lastRsi <= OVERSELL_RSI && !isInPosition.get() && buyProfitPercentage >= 5 && lastOrderSide != BUY) {
                log.debug("OVERSELL RSI position, rsi: {}, closed candle {} ", lastRsi, lastClosedCandlePrise);
                try {
                    orderService.buy(symbol, "USDT", lastClosedCandlePrise.toString());
                    CompletableFuture.runAsync(() -> telegramNotifier.notify(String.format("Bought for {}", lastClosedCandlePrise)));
                    log.debug("Buy with price {}", lastClosedCandlePrise.toString());
                    atomicLastBuyPrice.set(lastClosedCandlePrise);
                    atomicLastSellPrice.set(0D);
                    lastOrderSide = BUY;
                } catch (Exception e) {
                    log.error("Exception during buying asset: {}", e.getMessage());
                }
            }
        }
    }
}
