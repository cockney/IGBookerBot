package com.golfbooking.service;

import com.golfbooking.model.Booking;
import com.golfbooking.model.BookingConfig;
import com.golfbooking.model.BookingPage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;

public class TeeTimeService {

    private static final Logger LOGGER = Logger.getLogger(TeeTimeService.class.getName());
    private final BookingHttpClient httpClient = BookingHttpClient.getInstance();

    public Booking lockTeeTime() {
        Booking booking = new Booking();
        LocalTime teeTime = LocalTime.parse(BookingConfig.getInstance().getTeeTime());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:00");

        int attempts = 0;

        while (attempts < BookingConfig.getInstance().getMaxNumberOfBookingAttempts()) {
            String formattedTeeTime = teeTime.format(formatter);
            booking.setTeeTime(formattedTeeTime);

            try {
                LOGGER.log(Level.INFO, "Attempt {0}: Trying tee time {1}", new Object[]{attempts + 1, formattedTeeTime});

                BookingPage bookingPage = new BookingPage(BookingConfig.getInstance().getBookingDate());
                String[] token1 = bookingPage.getTokensForTime(formattedTeeTime);
                booking.setBookingToken1(token1);

                // Token 1 retrieved — try to lock the tee time
                tryBooking(booking);

                if (!booking.getBookingToken2().isEmpty()) {
                    LOGGER.log(Level.INFO, "Successfully locked tee time: {0}", formattedTeeTime);
                    return booking; // success!
                } else {
                    LOGGER.warning("Token 2 not retrieved. Will try next tee time...");
                }

            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to get token 1 for {0}: {1}", new Object[]{formattedTeeTime, e.getMessage()});
            }

            // Try the next tee time
            teeTime = teeTime.plusMinutes(BookingConfig.getInstance().getTeeTimeGap());
            attempts++;
        }

        LOGGER.log(Level.SEVERE, "Failed to lock any tee time after {0} attempts.", attempts);
        return null;
    }

    private void tryBooking(Booking booking) {
        LOGGER.log(Level.INFO, "Trying to lock slot at {0}", booking.getTeeTime());
        String grabURL = createTeeTimeURL(booking);
        try {
            ClassicHttpResponse resp = httpClient.get(grabURL);
            String token2 = extractBookingToken2(resp);
            booking.setBookingToken2(token2);
            LOGGER.log(Level.INFO, "Locked tee time. Token2: {0}", token2);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to grab tee time: {0}", e.getMessage());
        }
    }

    private String createTeeTimeURL(Booking booking) {
        try {
            String base = BookingConfig.getInstance().getBaseClubUrl() + "/memberbooking/?numslots=" + BookingConfig.getInstance().getNumberOfGolfers();
            base += "&date=" + BookingConfig.getInstance().getBookingDate();
            base += "&course=1&group=1";
            base += "&book=" + URLEncoder.encode(booking.getTeeTime(), StandardCharsets.UTF_8.toString());
            base += "&" + booking.getBookingToken1()[0] + "=" + booking.getBookingToken1()[1];
            base += "&holes=18";
            return base;
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, "Error encoding booking URL", e);
            return "";
        }
    }

    private String extractBookingToken2(ClassicHttpResponse resp) {
        Header locationHeader = resp.getFirstHeader("Location");
        if (locationHeader == null) {
            throw new IllegalStateException("Missing Location header in response.");
        }

        String redirectUrl = locationHeader.getValue();
        Pattern pattern = Pattern.compile("edit=([^&]+)");
        Matcher matcher = pattern.matcher(redirectUrl);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalStateException("Missing 'edit' parameter in Location URL: " + redirectUrl);
        }
    }

}
