package com.rostyslav.trading.bot.notifier;

import com.rostyslav.trading.bot.configuration.PrivateConfig;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class TelegramNotifier {

    private final String apiToken;

    private final String chatId;

    private final String urlStringTemplate = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";

    public TelegramNotifier() {
        this.apiToken = PrivateConfig.getProperty("TELEGRAM_API_TOKEN");
        this.chatId = PrivateConfig.getProperty("TELEGRAM_CHAT_ID");
    }

    public void notify(String message) {
        var urlString = String.format(urlStringTemplate, apiToken, chatId, message);
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            InputStream is = new BufferedInputStream(conn.getInputStream());
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

