package com.rostyslav.trading.bot.strategy.dinamicPriceEvaluation;

import com.rostyslav.trading.bot.model.Candle;
import com.rostyslav.trading.bot.model.input.socket.Event;
import com.rostyslav.trading.bot.model.input.socket.EventCandle;
import com.rostyslav.trading.bot.service.candle.CandleService;
import com.rostyslav.trading.bot.service.candle.CandleWebClientResponse;
import com.rostyslav.trading.bot.service.event.EventService;
import com.rostyslav.trading.bot.service.indicator.calculator.ma.EmaSma;
import com.rostyslav.trading.bot.strategy.TradingStrategy;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class DynamicPriceEvaluation implements TradingStrategy {

  private final CandleService candleService;
  private final EventService eventService;
  private final LinkedList<Candle> candles = new LinkedList<>();
  private boolean coldStart = true;

  public DynamicPriceEvaluation(CandleService candleService,
      EventService eventService) {
    this.candleService = candleService;
    this.eventService = eventService;
  }

  @Override
  public void apply(String event) {
    log.trace("Applied {} for event: {}", this.getClass().getName(), event);
    Event deserializedEvent = eventService.getDeserializedEvent(event);
    EventCandle eventCandle = deserializedEvent.getCandle();
    Candle latestCandle = candleService.mapEventCandleToDomain(eventCandle);
    if (coldStart) {
      Long eventTime = deserializedEvent.getEventTime();
      long desiredLastCandleTime = eventTime - 1000L;
      candles.addAll(getPreviousCandlesSortedByTimeInDesc(desiredLastCandleTime));
      candles.add(latestCandle);
      coldStart = false;
    } else {
      candles.pollLast();
      candles.offerFirst(latestCandle);
    }
    double[] candlesClosedPrice = candleService.getCandlesClosedPrices(candles);

  }

  @NotNull
  private List<Candle> getPreviousCandlesSortedByTimeInDesc(long desiredLastCandleTime) {
    return candleService.getDeserializedCandles("BTCUSDT", desiredLastCandleTime, (byte) 100)
        .stream()
        .sorted(Comparator.comparingLong(CandleWebClientResponse::getOpenTime).reversed())
        .map(candleService::mapWebClientCandleToDomain)
        .collect(Collectors.toList());
  }
}
