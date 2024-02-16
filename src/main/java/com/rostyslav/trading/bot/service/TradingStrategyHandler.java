package com.rostyslav.trading.bot.service;

import com.binance.connector.client.utils.websocketcallback.WebSocketMessageCallback;
import com.rostyslav.trading.bot.strategy.TradingStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class TradingStrategyHandler implements WebSocketMessageCallback {

    private final List<TradingStrategy> strategies;

    public TradingStrategyHandler(List<TradingStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public void onMessage(String data) {
        log.trace("Received event: {}", data);
        strategies.forEach(tradingStrategy -> tradingStrategy.apply(data));
    }
}
