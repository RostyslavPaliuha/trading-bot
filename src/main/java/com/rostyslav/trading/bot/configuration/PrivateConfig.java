package com.rostyslav.trading.bot.configuration;

import java.util.Optional;

public class PrivateConfig {

    public static final String API_KEY = getProperty("API_KEY");

    public static final String SECRET_KEY = getProperty("SECRET_KEY");

    public static String getProperty(String name) {
        return Optional.ofNullable(System.getenv(name))
                       .orElseThrow(() -> new IllegalStateException(String.format("Property %s is empty", name)));
    }
}
