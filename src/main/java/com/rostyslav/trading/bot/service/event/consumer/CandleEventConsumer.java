package com.rostyslav.trading.bot.service.event.consumer;

import com.binance.connector.client.WebSocketStreamClient;
import com.rostyslav.trading.bot.service.TradingStrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

@Slf4j
public class CandleEventConsumer {

    private final WebSocketStreamClient websocketClient;

    private final String symbol;

    private final String timeFrame;

    private final TradingStrategyHandler strategyHandler;

    public CandleEventConsumer(WebSocketStreamClient websocketClient, String symbol, String timeFrame, TradingStrategyHandler strategyHandler) {
        this.websocketClient = websocketClient;
        this.symbol = symbol;
        this.timeFrame = timeFrame;
        this.strategyHandler = strategyHandler;
    }

    public void consume() {
        websocketClient.klineStream(symbol, timeFrame, strategyHandler::onMessage);
    }

}
