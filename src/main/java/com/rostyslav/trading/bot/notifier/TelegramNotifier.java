package com.rostyslav.trading.bot.notifier;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class TelegramNotifier {

    public void notify(String message) {
        String urlString = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";
        String apiToken = "6027843259:AAFv2g1wjAylfP_P4UIoN_RHMKnRMVa1iDE";
        String chatId = "1510894809";
        urlString = String.format(urlString, apiToken, chatId, message);
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            InputStream is = new BufferedInputStream(conn.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

