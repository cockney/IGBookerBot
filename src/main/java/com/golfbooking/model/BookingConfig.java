package com.golfbooking.model;

import com.golfbooking.service.UserAgentProvider;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingConfig {

    private static BookingConfig instance;

    private Boolean botIsActive = false;
    public static final boolean DRY_RUN = true;

    private String earliestTeeTime;
    private final ArrayList<Golfer> golfers = new ArrayList<>();
    private Integer maxTeeTimesToConsider;
    private Integer teeTimeGap;
    private Integer maxNumberOfBookingAttempts;
    private Integer bookingOpenHour;
    private Integer bookingOpenMinute;
    public Integer minBookingPauseInSecs;
    public Integer bookingPauseVariabilityInSecs;
    private String baseClubUrl;
    private JSONObject bookingDetails = null;
    private int daysAheadToBook = 8; // default fallback if not set
    
    private BookingConfig(String bookingConfigFile) {
        getBookingDetails(bookingConfigFile);
        extractBaseClubUrl();
        extractTeeTime();
        extractBookingOpens();
        extractBookingPause();
        processGolferDetails();
    }

    public static void init(String configFilePath) {
        if (instance != null) {
            throw new IllegalStateException("BookingConfig is already initialized.");
        }
        instance = new BookingConfig(configFilePath);
    }

    public static BookingConfig getInstance() {
        if (instance == null) {
            throw new IllegalStateException("BookingConfig has not been initialized.");
        }
        return instance;
    }

    private void extractBookingPause() {
        JSONObject bookingPause = (JSONObject) bookingDetails.get("bookingPause");
        minBookingPauseInSecs = Integer.valueOf(bookingPause.get("minBookingPauseInSecs").toString());
        bookingPauseVariabilityInSecs = Integer.valueOf(bookingPause.get("bookingPauseVariabilityInSecs").toString());
    }

    private void extractTeeTime() {
        JSONObject tTime = (JSONObject) bookingDetails.get("teeTime");
        earliestTeeTime = (String) tTime.get("earliestTeeTime");
        teeTimeGap = Integer.valueOf(tTime.get("teeTimeGap").toString());
        maxNumberOfBookingAttempts = Integer.valueOf(tTime.get("maxNumberOfBookingAttempts").toString());
        maxTeeTimesToConsider = Integer.valueOf(tTime.get("maxTeeTimesToConsider").toString());
    }

    private void extractBookingOpens() {
        JSONObject bOpens = (JSONObject) bookingDetails.get("bookingOpens");
        botIsActive = Boolean.valueOf(bOpens.get("botIsActive").toString());
        bookingOpenHour = Integer.valueOf(bOpens.get("hour").toString());
        bookingOpenMinute = Integer.valueOf(bOpens.get("minute").toString());
        Object daysAheadObj = bOpens.get("daysAheadToBook");
        if (daysAheadObj != null) {
            daysAheadToBook = Integer.parseInt(daysAheadObj.toString());
        }
    }

    private void extractBaseClubUrl() {
        this.baseClubUrl = (String) bookingDetails.get("baseClubUrl");
    }

    private JSONObject getBookingDetails(String bookingDetailsFile) {
        if (bookingDetails == null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(bookingDetailsFile))) {
                JSONParser parser = new JSONParser();
                bookingDetails = (JSONObject) parser.parse(reader);
            } catch (IOException | ParseException ex) {
                Logger.getLogger(BookingConfig.class.getName()).log(Level.SEVERE, "Failed to parse config", ex);
                throw new RuntimeException("Unable to load booking config", ex);
            }
        }
        return bookingDetails;
    }

    public final void processGolferDetails() {
        ArrayList<Golfer> golfersWithoutPins = new ArrayList<>();
        JSONArray golferList = (JSONArray) bookingDetails.get("golfers");

        for (JSONObject objGolfer : (Iterable<JSONObject>) golferList) {
            String type = (String) objGolfer.get("type");
            String firstname = (String) objGolfer.get("firstname");
            String surname = (String) objGolfer.get("surname");

            Golfer golfer;

            if ("member".equals(type)) {
                String memberid = (String) objGolfer.get("memberid");
                String pin = (String) objGolfer.get("pin");
                if (pin == null) pin = "";
                golfer = Golfer.createMember(firstname, surname, memberid, pin);
            } else {
                golfer = Golfer.createGuest(firstname, surname);
            }

            if (golfer.getPIN().isEmpty()) {
                // can't be used to book
                golfersWithoutPins.add(golfer);
            } else {
                golfer.setUserAgent(UserAgentProvider.getRandomUserAgent());
                golfers.add(golfer);
            }
        }

        Collections.shuffle(golfers); // randomly choose who will book
        golfers.addAll(golfersWithoutPins);
    }

    public String getTeeTime() {
        int teeTimeOffset = ThreadLocalRandom.current().nextInt(0, maxTeeTimesToConsider) * teeTimeGap;
        LocalTime teeTime = LocalTime.parse(earliestTeeTime).plusMinutes(teeTimeOffset);
        return teeTime.format(DateTimeFormatter.ofPattern("HH:mm:00"));
    }

    public ArrayList<Golfer> getGolfers() { return golfers; }
    public Integer getNumberOfGolfers() { return golfers.size(); }
    public Integer getTeeTimeGap() { return teeTimeGap; }
    public Integer getMaxTeeTimesToConsider() { return maxTeeTimesToConsider; }
    public Integer getMaxNumberOfBookingAttempts() { return maxNumberOfBookingAttempts; }
    public Boolean botIsActive() { return botIsActive; }
    public Integer getBookingOpenHour() { return bookingOpenHour; }
    public Integer getBookingOpenMinute() { return bookingOpenMinute; }
    public String getBaseClubUrl() { return baseClubUrl; }

    public String getBookingDate() {
        LocalDate targetDate = LocalDate.now().plusDays(daysAheadToBook);
        return targetDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));    
    }

    public String getNextTeeTime(String currentTeeTime) {
        LocalTime t = LocalTime.parse(currentTeeTime);
        return t.plusMinutes(getTeeTimeGap()).format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public Duration getRandomPlayerBookingDelay() {
        int randomSeconds = ThreadLocalRandom.current()
            .nextInt(0, bookingPauseVariabilityInSecs) + minBookingPauseInSecs;
        return Duration.ofSeconds(randomSeconds);
    }
}
