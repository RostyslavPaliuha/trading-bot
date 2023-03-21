package com.rostyslav.trading.bot.strategy;

public interface TradingStrategy {
    //todo make decision based on strategy and return result
    void apply(String event);
}
