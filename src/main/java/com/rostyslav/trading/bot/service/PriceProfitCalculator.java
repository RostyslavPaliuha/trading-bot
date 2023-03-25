package com.rostyslav.trading.bot.service;

public class PriceProfitCalculator {

    public PriceProfitCalculator() {
    }

    public double getSellProfitPercentage(Double lastClosedCandlePrise, Double lastBuyPrice) {
        if (lastClosedCandlePrise - lastBuyPrice < 0) {
            return 0D;
        }
        return (Math.abs(lastBuyPrice - lastClosedCandlePrise) / lastClosedCandlePrise) * 100;
    }

    public double getBuyProfitPercentage(Double lastClosedCandlePrise, Double lastSellPrice) {
        if (lastClosedCandlePrise - lastSellPrice < 0) {
            return (Math.abs(lastSellPrice - lastClosedCandlePrise) / lastClosedCandlePrise) * 100;
        }
        return 0D;
    }
}