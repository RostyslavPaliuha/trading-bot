package com.rostyslav.trading.bot;

import com.rostyslav.trading.bot.service.candle.CandleWebClientResponse;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
@Slf4j
public class GrepCandleDataTest extends TestTools {

  @Test
  public void get1hourTimeFrameCandles(){
    List<CandleWebClientResponse> candles = getWebClientCandles("BTCUSDT", "1h", System.currentTimeMillis(), 1000);
    candles.forEach(clientResponse -> log.info("{}",clientResponse));
  }

}
