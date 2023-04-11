package com.rostyslav.trading.bot.strategy;

import static com.rostyslav.trading.bot.service.order.LastOrderSide.BUY;
import static com.rostyslav.trading.bot.service.order.LastOrderSide.SELL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rostyslav.trading.bot.eventHandler.CandleEventHandler;
import com.rostyslav.trading.bot.notifier.TelegramNotifier;
import com.rostyslav.trading.bot.service.ClosedCandlesQueue;
import com.rostyslav.trading.bot.service.OrderService;
import com.rostyslav.trading.bot.service.PriceProfitCalculator;
import com.rostyslav.trading.bot.service.indicator.calculator.StochacticCalculator;
import com.rostyslav.trading.bot.service.order.LastOrderSide;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StochRsiStrategy implements TradingStrategy {

  private static final Integer OVERBOUGHT_RSI = 90;

  private static final Integer OVERSELL_RSI = 10;

  private final String symbol;

  private final Integer closedCandlesSizeThreshold;

  private final AtomicBoolean isInPosition;

  private final OrderService orderService;

  private final ObjectMapper objectMapper;

  private final StochacticCalculator stochacticCalculator;

  private final PriceProfitCalculator priceProfitCalculator = new PriceProfitCalculator();
  private final TelegramNotifier telegramNotifier;
  AtomicReference<Double> atomicLastBuyPrice = new AtomicReference<>(-1D);
  AtomicReference<Double> atomicLastSellPrice = new AtomicReference<>(-1D);
  private ClosedCandlesQueue closedCandlesQueue;
  private boolean coldStart = true;
  private LastOrderSide lastOrderSide;

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
    log.info("Received event: {}", event);
    orderService.syncLastMadeOrder(coldStart, symbol, atomicLastBuyPrice, atomicLastSellPrice,
        lastOrderSide);
    CandleEventHandler candleEventHandler = new CandleEventHandler(event, objectMapper);
    Map<String, Object> candleEvent = candleEventHandler.getCandleEvent();
    LinkedHashMap candle = candleEventHandler.getCandle(candleEvent);
    closedCandlesQueue.add(candle);
    LinkedList<Double> closedCandlePrices = closedCandlesQueue.getClosedCandlePrices();
    if (closedCandlePrices.size() > closedCandlesSizeThreshold) {
      log.debug("Closed candles passed rsi period threshold.");
      stochacticCalculator.calculate(closedCandlesQueue.getClosedCandlePrisesArray());
      double[] rsi = stochacticCalculator.getFastK();
      log.debug("RSI calculations: {}, time: {}", rsi, LocalTime.now());
      Double lastRsi = rsi[rsi.length - 1];
      Double lastClosedCandlePrise = closedCandlePrices.getLast();
      if (lastRsi != null && lastRsi >= OVERBOUGHT_RSI && !isInPosition.get()
          && atomicLastBuyPrice.get() < lastClosedCandlePrise && lastOrderSide != SELL) {
        log.debug("OVERBOUGHT RSI position, rsi: {}, closed candle {}", lastRsi,
            lastClosedCandlePrise);
        try {
          orderService.sell(symbol, "BTC", lastClosedCandlePrise.toString());
          CompletableFuture.runAsync(
              () -> telegramNotifier.notify(String.format("Sold for %s", lastClosedCandlePrise)));
          log.debug("Sell with price {}", lastClosedCandlePrise);
          atomicLastBuyPrice.set(0D);
          atomicLastSellPrice.set(lastClosedCandlePrise);
          lastOrderSide = SELL;
        } catch (Exception e) {
          log.error("Exception during selling asset: {}", e.getMessage());
        }
      }
      if (lastRsi != null && lastRsi <= OVERSELL_RSI
          && !isInPosition.get()
          && lastClosedCandlePrise < atomicLastSellPrice.get()
          && lastOrderSide != BUY) {
        log.debug("OVERSELL RSI position, rsi: {}, closed candle {} ", lastRsi,
            lastClosedCandlePrise);
        try {
          orderService.buy(symbol, "USDT", lastClosedCandlePrise.toString());
          CompletableFuture.runAsync(
              () -> telegramNotifier.notify(String.format("Bought for %s", lastClosedCandlePrise)));
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

  private void extracted1(Double lastClosedCandlePrise, double buyProfitPercentage) {
    if (BUY.equals(lastOrderSide) && buyProfitPercentage > 5) {
      CompletableFuture.runAsync(() -> telegramNotifier.notify(
          String.format("Price {} falls for {} from the last buy {} operation.",
              lastClosedCandlePrise,
              buyProfitPercentage,
              atomicLastBuyPrice.get())));
    }
  }

  private void extracted(Double lastClosedCandlePrise, double sellProfitPercentage) {
    if (SELL.equals(lastOrderSide) && sellProfitPercentage > 5) {
      CompletableFuture.runAsync(() -> telegramNotifier.notify(
          String.format("Price {} grows for {} from the last sell {} operation.",
              lastClosedCandlePrise,
              sellProfitPercentage,
              atomicLastSellPrice.get())));
    }
  }
}
