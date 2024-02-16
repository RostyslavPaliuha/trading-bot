package com.rostyslav.trading.bot.service.event.consumer;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.WebSocketStreamClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rostyslav.trading.bot.model.ListenKey;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class OrderUpdatesEventConsumer {

    private final WebSocketStreamClient websocketClient;

    private final ObjectMapper objectMapper;

    private final SpotClient spotClient;

    private AtomicBoolean isInPosition;

    public OrderUpdatesEventConsumer(WebSocketStreamClient websocketClient, ObjectMapper objectMapper, AtomicBoolean isInPosition, SpotClient spotClient) {
        this.websocketClient = websocketClient;
        this.objectMapper = objectMapper;
        this.isInPosition = isInPosition;
        this.spotClient = spotClient;
    }


    public void consume() {
        log.info("Start read web socket stream from binance.");
        ListenKey listenKey = getListenKey();
        websocketClient.listenUserStream(listenKey.getListenKey(), data -> {
            log.info("Order UpdateData received: {}", data);
            Map<String, Object> eventData = extractEventData(data);
            String eventType = getOrderUpdateEventType(eventData);
            String orderStatus = getOrderStatus(eventData);
            if ("executionReport".equals(eventType) && "NEW".equals(orderStatus)) {
                isInPosition.set(true);
            }
            if ("executionReport".equals(eventType) && "CANCELED".equals(orderStatus)) {
                isInPosition.set(false);
            }
        });
    }

    private ListenKey getListenKey() {
        try {
            return objectMapper.readValue(spotClient.createUserData()
                                                    .createListenKey(), ListenKey.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> extractEventData(String data) {
        Map<String, Object> map = new HashMap<>();
        try {
           return objectMapper.readValue(data, map.getClass());
        } catch (JsonProcessingException e) {
            log.error("Extracting event data failed.");
        }
        return map;
    }

    private String getOrderUpdateEventType(Map<String, Object> eventData) {
        return (String) eventData.get("e");
    }

    private String getOrderStatus(Map<String, Object> eventData) {
        return (String) eventData.get("X");
    }
}
