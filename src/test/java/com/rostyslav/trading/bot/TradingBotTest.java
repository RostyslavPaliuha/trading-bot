package com.rostyslav.trading.bot;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.WebsocketClient;
import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.impl.WebsocketClientImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rostyslav.trading.bot.configuration.PrivateConfig;
import com.rostyslav.trading.bot.service.OrderService;
import com.rostyslav.trading.bot.service.event.consumer.OrderUpdatesEventConsumer;
import com.rostyslav.trading.bot.service.order.HistoricalOrder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
public class TradingBotTest {

    ObjectMapper objectMapper;

    OrderService orderService;

    private SpotClient spotClient;

    private WebsocketClient websocketClient;

    public TradingBotTest() {
        this.spotClient = new SpotClientImpl(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY);
        this.websocketClient = new WebsocketClientImpl();
        this.objectMapper = new ObjectMapper();
        this.orderService = new OrderService(objectMapper, spotClient);
    }

    public static void main(String[] args) {
        //consumeOrderUpdates();
    }

    @Test
    public void testLastOrder(){
        HistoricalOrder lastOrder= orderService.getLastOrder("BTCUSDT");
        System.out.println(lastOrder);
        System.out.println(lastOrder.getSide());
        System.out.println(lastOrder.getPrice());
    }

    @Test
    public void testConnectorGetOpenOrders() {
        LinkedHashMap parameters = new LinkedHashMap();
        // parameters.put("symbol", "");
        // parameters.put("recvWindow", 0l);
        // parameters.put("timestamp", System.currentTimeMillis());


        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                Thread.sleep(1001);
                String openOrders = spotClient.createTrade().getOpenOrders(new LinkedHashMap());
                System.out.println(openOrders);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testLicenseKeyApi() {
        String listenKey = spotClient.createUserData().createListenKey();
        System.out.println(listenKey);
    }

    @Test
    public void openBuyOrder() throws JsonProcessingException {
        orderService.buy("BTCUSDT", "USDT", "100");
    }

    @Test
    public void openSellOrder() throws JsonProcessingException {
        orderService.sell("BTCUSDT", "BTC", "25000");
    }

    @Test
    public void cancelOrder() throws JsonProcessingException {
        List<Map<String, Object>> openOrdersMap = getOpenOrdersMap();
        Map<String, Object> stringObjectMap = openOrdersMap.get(0);
        Object orderId = stringObjectMap.get("orderId");
        cancelOrder(orderId);
    }

    private void cancelOrder(Object orderId) {
        LinkedHashMap parameters = new LinkedHashMap<>();
        parameters.put("orderId", orderId);
        parameters.put("symbol", "BTCUSDT");
        parameters.put("timestamp", Instant.now());
        spotClient.createTrade().cancelOrder(parameters);
    }

    private List<Map<String, Object>> getOpenOrdersMap() throws JsonProcessingException {
        String openOrders = spotClient.createTrade().getOpenOrders(new LinkedHashMap());
        List<Map<String, Object>> openOrdersMap = objectMapper.readValue(openOrders, List.class);
        System.out.println(openOrdersMap);
        return openOrdersMap;
    }

    @Test
    public void getExchangeInfo() {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        //parameters.put("permissions", "SPOT");
        String s = spotClient.createMarket().exchangeInfo(parameters);
        System.out.println(s);
    }

    @Test
    public void differencePercentage() {
        double lastCandlePrice = 22470;
        double lastBuyPrice = 22465;
        double profitPercentage = (Math.abs(lastBuyPrice - lastCandlePrice) / lastCandlePrice) * 100;
        assertEquals(0.022251891410769914, profitPercentage);
        System.out.println(profitPercentage);
    }

    @NotNull
    private void consumeOrderUpdates() {
        SpotClient spotClient = new SpotClientImpl(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY);
        WebsocketClient websocketClient = new WebsocketClientImpl();
        ObjectMapper objectMapper = new ObjectMapper();
        OrderUpdatesEventConsumer orderUpdatesEventConsumer = new OrderUpdatesEventConsumer(websocketClient, objectMapper, new AtomicBoolean(false), spotClient);
        orderUpdatesEventConsumer.consume();

    }
}