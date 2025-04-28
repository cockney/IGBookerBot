package com.golfbooking.service;

import com.golfbooking.model.BookingConfig;
import com.golfbooking.model.Golfer;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginService {

    private static final Logger LOGGER = Logger.getLogger(LoginService.class.getName());
    private final BookingHttpClient httpClient = BookingHttpClient.getInstance();

    public boolean login() {
        Golfer golfer = BookingConfig.getInstance().getGolfers().get(0);
        LOGGER.log(Level.INFO, "Logging in as {0}", golfer.getFirstName());

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("task", "login"));
        params.add(new BasicNameValuePair("topmenu", "1"));
        params.add(new BasicNameValuePair("memberid", golfer.getMemberID()));
        params.add(new BasicNameValuePair("pin", golfer.getPIN()));
        params.add(new BasicNameValuePair("Submit", "Login"));

        try (CloseableHttpResponse response = httpClient.post(BookingConfig.getInstance().getBaseClubUrl() + "/login.php", params)) {
            httpClient.consume(response);
            LOGGER.info("Logged in successfully");
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Login failed: {0}", e.getMessage());
            return false;
        }
    }
}
