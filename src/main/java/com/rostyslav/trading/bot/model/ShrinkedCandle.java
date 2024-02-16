package com.rostyslav.trading.bot.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShrinkedCandle {

    private Long openTime;

    private Double closedPrice;
}
