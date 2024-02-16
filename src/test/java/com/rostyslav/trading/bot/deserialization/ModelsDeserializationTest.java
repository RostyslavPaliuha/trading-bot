package com.rostyslav.trading.bot.deserialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.impl.SpotClientImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rostyslav.trading.bot.configuration.PrivateConfig;
import com.rostyslav.trading.bot.model.input.socket.Event;
import com.rostyslav.trading.bot.dto.CandleWebClientResponse;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ModelsDeserializationTest {

  private final ObjectMapper objectMapper;
  private final String event = "{\"e\":\"kline\",\"E\":1680367931001,\"s\":\"BTCUSDT\",\"k\":{\"t\":1680367930000,\"T\":1680367930999,\"s\":\"BTCUSDT\",\"i\":\"1s\",\"f\":3067357066,\"L\":3067357067,\"o\":\"28360.01000000\",\"c\":\"28360.01000000\",\"h\":\"28360.01000000\",\"l\":\"28360.01000000\",\"v\":\"0.01416000\",\"n\":2,\"x\":true,\"q\":\"401.57774160\",\"V\":\"0.01416000\",\"Q\":\"401.57774160\",\"B\":\"0\"}}";
  private final String candles= "[\n  [\n    1680368921000,\n    \"28348.41000000\",\n    \"28348.57000000\",\n    \"28348.41000000\",\n    \"28348.57000000\",\n    \"0.01817000\",\n    1680368921999,\n    \"515.09109950\",\n    13,\n    \"0.01779000\",\n    \"504.31864670\",\n    \"0\"\n  ],\n  [\n    1680368922000,\n    \"28348.59000000\",\n    \"28348.60000000\",\n    \"28348.59000000\",\n    \"28348.60000000\",\n    \"0.00214000\",\n    1680368922999,\n    \"60.66599310\",\n    4,\n    \"0.00214000\",\n    \"60.66599310\",\n    \"0\"\n  ],\n  [\n    1680368923000,\n    \"28348.60000000\",\n    \"28349.09000000\",\n    \"28348.60000000\",\n    \"28349.09000000\",\n    \"0.27343000\",\n    1680368923999,\n    \"7751.38688900\",\n    26,\n    \"0.27293000\",\n    \"7737.21258900\",\n    \"0\"\n  ]\n]".formatted();
  private final SpotClient spotClient;

  public ModelsDeserializationTest() {
    this.objectMapper = new ObjectMapper();
    this.spotClient = new SpotClientImpl(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY);
  }

  @SneakyThrows
  @Test
  public void KlineWebSocketEventDeserialization_test() {
    Event deserializedEvent = objectMapper.readValue(event, Event.class);
    assertEquals(1680367931001L, deserializedEvent.getEventTime());
    assertEquals(28360.01000000, deserializedEvent.getCandle().getClosedPrice());
    assertEquals(true, deserializedEvent.getCandle().getIsClosed());
  }

  @SneakyThrows
  @Test
  public void WebClientGetCandlesDeserialization_test() {
    List<CandleWebClientResponse> deserializedHistoricalCandles = objectMapper.readValue(candles, new TypeReference<List<CandleWebClientResponse>>() {});
    assertEquals(3,deserializedHistoricalCandles.size());
    assertEquals(28348.57000000,deserializedHistoricalCandles.get(0).getClosedPrice());
    assertEquals(28348.60000000,deserializedHistoricalCandles.get(1).getClosedPrice());
    assertEquals(28349.09000000,deserializedHistoricalCandles.get(2).getClosedPrice());
  }

}
