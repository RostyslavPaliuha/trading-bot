package com.rostyslav.trading.bot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"openTime", "openPrice", "highPrice", "lowPrice", "closedPrice", "volume", "closeTime"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class CandleWebClientResponse {

    private Long openTime;

    private Double openPrice;

    private Double highPrice;

    private Double lowPrice;

    private Double closedPrice;

    private Double volume;

    private Long closeTime;
}

