package com.rostyslav.trading.bot.service.candle;

import com.binance.connector.client.SpotClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rostyslav.trading.bot.model.Candle;
import com.rostyslav.trading.bot.model.input.socket.EventCandle;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;

public class CandleService {

  private final SpotClient spotClient;
  private final ObjectMapper objectMapper;

  public CandleService(SpotClient spotClient, ObjectMapper objectMapper) {
    this.spotClient = spotClient;
    this.objectMapper = objectMapper;
  }

  @SneakyThrows
  public LinkedList<CandleWebClientResponse> getDeserializedCandles(String symbol, long endTime,byte limit) {
    LinkedHashMap hashMap = new LinkedHashMap();
    hashMap.put("symbol", symbol);
    hashMap.put("interval", "1s");
    hashMap.put("endTime", endTime);
    hashMap.put("limit", limit);
    String candles = spotClient.createMarket().klines(hashMap);
    return objectMapper.readValue(candles,
        new TypeReference<LinkedList<CandleWebClientResponse>>() {
        });

  }

  public long getEventTime(Map candle) {
    return Long.parseLong((String) candle.get("E"));
  }

  public double[] getCandlesClosedPrices(List<Candle> candles) {
    return candles.stream()
        .mapToDouble(Candle::getClosedPrice)
        .toArray();
  }

  @SneakyThrows
  public LinkedHashMap getCandle(String event) {
    Map<String, Object> candleEvent = objectMapper.readValue(event,
        new TypeReference<LinkedHashMap<String, Object>>() {
        });
    return (LinkedHashMap) candleEvent.get("k");
  }

  public Candle mapEventCandleToDomain(EventCandle eventCandle) {
    return Candle.builder()
        .openTime(eventCandle.getOpenTime())
        .closedPrice(eventCandle.getClosedPrice())
        .build();
  }

  public Candle mapWebClientCandleToDomain(CandleWebClientResponse clientResponse) {
    return Candle.builder()
        .openTime(clientResponse.getOpenTime())
        .closedPrice(clientResponse.getClosedPrice())
        .build();
  }
}
