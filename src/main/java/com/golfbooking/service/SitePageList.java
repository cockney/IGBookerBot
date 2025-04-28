package com.golfbooking.service;

import com.golfbooking.model.BookingConfig;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SitePageList {

    private static final Logger LOGGER = Logger.getLogger(SitePageList.class.getName());

    public static List<String> getPages() {
        String baseUrl = BookingConfig.getInstance().getBaseClubUrl();
        List<String> pages = new ArrayList<>();
        pages.add(baseUrl);

        try {
            Document doc = Jsoup.connect(baseUrl).get();
            Elements links = doc.select("a[href]");
            
            // Convert Elements to a List so we can shuffle
            List<Element> linkList = new ArrayList<>(links); 
            Collections.shuffle(linkList); // Randomize order

            Set<String> collected = new HashSet<>();
            URI baseUri = URI.create(baseUrl);

            for (Element link : linkList) {
                String href = link.absUrl("href");

                // Only collect internal links to the same domain
                if (href.startsWith(baseUri.getScheme() + "://" + baseUri.getHost()) && !href.equals(baseUrl)) {
                    if (collected.add(href)) {
                        pages.add(href);
                        if (pages.size() == 5) break; // base + 4 more
                    }
                }
            }

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load or parse base URL: " + baseUrl, e);
        }

        return pages;
    }
}
