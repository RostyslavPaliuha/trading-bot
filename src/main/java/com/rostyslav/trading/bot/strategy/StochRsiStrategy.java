package com.rostyslav.trading.bot.strategy;

import com.rostyslav.trading.bot.model.ShrinkedCandle;
import com.rostyslav.trading.bot.model.input.socket.Event;
import com.rostyslav.trading.bot.model.input.socket.EventCandle;
import com.rostyslav.trading.bot.notifier.TelegramNotifier;
import com.rostyslav.trading.bot.service.OrderService;
import com.rostyslav.trading.bot.service.candle.CandleService;
import com.rostyslav.trading.bot.service.event.EventService;
import com.rostyslav.trading.bot.service.indicator.calculator.RsiCalculator;
import com.rostyslav.trading.bot.service.order.LastOrderSide;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.rostyslav.trading.bot.service.order.LastOrderSide.BUY;
import static com.rostyslav.trading.bot.service.order.LastOrderSide.SELL;

@Slf4j
public class StochRsiStrategy implements TradingStrategy {

    private static final Integer OVERBOUGHT_RSI = 90;

    private static final Integer OVERSELL_RSI = 10;

    private final String symbol;

    private final Integer closedCandlesSizeThreshold;

    private final AtomicBoolean isInPosition;

    private final OrderService orderService;

    private final RsiCalculator rsiCalculator;

    private final TelegramNotifier telegramNotifier;

    private final CandleService candleService;

    private final EventService eventService;

    private final LinkedList<ShrinkedCandle> candles = new LinkedList<>();

    private final AtomicReference<LastOrderSide> lastOrderSide = new AtomicReference<>();

    AtomicReference<Double> atomicLastBuyPrice = new AtomicReference<>(-1D);

    AtomicReference<Double> atomicLastSellPrice = new AtomicReference<>(-1D);

    private boolean coldStart = true;

    public StochRsiStrategy(String symbol, Integer closedCandlesSizeThreshold, OrderService orderService, AtomicBoolean isInPosition, RsiCalculator rsiCalculator, TelegramNotifier telegramNotifier, CandleService candleService, EventService eventService) {
        this.symbol = symbol;
        this.closedCandlesSizeThreshold = closedCandlesSizeThreshold;
        this.orderService = orderService;
        this.isInPosition = isInPosition;
        this.rsiCalculator = rsiCalculator;
        this.telegramNotifier = telegramNotifier;
        this.candleService = candleService;
        this.eventService = eventService;
    }

    @Override
    public void apply(String event) {
        orderService.syncLastMadeOrder(coldStart, symbol, atomicLastBuyPrice, atomicLastSellPrice, lastOrderSide);
        Event deserializedEvent = eventService.getDeserializedEvent(event);
        EventCandle eventCandle = getCandle(deserializedEvent);
        ShrinkedCandle currentCandle = candleService.mapEventCandleToDomain(eventCandle);
        fillCandleQuee(deserializedEvent, currentCandle);
        double[] candlesPrices = getClosedCandlesPrices();
        Double rsi = getRSI(candlesPrices);
        Double currentPrice = currentCandle.getClosedPrice();
        log.debug("Current candle price: {} , rsi: {}", currentPrice, rsi);
        double profitPercentage = getProfitPercentage(currentPrice, atomicLastBuyPrice.get());
        if (rsi != null && rsi >= OVERBOUGHT_RSI && !isInPosition.get() && profitPercentage > 0.3 && atomicLastBuyPrice.get() < currentPrice && !lastOrderSide.get().equals(SELL)) {
            // flag check for price increase
            //save current price and check price movement up
            //if rsi first time moves below overbought then sell
            sell(currentPrice);
            //wait/skip for 10sec
        }
        if (rsi != null && rsi <= OVERSELL_RSI && !isInPosition.get() && !lastOrderSide.get().equals(BUY)) {
            //the same here
            // flag check for price reduce
            //save current price and check price reduce
            //if rsi first time moves above oversell then buy
            buy(currentPrice);
            //wait/skip for 10sec
        }
    }

    private double getProfitPercentage(Double lastClosedCandlePrise, Double lastBuyPrice) {
        if (lastClosedCandlePrise - lastBuyPrice < 0) {
            return 0D;
        }
        return (Math.abs(lastBuyPrice - lastClosedCandlePrise) / lastClosedCandlePrise) * 100;
    }

    private void buy(Double currentPrice) {
        try {
            orderService.buy(symbol, "USDT", currentPrice.toString());
            CompletableFuture.runAsync(() -> telegramNotifier.notify(String.format("Bought for %s", currentPrice)));
            log.debug("Buy with price {}", currentPrice);
            atomicLastBuyPrice.set(currentPrice);
            atomicLastSellPrice.set(0D);
            lastOrderSide.set(BUY);
        } catch (Exception e) {
            log.error("Exception during buying asset: {}", e.getMessage());
        }
    }

    private void sell(Double currentPrice) {
        try {
            orderService.sell(symbol, "BTC", currentPrice.toString());
            CompletableFuture.runAsync(() -> telegramNotifier.notify(String.format("Sold for %s", currentPrice)));
            log.debug("Sell with price {}", currentPrice);
            atomicLastBuyPrice.set(0D);
            atomicLastSellPrice.set(currentPrice);
            lastOrderSide.set(SELL);
        } catch (Exception e) {
            log.error("Exception during selling asset: {}", e.getMessage());
        }
    }


    private Double getRSI(double[] candlesPrices) {
        double[] rsi = rsiCalculator.calculateRSI(candlesPrices, 30);
        Double lastRsi = rsi[rsi.length - 1];
        return lastRsi;
    }

    private double[] getClosedCandlesPrices() {
        return candles.stream()
                      .map(candle -> candle.getClosedPrice())
                      .mapToDouble(value -> value)
                      .toArray();
    }

    private EventCandle getCandle(Event deserializedEvent) {
        EventCandle eventCandle = deserializedEvent.getCandle();
        return eventCandle;
    }

    private void fillCandleQuee(Event deserializedEvent, ShrinkedCandle latestCandle) {
        if (coldStart) {
            Long eventTime = deserializedEvent.getEventTime();
            long desiredLastCandleTime = eventTime - 1000L;
            candles.addAll(getPreviousCandlesSortedByTimeInDesc(desiredLastCandleTime));
            candles.add(latestCandle);
            coldStart = false;
        } else {
            candles.pollFirst();
            candles.offerLast(latestCandle);
        }
    }


    private List<ShrinkedCandle> getPreviousCandlesSortedByTimeInDesc(long desiredLastCandleTime) {
        return candleService.getDeserializedCandles("BTCUSDT", "1s",desiredLastCandleTime, (short) 120)
                            .stream()
                            .map(candleService::mapWebClientCandleToDomain)
                            .collect(Collectors.toList());
    }
}
