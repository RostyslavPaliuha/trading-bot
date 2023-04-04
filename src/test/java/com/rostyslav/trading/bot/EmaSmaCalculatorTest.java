package com.rostyslav.trading.bot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.rostyslav.trading.bot.service.indicator.calculator.ma.EMaSmaCalculator;
import com.rostyslav.trading.bot.service.indicator.calculator.ma.EmaSma;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class EmaSmaCalculatorTest extends TestTools {


  private final EMaSmaCalculator calculator;

  public EmaSmaCalculatorTest() {
    this.calculator = new EMaSmaCalculator();
  }

  @Test
  @SneakyThrows
  public void smaEmaCalculation() {
    List<List<String>> deserializedCandles = getDeserializedCandles("BTCUSDT", "1s",
        System.currentTimeMillis(), 20);
    TimePriceEmaSma[] timePriceEmaSmas = deserializedCandles.stream()
        .sorted(Comparator.<List<String>>comparingLong(o -> Long.parseLong(o.get(0)))
            .reversed())
        .map(candle -> TimePriceEmaSma.builder()
            .time(Long.parseLong(candle.get(0)))
            .price(Double.parseDouble(candle.get(4)))
            .build())
        .toArray(TimePriceEmaSma[]::new);
    assertEquals(20, timePriceEmaSmas.length);
    double[] candlesClosedPrise = Arrays.stream(timePriceEmaSmas)
        .mapToDouble(timePriceEmaSma -> timePriceEmaSma.getPrice())
        .toArray();
    EmaSma[] emaSmaPoints = calculator.calculate(candlesClosedPrise);
    assertEquals(20, emaSmaPoints.length);
    LinkedList<TimePriceEmaSma> timePriceEmaSmaLinkedList = IntStream.range(0,
            candlesClosedPrise.length)
        .mapToObj(integer -> {
          EmaSma emaSmaPoint = emaSmaPoints[integer];
          TimePriceEmaSma timePriceEmaSma = timePriceEmaSmas[integer];
          return TimePriceEmaSma.builder()
              .time(timePriceEmaSma.getTime())
              .price(timePriceEmaSma.getPrice())
              .ema(emaSmaPoint.getEma())
              .sma(emaSmaPoint.getSma())
              .build();
        }).collect(Collectors.toCollection(LinkedList::new));

    assertEquals(timePriceEmaSmaLinkedList.size(), 20);
    timePriceEmaSmaLinkedList.forEach(quater ->
        log.info("Time: {}, price: {}, ema: {}, sma: {}", quater.getTime(), quater.getPrice(),
            quater.getEma(), quater.getSma())
    );
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class TimePriceEmaSma {

    private Long time;

    private Double price;

    private Double ema;

    private Double sma;
  }


}
