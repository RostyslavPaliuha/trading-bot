package com.rostyslav.trading.bot.notifier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TelegramNotifierTest {

    private TelegramNotifier telegramNotifier = new TelegramNotifier();
    @Test
    void testNotify() {
        telegramNotifier.notify("Hello there!");
    }
}