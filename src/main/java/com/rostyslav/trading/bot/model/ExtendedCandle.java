package com.rostyslav.trading.bot.model;

import lombok.Data;

@Data
public class ExtendedCandle {

    private Long openTime;

    private Double openPrice;

    private Double highPrice;

    private Double lowPrice;

    private Double closedPrice;

    private Double volume;

    private Long closeTime;

    private Integer priceLabel;
}

