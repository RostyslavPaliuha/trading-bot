package com.rostyslav.trading.bot.service.connection;


import com.restart4j.ApplicationRestart;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class DisasterRecoveryService {

    private AtomicLong atomicTimeStamp = new AtomicLong(0);

    @SneakyThrows
    public static void main(String[] args) {
        long pid = ManagementFactory.getRuntimeMXBean().getPid();
        System.out.println(pid);
        int index = 3;
        while (index != 0) {
            index -= 1;
            Thread.sleep(1000);
            System.out.println("CountDown before stop process, " + index);
        }

        new DisasterRecoveryService().restart();
        Thread.sleep(20000);
    }

    public void recoverConnection() {
        log.info("Start connection recovery process.");
        // runCommand();
    }

    public boolean ifDataReceivedInTime(String data) {
        long now = System.currentTimeMillis();
        if (atomicTimeStamp.get() == 0) {
            atomicTimeStamp.set(now);
            return true;
        }
        long previousDataReceivedTime = atomicTimeStamp.get();
        long receivedEventTimeDifference = now - previousDataReceivedTime;
        if (receivedEventTimeDifference > 1000 && receivedEventTimeDifference < 1100) {
            atomicTimeStamp.set(now);
            return true;
        }
        return false;
    }

    public void restart() {
        final ApplicationRestart appRestart = ApplicationRestart
                .builder()
                .build();
        appRestart.restartApp();
    }
}
