package com.rostyslav.trading.bot;

import ch.qos.logback.classic.Level;

import java.util.Arrays;

public class BotLauncher {

    public static void main(String[] args) {
        setLoggingLevel(Level.DEBUG);
        if (hasLoggingArgument(args)) {
            Arrays.stream(args)
                    .map(argument -> argument.split("=")[1])
                    .findFirst()
                    .map(debugLevel -> {
                        if (debugLevel.equalsIgnoreCase("DEBUG"))
                            setLoggingLevel(Level.DEBUG);
                        return debugLevel;
                    });
        }
        new TradingBot().run();
    }

    private static boolean hasLoggingArgument(String[] args) {
        return Arrays.stream(args)
                .anyMatch(s -> s.startsWith("-l"));
    }

    public static void setLoggingLevel(ch.qos.logback.classic.Level level) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }
}
