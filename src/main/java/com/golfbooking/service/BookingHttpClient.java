package com.golfbooking.service;

import com.golfbooking.model.BookingConfig;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hc.core5.http.ParseException;

public class BookingHttpClient {

    private static final Logger LOGGER = Logger.getLogger(BookingHttpClient.class.getName());
    private static BookingHttpClient instance;

    private final CookieStore cookieStore;
    private final CloseableHttpClient httpClient;

    private BookingHttpClient() {
        this.cookieStore = new BasicCookieStore();
        this.httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setUserAgent(BookingConfig.getInstance().getGolfers().get(0).getUserAgent())
                .disableRedirectHandling()
                .build();
    }

    public static synchronized BookingHttpClient getInstance() {
        if (instance == null) {
            instance = new BookingHttpClient();
        }
        return instance;
    }

    public CloseableHttpResponse get(String url) throws IOException {
        LOGGER.log(Level.INFO, "GET: {0}", url);
        HttpGet request = new HttpGet(url);
        CloseableHttpResponse response = httpClient.execute(request);
        logResponseCode(response);
        return response;
    }

    public CloseableHttpResponse post(String url, List<NameValuePair> postParams) throws IOException {
        LOGGER.log(Level.INFO, "POST: {0} with params {1}", new Object[]{url, postParams});
        HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(postParams));
        CloseableHttpResponse response = httpClient.execute(post);
        logResponseCode(response);
        return response;
    }

    public String getBody(ClassicHttpResponse response) throws IOException, ParseException {
        HttpEntity entity = response.getEntity();
        return entity != null ? EntityUtils.toString(entity) : "";
    }

    public void consume(ClassicHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            EntityUtils.consume(entity);
        }
    }

    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to close HttpClient: {0}", e.getMessage());
        }
    }

    private void logResponseCode(ClassicHttpResponse response) {
        int statusCode = response.getCode();
        LOGGER.log(Level.INFO, "Response Code: {0}", statusCode);
    }
}
