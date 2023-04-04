package com.rostyslav.trading.bot.strategy.ma;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rostyslav.trading.bot.model.Candle;
import com.rostyslav.trading.bot.model.input.socket.Event;
import com.rostyslav.trading.bot.model.input.socket.EventCandle;
import com.rostyslav.trading.bot.service.candle.CandleService;
import com.rostyslav.trading.bot.service.candle.CandleWebClientResponse;
import com.rostyslav.trading.bot.service.event.EventService;
import com.rostyslav.trading.bot.service.indicator.calculator.ma.EMaSmaCalculator;
import com.rostyslav.trading.bot.service.indicator.calculator.ma.EmaSma;
import com.rostyslav.trading.bot.strategy.TradingStrategy;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class EmaSmaTradingStrategy implements TradingStrategy {

  private final EMaSmaCalculator calculator;

  private final ObjectMapper objectMapper;

  private final CandleService candleService;
  private final EventService eventService;
  private final LinkedList<Candle> candles = new LinkedList<>();
  private boolean coldStart = true;

  public EmaSmaTradingStrategy(EMaSmaCalculator calculator,
      ObjectMapper objectMapper,
      CandleService candleService, EventService eventService) {
    this.calculator = calculator;
    this.objectMapper = objectMapper;
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
    EmaSma[] calculate = calculator.calculate(candlesClosedPrice);

    EmaSma current = calculate[0];
    EmaSma previous = calculate[1];
    EmaSma previous2 = calculate[2];
    //if current ema >= current sma && previous ema < previous sma
    if (current.getEma() > current.getSma()
        && previous.getEma().equals( previous.getSma())
        && previous2.getEma()< previous2.getSma()) {
      log.info("ema {} crosses sma {}, buy position fixed.", current.getEma(), current.getSma());
    }
    if (current.getEma() < current.getSma() && previous.getEma() > previous.getSma()) {
      log.info("sma {} crosses ema {}, sell position fixed.", current.getEma(), current.getSma());
    }
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
