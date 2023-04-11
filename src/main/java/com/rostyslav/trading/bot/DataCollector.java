package com.rostyslav.trading.bot;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.WebsocketClient;
import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.impl.WebsocketClientImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rostyslav.trading.bot.service.ClosedCandlesQueue;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataCollector {

    private WebsocketClient websocketClient;

    private ObjectMapper objectMapper;

    private ClosedCandlesQueue closedCandlesQueue;

    private SpotClient spotClient = new SpotClientImpl();

    public DataCollector(WebsocketClient websocketClient) {
        this.websocketClient = websocketClient;
        this.objectMapper = new ObjectMapper();
        closedCandlesQueue = new ClosedCandlesQueue(10000);
    }

    public static void main(String[] args) {
        DataCollector dataCollector = new DataCollector(new WebsocketClientImpl());
        dataCollector.collectToFile();
      //  dataCollector.test();
    }

    public void collectToFile() {
        websocketClient.klineStream("BTCUSDT", "1s",
                data -> {
                },
                message -> {
                   log.info(message);
                },
                closeMessage -> {
                },
                failureMessage -> {
                });

    }

   /* public void test() {
        LocalDateTime startTime = LocalDateTime.now().minus(Duration.ofDays(1));
        Instant instant = startTime.toInstant(ZoneOffset.of("+02:00"));
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        parameters.put("interval", "1s");
        //parameters.put("startTime", instant.getEpochSecond());
        //parameters.put("endTime", instant.getEpochSecond());
        String klines = spotClient.createMarket().klines(parameters);
        System.out.println(klines);

        Gson gson = new Gson();
        List<List<String>> jsonElements = gson.fromJson(klines, List.class);
       // System.out.println(jsonElements.size());
        double[] prices = new double[jsonElements.size()];
        AtomicInteger index=new AtomicInteger(0);
        jsonElements.forEach(list -> {
            String s = (String) list.get(4);
            // System.out.println(String.format("%.2f", Double.parseDouble(s)));
            prices[index.getAndIncrement()] = Double.parseDouble(String.format("%.2f", Double.parseDouble(s)));
        });
        StochacticCalculator calculator = new StochacticCalculator( 14, 14, 14);
        calculator.calculate(prices);
        System.out.print("RSI: ");
        Arrays.stream( calculator.getRSI()).forEach(value -> System.out.print(String.format("%.1f",value)+", "));
        System.out.println("");
        System.out.print("FastK ");
        Arrays.stream( calculator.getFastK()).forEach(value -> System.out.print(String.format("%.1f",value)+", "));
        System.out.println("");
        System.out.print("FastD ");
        Arrays.stream( calculator.getFastD()).forEach(value -> System.out.print(String.format("%.1f",value)+", "));
    }*/




    public double[] calculateStochRSI(double[] prices, int period) {
        double[] output = new double[prices.length];
        double[] output2 = new double[prices.length];
        double[] tempOutPut = new double[prices.length];
        double[] tempOutPut2 = new double[prices.length];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        RetCode retCode = RetCode.InternalError;
        begin.value = -1;
        length.value = -1;
        retCode = new Core().stochRsi(0, prices.length - 1, prices, period,5 ,3, MAType.Sma,begin, length, tempOutPut,tempOutPut2);
        for (int i = 0; i < period; i++) {
            output[i] = 0;
        }
        for (int i = period; 0 < i && i < (prices.length); i++) {
            output[i] = tempOutPut[i - period];
        }
        return output;
    }
}
