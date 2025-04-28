package com.golfbooking.service;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserAgentProvider {

    private static final Logger LOGGER = Logger.getLogger(UserAgentProvider.class.getName());
    private static final List<String> userAgents = new ArrayList<>();

    static {
        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    Objects.requireNonNull(
                        UserAgentProvider.class.getClassLoader().getResourceAsStream("user_agents.json"),
                        "user_agents.json not found in resources"
                    )
                )
            )
        ) {
            JSONParser parser = new JSONParser();
            JSONArray array = (JSONArray) parser.parse(reader);

            for (Object item : array) {
                userAgents.add(item.toString());
            }

            LOGGER.log(Level.INFO, "Loaded {0} user agents.", userAgents.size());
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, "Failed to parse user_agents.json: {0}", e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load user agents: {0}", e.getMessage());
        }
    }

    public static String getRandomUserAgent() {
        if (userAgents.isEmpty()) {
            return "Mozilla/5.0"; // fallback
        }
        int index = ThreadLocalRandom.current().nextInt(userAgents.size());
        return userAgents.get(index);
    }
}
