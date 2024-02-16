package com.rostyslav.trading.bot.service.candle;

import com.binance.connector.client.SpotClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rostyslav.trading.bot.dto.CandleWebClientResponse;
import com.rostyslav.trading.bot.model.ShrinkedCandle;
import com.rostyslav.trading.bot.model.input.socket.EventCandle;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class CandleService {

    private final SpotClient spotClient;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public LinkedList<CandleWebClientResponse> getDeserializedCandles(String symbol, String timeFrame, long endTime, short limit) {
        LinkedHashMap hashMap = new LinkedHashMap();
        hashMap.put("symbol", symbol);
        hashMap.put("interval", timeFrame);
        hashMap.put("endTime", endTime);
        hashMap.put("limit", limit);
        String candles = spotClient.createMarket().klines(hashMap);
        return objectMapper.readValue(candles, new TypeReference<LinkedList<CandleWebClientResponse>>() {});

    }

    public long getEventTime(Map candle) {
        return Long.parseLong((String) candle.get("E"));
    }

    public double[] getCandlesClosedPrices(List<ShrinkedCandle> candles) {
        return candles.stream()
                      .mapToDouble(ShrinkedCandle::getClosedPrice)
                      .toArray();
    }

    @SneakyThrows
    public LinkedHashMap getCandle(String event) {
        Map<String, Object> candleEvent = objectMapper.readValue(event, new TypeReference<LinkedHashMap<String, Object>>() {});
        return (LinkedHashMap) candleEvent.get("k");
    }

    public ShrinkedCandle mapEventCandleToDomain(EventCandle eventCandle) {
        return ShrinkedCandle.builder()
                             .openTime(eventCandle.getOpenTime())
                             .closedPrice(eventCandle.getClosedPrice())
                             .build();
    }

    public ShrinkedCandle mapWebClientCandleToDomain(CandleWebClientResponse clientResponse) {
        return ShrinkedCandle.builder()
                             .openTime(clientResponse.getOpenTime())
                             .closedPrice(clientResponse.getClosedPrice())
                             .build();
    }
}
