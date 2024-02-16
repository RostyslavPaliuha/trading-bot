package com.rostyslav.trading.bot;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.Arrays;

public class BotLauncher {

    public static void main(String[] args) {
        setLoggingLevel(Level.DEBUG);
        if (hasLoggingArgument(args)) {
            Arrays.stream(args)
                  .map(argument -> argument.split("=")[1])
                  .findFirst()
                  .map(debugLevel -> {
                      if (debugLevel.equalsIgnoreCase("DEBUG")) setLoggingLevel(Level.DEBUG);
                      return debugLevel;
                  });
        }
        new TradingBot().run();
    }

    public static void setLoggingLevel(Level level) {
        Logger root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.atLevel(level);
    }

    private static boolean hasLoggingArgument(String[] args) {
        return Arrays.stream(args)
                     .anyMatch(s -> s.startsWith("-l"));
    }
}
