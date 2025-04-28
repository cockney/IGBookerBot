package com.golfbooking.service;

import java.io.IOException;
import java.util.logging.*;

public class LoggerInitialiser {

    private static final String LOG_FILE_NAME = "bookerBot.log";
    private static FileHandler fileHandler;

    public void init() {
        // Set global format for all loggers
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT.%1$tL] [%4$-7s] %5$s %n");

        try {
            fileHandler = new FileHandler(LOG_FILE_NAME, true);
            fileHandler.setFormatter(new SimpleFormatter());

            Logger rootLogger = Logger.getLogger("");
            rootLogger.addHandler(fileHandler);

            // Optional: avoid duplicate console output
            for (Handler handler : rootLogger.getHandlers()) {
                if (handler instanceof ConsoleHandler) {
                    handler.setLevel(Level.OFF);
                }
            }

        } catch (IOException e) {
            Logger.getLogger(LoggerInitialiser.class.getName())
                  .log(Level.SEVERE, "Failed to initialize file logger", e);
        }
    }

    public void shutdown() {
        if (fileHandler != null) {
            fileHandler.close();
            Logger.getLogger("").removeHandler(fileHandler);
        }
    }
}
