package com.rostyslav.trading.bot.eventHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

public class CandleEventHandler {

    private final String event;

    private final ObjectMapper objectMapper;

    public CandleEventHandler(String event, ObjectMapper objectMapper) {
        this.event = event;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getCandleEvent() {
        Map<String, Object> candleEvent;
        try {
            candleEvent = objectMapper.readValue(event, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return candleEvent;
    }

    public LinkedHashMap getCandle(Map<String, Object> candleEvent) {
        return (LinkedHashMap) candleEvent.get("k");
    }
}
