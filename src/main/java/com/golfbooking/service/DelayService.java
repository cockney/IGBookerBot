package com.golfbooking.service;

import com.golfbooking.model.BookingConfig;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DelayService {

    private static final Logger LOGGER = Logger.getLogger(DelayService.class.getName());

    public void pause(Duration duration, String reason) {
        if (BookingConfig.DRY_RUN) {
            LOGGER.log(Level.INFO, "[Dry Run] Would pause for {0} ms before {1}", new Object[]{duration.toMillis(), reason});
            return;
        }

        long millis = duration.toMillis();
        try {
            LOGGER.log(Level.INFO, "Pausing for {0} ms before {1}", new Object[]{millis, reason});
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interrupted during pause: {0}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public void pauseRandom(int minSeconds, int maxSeconds, String reason) {
        int delaySeconds = ThreadLocalRandom.current().nextInt(minSeconds, maxSeconds + 1);
        pause(Duration.ofSeconds(delaySeconds), reason);
    }

}
