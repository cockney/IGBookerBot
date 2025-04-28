package com.golfbooking;

import com.golfbooking.service.BookingOrchestrator;

public class IGBookingBot {

    public static void main(String[] args) {
        try {
            BookingOrchestrator IGBookingBot = new BookingOrchestrator("bookingConfig.json");
            IGBookingBot.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}