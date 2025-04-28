package com.golfbooking.model;

import java.util.Objects;

public class Golfer {

    public enum GolferType {
        MEMBER, GUEST
    }

    private final GolferType type;
    private final String firstname;
    private final String surname;
    private final String memberid;
    private final String pin;
    private String userAgent;

    private Golfer(GolferType type, String firstname, String surname, String memberid, String pin) {
        this.type = type;
        this.firstname = firstname;
        this.surname = surname;
        this.memberid = memberid;
        this.pin = pin;
    }

    public static Golfer createMember(String firstname, String surname, String memberid, String pin) {
        return new Golfer(GolferType.MEMBER, firstname, surname, memberid, pin);
    }

    public static Golfer createGuest(String firstname, String surname) {
        return new Golfer(GolferType.GUEST, firstname, surname, "", "");
    }

    public GolferType getType() {
        return type;
    }

    public String getFirstName() {
        return firstname;
    }

    public String getSurname() {
        return surname;
    }

    public String getMemberID() {
        return memberid;
    }

    public String getPIN() {
        return pin;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getFullName() {
        return firstname + " " + surname;
    }

    @Override
    public String toString() {
        return "Golfer{" +
               "type=" + type +
               ", firstname='" + firstname + '\'' +
               ", surname='" + surname + '\'' +
               ", memberid='" + memberid + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Golfer golfer = (Golfer) o;
        return type == golfer.type &&
               Objects.equals(firstname, golfer.firstname) &&
               Objects.equals(surname, golfer.surname) &&
               Objects.equals(memberid, golfer.memberid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, firstname, surname, memberid);
    }
}
