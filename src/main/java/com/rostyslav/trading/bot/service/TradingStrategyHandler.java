package com.rostyslav.trading.bot.service;

import com.binance.connector.client.utils.WebSocketCallback;
import com.rostyslav.trading.bot.strategy.TradingStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class TradingStrategyHandler implements WebSocketCallback {

    private final List<TradingStrategy> strategies;

    public TradingStrategyHandler(List<TradingStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public void onReceive(String data) {
        log.debug("Received event: {}", data);
        strategies.forEach(tradingStrategy -> tradingStrategy.apply(data));
    }
}
