package com.rostyslav.trading.bot.service.connection;

import lombok.SneakyThrows;



public class TestProcess {

    @SneakyThrows
    public static void main(String[] args) {
        ProcessBuilder processBuilder = new ProcessBuilder("osascript" ,"-e" ,"Trading bot", "do", "script","java","-jar","/Users/rostyslavpaliuha/development/projects/trading-bot/target/trading-bot-0.1.jar", "end tell'");
        Process start = processBuilder.start();
        System.out.println(start);
    }

}
