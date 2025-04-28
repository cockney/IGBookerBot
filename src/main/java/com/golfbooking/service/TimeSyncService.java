package com.golfbooking.service;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimeSyncService {

    private static final Logger LOGGER = Logger.getLogger(TimeSyncService.class.getName());

    private final List<String> probeUrls;
    private final ZoneId serverZone = ZoneId.of("GMT");

    public TimeSyncService(List<String> probeUrls) {
        this.probeUrls = probeUrls;
    }

    public LocalTime getCalculatedServerTime() {
        List<Duration> latencies = new ArrayList<>();
        String lastSuccessfulUrl = null;
        String lastDateHeader = null;

        for (int i = 0; i < 10; i++) {
            String url = getRandomUrl();
            Instant requestTime = Instant.now();

            try (CloseableHttpResponse response = BookingHttpClient.getInstance().get(url)) {
                Instant responseTime = Instant.now();

                // Measure round-trip latency
                Duration latency = Duration.between(requestTime, responseTime).dividedBy(2);
                latencies.add(latency);

                if (response.containsHeader(HttpHeaders.DATE)) {
                    lastDateHeader = response.getFirstHeader(HttpHeaders.DATE).getValue();
                }

                lastSuccessfulUrl = url;

            } catch (IOException | NullPointerException e) {
                LOGGER.log(Level.WARNING, "Request failed for {0}: {1}", new Object[]{url, e.getMessage()});
            }
        }

        if (latencies.isEmpty() || lastDateHeader == null) {
            LOGGER.severe("Failed to contact any probe URLs. Defaulting to system time.");
            return LocalTime.now();
        }

        Duration averageLatency = latencies.stream()
                .reduce(Duration.ZERO, Duration::plus)
                .dividedBy(latencies.size());

        LocalTime serverReportedTime = ZonedDateTime
                .parse(lastDateHeader, DateTimeFormatter.RFC_1123_DATE_TIME)
                .withZoneSameInstant(serverZone)
                .toLocalTime();

        LocalTime calculatedServerTime = serverReportedTime.plus(averageLatency);

        LocalTime localNow = LocalTime.now(serverZone);
        if (calculatedServerTime.isAfter(localNow)) {
            calculatedServerTime = localNow.minus(Duration.ofMillis(100));
        }

        LOGGER.log(Level.INFO,
                "Time sync - Server: {1}, Local: {2}, Avg Latency: {3} ms",
                new Object[]{calculatedServerTime, localNow, averageLatency.toMillis()}
        );

        return calculatedServerTime;
    }

    private String getRandomUrl() {
        int index = (int) (Math.random() * probeUrls.size());
        return probeUrls.get(index);
    }
}
