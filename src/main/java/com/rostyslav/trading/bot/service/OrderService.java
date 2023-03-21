package com.rostyslav.trading.bot.service;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.impl.SpotClientImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rostyslav.trading.bot.configuration.PrivateConfig;
import com.rostyslav.trading.bot.service.order.HistoricalOrder;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class OrderService {

    private final ObjectMapper objectMapper;

    private final SpotClient spotClient;

    public OrderService(ObjectMapper objectMapper, SpotClient spotClient) {
        this.objectMapper = objectMapper;
        this.spotClient = spotClient;
    }

    public String sell(String symbol, String asset, String price) throws JsonProcessingException {
        String assetInfo = getAssetInfo(asset);
        String assetQuantity = getFreeAsset(assetInfo);
        return openNewOrder(symbol, "SELL", assetQuantity, price);
    }

    public String buy(String symbol, String asset, String price) throws JsonProcessingException {
        String assetInfo = getAssetInfo(asset);
        String assetQuantity = getBTCValuation(assetInfo);
        return openNewOrder(symbol, "BUY", assetQuantity, price);
    }

    private String getBTCValuation(String assetInfo) throws JsonProcessingException {
        List assetsInfo = objectMapper.readValue(assetInfo, List.class);
        Map<String, String> asset = (Map<String, String>) assetsInfo.get(0);
        String assetQuantity = asset.get("btcValuation");
        return assetQuantity;
    }

    private String getFreeAsset(String assetInfo) throws JsonProcessingException {
        List assetsInfo = objectMapper.readValue(assetInfo, List.class);
        Map<String, String> asset = (Map<String, String>) assetsInfo.get(0);
        String assetQuantity = asset.get("free");
        return assetQuantity;
    }

    private String openNewOrder(String symbol, String side, String quantity, String price) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("side", side);
        parameters.put("type", "LIMIT");
        parameters.put("timestamp", Instant.now().toString());
        parameters.put("newOrderRespType", "FULL");
        parameters.put("quantity", String.format("%.7s", quantity));
        parameters.put("price", price);
        parameters.put("timeInForce", "GTC");
        log.debug("New order parameters: {}", parameters);
        return spotClient.createTrade().newOrder(parameters);
    }

    private String getAssetInfo(String asset) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("asset", asset);
        String userAsset = spotClient.createWallet().getUserAsset(parameters);
        log.info("Get Assets Info: {}", userAsset);
        return userAsset;
    }

    public boolean hasOpenOrders() {
        return !checkOpenOrders().equals("[]");
    }

    private String checkOpenOrders() {
        LinkedHashMap parameters = new LinkedHashMap();
        // parameters.put("symbol", "");
        // parameters.put("recvWindow", 0l);
        // parameters.put("timestamp", System.currentTimeMillis());
        return spotClient.createTrade().getOpenOrders(parameters);
    }

    public void checkIfOrderFilled(AtomicBoolean isInPosition) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        SpotClient spotClient = new SpotClientImpl(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY);

        executorService.submit(() -> {
                    while (true) {
                        try {
                            Thread.sleep(5005);
                            String openOrders = spotClient.createTrade().getOpenOrders(new LinkedHashMap());
                            if ("[]".equals(openOrders)) {
                                isInPosition.set(false);
                            } else {
                                isInPosition.set(true);
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

    public HistoricalOrder getLastOrder(String symbol) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("limit", "10");
        parameters.put("timestamp", Instant.now().toEpochMilli());
        String lastOrder = spotClient.createTrade().getOrders(parameters);
        List<HistoricalOrder> ordersHistory = null;
        try {
            ordersHistory = objectMapper.readValue(lastOrder, new TypeReference<List<HistoricalOrder>>() {
            });
            return ordersHistory.stream()
                    .filter(stringStringMap -> !stringStringMap.getStatus().equals("CANCELED"))
                    .sorted((o1, o2) -> Comparator.<Long>reverseOrder().compare(o1.getTime(), o2.getTime()))
                    .findFirst()
                    .orElse(null);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
