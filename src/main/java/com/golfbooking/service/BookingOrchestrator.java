package com.golfbooking.service;

import com.golfbooking.model.Booking;
import com.golfbooking.model.BookingConfig;
import java.time.Duration;
import java.time.LocalTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingOrchestrator {

    private static final Logger LOGGER = Logger.getLogger(BookingOrchestrator.class.getName());

    private final LoginService loginService;
    private final TeeTimeService teeTimeService;
    private final GolferBookingService golferBookingService;
    private final LoggerInitialiser loggerInitialiser;
    private final DelayService delayService;

    public BookingOrchestrator(String configPath) {
        BookingConfig.init(configPath);
        this.loginService = new LoginService();
        this.teeTimeService = new TeeTimeService();
        this.golferBookingService = new GolferBookingService();
        this.loggerInitialiser = new LoggerInitialiser();
        this.delayService = new DelayService();
    }

    public void run() throws Exception {
        loggerInitialiser.init();

        if (!BookingConfig.getInstance().botIsActive()) {
            LOGGER.info("Bot is set as inactive, quitting.");
            return;
        }

        LOGGER.info("Starting booking bot");

        delayService.pauseRandom(15, 40, "login");
        loginService.login();
        delayService.pause(getDelayUntilBookingOpens(), "tee sheet opens");        

        Booking booking = teeTimeService.lockTeeTime();
        if (booking == null) {
            LOGGER.severe("Booking failed — no tokens obtained.");
            return;
        }

        delayService.pause(BookingConfig.getInstance().getRandomPlayerBookingDelay(), "booking first player");
        golferBookingService.addGolfersToBooking(booking);

        LOGGER.log(Level.INFO, "Finished booking at {0}", LocalTime.now());
    }

    private Duration getDelayUntilBookingOpens() {
        TimeSyncService timeSyncService = new TimeSyncService(SitePageList.getPages());
        LocalTime serverTime = timeSyncService.getCalculatedServerTime();

        LocalTime bookingOpenTime = LocalTime.of(
            BookingConfig.getInstance().getBookingOpenHour(),
            BookingConfig.getInstance().getBookingOpenMinute()
        );

        Duration delay;
        if (BookingConfig.DRY_RUN) {
            LOGGER.info("[DryRun] Booking immediately");
            delay = Duration.ZERO;
        } else {
            delay = Duration.between(serverTime, bookingOpenTime);
        }

        if (delay.isNegative()) {
            LOGGER.warning("Booking open time has already passed. Proceeding without delay.");
            return Duration.ZERO;
        }

        LOGGER.log(Level.INFO, "Delay until booking opens: {0} ms", delay.toMillis());
        return delay;
    }
    
}
