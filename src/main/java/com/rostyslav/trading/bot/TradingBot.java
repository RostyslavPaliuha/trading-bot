package com.rostyslav.trading.bot;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.WebsocketClient;
import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.impl.WebsocketClientImpl;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rostyslav.trading.bot.configuration.PrivateConfig;
import com.rostyslav.trading.bot.notifier.TelegramNotifier;
import com.rostyslav.trading.bot.service.OrderService;
import com.rostyslav.trading.bot.service.TradingStrategyHandler;
import com.rostyslav.trading.bot.service.candle.CandleService;
import com.rostyslav.trading.bot.service.event.EventService;
import com.rostyslav.trading.bot.service.event.consumer.CandleEventConsumer;
import com.rostyslav.trading.bot.service.event.consumer.OrderUpdatesEventConsumer;
import com.rostyslav.trading.bot.service.indicator.calculator.RsiCalculator;
import com.rostyslav.trading.bot.strategy.StochRsiStrategy;
import com.rostyslav.trading.bot.strategy.TradingStrategy;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TradingBot {

  public static final String BTCUSDT = "BTCUSDT";

  public static final String TIME_FRAME_1MIN = "1m";

  public static final String TIME_FRAME_1SEC = "1s";

  private static final Integer RSI_PERIOD = 320;

  private final ObjectMapper objectMapper;

  private final TradingStrategyHandler strategyHandler;

  private final TradingStrategy rsiTradingStrategy;

  private final WebsocketClient websocketClient;

  private final SpotClient spotClient;

  private final AtomicBoolean isInPosition = new AtomicBoolean(false);

  private final OrderService orderService;

  private final CandleEventConsumer candleEventConsumer;

  private final OrderUpdatesEventConsumer orderUpdatesEventConsumer;


  public TradingBot() {
    this.spotClient = new SpotClientImpl(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY);
    this.websocketClient = new WebsocketClientImpl();
    this.objectMapper = new ObjectMapper();
    this.objectMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
    this.orderService = new OrderService(objectMapper, spotClient);
    this.rsiTradingStrategy = new StochRsiStrategy(BTCUSDT,
        objectMapper,
        RSI_PERIOD,
        orderService,
        isInPosition,
        new RsiCalculator(),
        new TelegramNotifier(),
        new CandleService(spotClient, objectMapper),
        new EventService(objectMapper));
    this.strategyHandler = new TradingStrategyHandler(List.of(rsiTradingStrategy));
    this.orderUpdatesEventConsumer = new OrderUpdatesEventConsumer(websocketClient,
        objectMapper,
        isInPosition,
        spotClient);
    this.candleEventConsumer = new CandleEventConsumer(websocketClient,
        BTCUSDT,
        TIME_FRAME_1SEC,
        strategyHandler);
  }

  public void run() {
    isInPosition.set(orderService.hasOpenOrders());
    orderService.checkIfOrderFilled(isInPosition);
    orderUpdatesEventConsumer.consume();
    candleEventConsumer.consume();
  }
}
