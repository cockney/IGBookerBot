package com.golfbooking.model;

import com.golfbooking.service.BookingHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingPage {

    private Document parsedHTML;
    private static final Logger LOGGER = Logger.getLogger(BookingPage.class.getName());

    public BookingPage(String dateToBook) {
        try {
            String bookingPage = BookingConfig.getInstance().getBaseClubUrl() + "/memberbooking/?date=" + dateToBook;

            try (CloseableHttpResponse resp = BookingHttpClient.getInstance().get(bookingPage)) {
                HttpEntity entity = resp.getEntity();
                String bookingPageHTML = EntityUtils.toString(entity);
                parsedHTML = Jsoup.parse(bookingPageHTML);
            }

        } catch (IOException | ParseException e) {
            LOGGER.log(Level.SEVERE, "Failed to get booking token: {0}", e.getMessage());
        }
    }

    public String[] getTokensForTime(String timeToBook) {
        try {
            Elements els = parsedHTML.select("input[value='" + timeToBook + "']");
            Node n = els.first().nextSibling();
            String[] token = new String[2];
            token[0] = n.attr("name");
            token[1] = n.attr("value");
            return token;
        } catch (Exception e) {
            throw new RuntimeException("Error extracting tokens for time: " + timeToBook, e);
        }
    }
}
