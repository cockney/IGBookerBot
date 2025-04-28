package com.golfbooking.service;

import com.golfbooking.model.Booking;
import com.golfbooking.model.BookingConfig;
import com.golfbooking.model.Golfer;
import java.io.IOException;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GolferBookingService {

    private static final Logger LOGGER = Logger.getLogger(GolferBookingService.class.getName());
    private final BookingHttpClient httpClient = BookingHttpClient.getInstance();
    private final DelayService delayService;

    public GolferBookingService() {
        this.delayService = new DelayService();
    }

    public void addGolfersToBooking(Booking booking) {
        LOGGER.info("Adding golfers to booking");

        String baseUrl = BookingConfig.getInstance().getBaseClubUrl() + "/memberbooking/?edit=" + booking.getBookingToken2();
        List<Golfer> golfers = BookingConfig.getInstance().getGolfers();

        if (golfers == null || golfers.size() <= 1) {
            LOGGER.warning("No additional golfers to add.");
            return;
        }

        int slot = 2; // Slot 1 is for the booking initiator
        List<Golfer> additionalGolfers = golfers.subList(1, golfers.size());

        for (Golfer golfer : additionalGolfers) {
            try {
                addGolferToBooking(golfer, baseUrl, slot);
                LOGGER.log(Level.INFO, "Added {0} to slot {1}", new Object[]{golfer.getFullName(), slot});
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to add golfer {0}: {1}", new Object[]{golfer.getFullName(), e.getMessage()});
            }

            delayService.pause(BookingConfig.getInstance().getRandomPlayerBookingDelay(), "Booking next golfer");
            slot++;
        }
    }

    private int fetchGolferId(Golfer golfer, String bookingUrl) throws Exception {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("partner", golfer.getSurname().substring(0, 3)));

        String responseHtml;
        try {
            responseHtml = httpClient.getBody(httpClient.post(bookingUrl, params));
        } catch (IOException e) {
            throw new Exception("HTTP error while fetching golfer ID", e);
        }

        return extractGolferIdFromHtml(golfer, responseHtml);
    }

    private void addGolferToBooking(Golfer golfer, String baseUrl, int slot) throws IOException, Exception {
        switch (golfer.getType()) {
            case MEMBER:
                addMemberGolfer(golfer, baseUrl, slot);
                break;
            case GUEST:
                addGuestGolfer(golfer, baseUrl, slot);
                break;
            default:
                throw new IllegalStateException("Unknown golfer type: " + golfer.getType());
        }
    }

    private void addMemberGolfer(Golfer golfer, String baseUrl, int slot) throws IOException, Exception {
        int golferId = fetchGolferId(golfer, baseUrl);
        String fullUrl = baseUrl + "&addpartner=" + golferId + "&partnerslot=" + slot;
        httpClient.get(fullUrl);
    }

    private void addGuestGolfer(Golfer golfer, String baseUrl, int slot) throws IOException {
        String postUrl = baseUrl + "&partnerslot=" + slot;

        // Create POST parameters
        List<NameValuePair> form = new ArrayList<>();
        form.add(new BasicNameValuePair("guest", "1"));
        form.add(new BasicNameValuePair("forename", golfer.getFirstName()));
        form.add(new BasicNameValuePair("surname", golfer.getSurname()));
        // email and mobile aren't required for guests
        form.add(new BasicNameValuePair("user_email", "")); 
        form.add(new BasicNameValuePair("mobtel", ""));

        // Execute the request     
        httpClient.post(postUrl, form);
    }

    private int extractGolferIdFromHtml(Golfer golfer, String html) throws Exception {
        Document doc = Jsoup.parse(html);
        Elements links = doc.getElementsByTag("a");

        for (Element link : links) {
            if (link.text().contains(golfer.getFirstName() + " " + golfer.getSurname())) {
                String href = link.attr("href");
                String[] parts = href.split("&");
                for (String part : parts) {
                    if (part.startsWith("addpartner=")) {
                        return Integer.parseInt(part.split("=")[1]);
                    }
                }
            }
        }

        throw new Exception("Could not find golfer ID for " + golfer.getFullName());
    }

}
