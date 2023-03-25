package com.rostyslav.trading.bot.service.event.consumer;

import com.binance.connector.client.WebsocketClient;
import com.rostyslav.trading.bot.service.TradingStrategyHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CandleEventConsumer {

    private final WebsocketClient websocketClient;

    private final String symbol;

    private final String timeFrame;

    private final TradingStrategyHandler strategyHandler;

    public CandleEventConsumer(WebsocketClient websocketClient,
                               String symbol,
                               String timeFrame,
                               TradingStrategyHandler strategyHandler) {
        this.websocketClient = websocketClient;
        this.symbol = symbol;
        this.timeFrame = timeFrame;
        this.strategyHandler = strategyHandler;
    }

    public void consume() {
        websocketClient.klineStream(symbol, timeFrame,
                OpenData -> {
                    log.info("Open callback data:" + OpenData);
                }, strategyHandler::onReceive,
                ClosingData -> {
                    log.info("Closing callback data:" + ClosingData);
                },
                FailureData -> {
                    log.info("Failure callback data:" + FailureData);
                });
    }

}
