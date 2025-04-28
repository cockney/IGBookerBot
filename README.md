# ⛳ Intelligent Golf Booking Bot

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![Made with Java](https://img.shields.io/badge/Made%20with-Java-blue.svg)](https://www.java.com/)

This project automatically books golf tee times at on Intelligent Golf websites while using **stealth techniques** to mimic human behavior and avoid bot detection.


## 🚀 How It Works

The bot is configured via the bookingConfig.json file, this allows you to define:

- How many days ahead you book
- What the earliest tee time is want and how many other later tee times to consider, which provides a random tee time no earlier than you want and helps avoid detection
- What the base URL of your golf club's Intelligent Golf website is
- How many attempts to try and book, useful if someone else grabs your preferred tee time before you do, the bot will try the next tee time after the one it failed to get
- Members' details, if you include more than 1 set of member details it will randomly select who to book as - great for avoiding detection


## 🥷 Stealth Features

This bot is carefully engineered to behave like a **real person**:

| Stealth Feature        | Description |
|:------------------------|:------------|
| **Startup Delay**        | Once the service starts it will **pause randomly** before it logs into the IG web site. |
| **Booking Pause** | After each player is added, it **pauses randomly** to simulate a human delay. |
| **Tee Times**       | Set an earliest tee time and the other times to consider later than that, and the bot will randomly pick one. |
| **Booking member** | Provide more than 2 members' credentials, and the bot will randomly select one to use as the main booker. |


## 📝 Configuration Items in bookingConfig.json

| Key | Example Value | Description |
|:----|:--------------|:------------|
| baseClubUrl | https://www.swgc.com | Base URL of the golf club's booking system |
| teeTime.earliestTeeTime | 17:20 | Earliest tee time to try for |
| teeTime.maxTeeTimesToConsider | 3 | Maximum number of later tee times to consider, this helps avoid detection |
| teeTime.teeTimeGap | 10 | Time gap between tee times (in minutes) |
| teeTime.maxNumberOfBookingAttempts | 3 | Maximum number of retries for booking |
| bookingPause.minBookingPauseInSecs | 20 | Minimum pause between booking attempts (in seconds) |
| bookingPause.bookingPauseVariabilityInSecs | 30 | Extra random delay added to booking pause (in seconds) |
| bookingOpens.botIsActive | true | Whether the bot is active, useful if you need to switch it off |
| bookingOpens.hour | 20 | Hour of the day when bookings open on your website |
| bookingOpens.minute | 15 | Minute of the hour when bookings open |
| bookingOpens.daysAheadToBook | 5 | How many days ahead of today to book a tee time |
| golfers | See below | List of golfers (both members and guests) |

Only members that you want to use to create the booking need member ID and PIN, at least one member needs this. If you have two members with IDs and PINs the bot will select one to book as, this is a good way to hide the fact you're using a bot. Guests just need a first and last name.

| Type | First Name | Surname | Member ID | PIN |
|:-----|:-----------|:--------|:----------|:----|
| member | Paul | Clark | EMAIL@ADDRESS | PIN-NUMBER |
| member | Alan | Brownlee | *(empty)* | *(empty)* |
| guest | Martin | Gay | *(N/A)* | *(N/A)* |

## ⏰ Running the Bot Automatically with Crontab

You can use **crontab** (Linux's built-in scheduler) to automatically run the compiled `.jar` file at a specific time each week.

Here’s an example crontab entry:

```bash
12 20 * * 5 cd /home/paul/IGBookingBot && java -jar bookerBot.jar
```

In plain English:
👉 Every Friday at 8:12 PM, the system will:

cd into /home/paul/IGBookingBot

Run the command java -jar bookerBot.jar
(launching the Golf Booking Bot)

