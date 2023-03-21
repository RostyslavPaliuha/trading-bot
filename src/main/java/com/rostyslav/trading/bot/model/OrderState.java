package com.rostyslav.trading.bot.model;

import lombok.Data;

import java.time.Instant;

@Data
public class OrderState {

    private Instant orderCreationTime;

    private String symbol;

    private SIDE side;

    private Double assetQuantity;

    private Double price;
}
