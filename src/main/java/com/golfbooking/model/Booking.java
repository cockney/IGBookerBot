package com.golfbooking.model;

public class Booking {

    private String teeTime;
    private String[] bookingToken1 = null;
    private String bookingToken2 = null;

    public String[] getBookingToken1() {
        return bookingToken1;
    }

    public void setBookingToken1(String[] bookingToken1) {
        this.bookingToken1 = bookingToken1;
    }

    public String getBookingToken2() {
        return bookingToken2;
    }

    public void setBookingToken2(String bookingToken2) {
        this.bookingToken2 = bookingToken2;
    }

    public String getTeeTime() {
        return teeTime;
    }

    public void setTeeTime(String teeTime) {
        this.teeTime = teeTime;
    }
    
}
