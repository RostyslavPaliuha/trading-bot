package com.rostyslav.trading.bot;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.impl.SpotClientImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rostyslav.trading.bot.configuration.PrivateConfig;
import com.rostyslav.trading.bot.model.Candle;
import com.rostyslav.trading.bot.service.candle.CandleWebClientResponse;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.SneakyThrows;

public class TestTools {
  private final ObjectMapper objectMapper;

  private final SpotClient spotClient;

  public TestTools() {
    this.objectMapper = new ObjectMapper();
    this.spotClient = new SpotClientImpl(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY);
  }

  protected List<List<String>> getDeserializedCandles(String symbol, String interval, Long endTime, Integer limit) throws JsonProcessingException {
    LinkedHashMap hashMap = new LinkedHashMap();
    hashMap.put("symbol", symbol);
    hashMap.put("interval", "1s");
    hashMap.put("endTime", 1680423612000L);
    hashMap.put("limit", 20);
    String candles = spotClient.createMarket().klines(hashMap);
    new TypeReference<List<List<String>>>() {
    };
    List<List<String>> deserializedCandles = objectMapper.readValue(candles,
        new TypeReference<List<List<String>>>() {
        });
    return deserializedCandles;
  }
  @SneakyThrows
  protected List<CandleWebClientResponse> getWebClientCandles(String symbol, String interval, Long endTime, Integer limit)  {
    LinkedHashMap hashMap = new LinkedHashMap();
    hashMap.put("symbol", symbol);
    hashMap.put("interval", interval);
    hashMap.put("endTime",endTime);
    hashMap.put("limit", limit);
    String candles = spotClient.createMarket().klines(hashMap);
    new TypeReference<List<List<String>>>() {
    };
    List<CandleWebClientResponse> deserializedCandles = objectMapper.readValue(candles,
        new TypeReference<List<CandleWebClientResponse>>() {});
    return deserializedCandles;
  }
}
