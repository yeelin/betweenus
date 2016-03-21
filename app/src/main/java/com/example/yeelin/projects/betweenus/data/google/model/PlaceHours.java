package com.example.yeelin.projects.betweenus.data.google.model;

import java.util.Arrays;

/**
 * Place Hours
 * Consists of open_now boolean, periods of open/close, and weekday text which is periods
 * in the form of string.
 */
public class PlaceHours {
    //boolean value indicating if the place is open at the current time.
    private final String open_now;
    //an array of seven strings representing the formatted opening hours for each day of the week.
    // If a language parameter was specified in the Place Details request, the Places Service will
    // format and localize the opening hours appropriately for that language.
    // The ordering of the elements in this array depends on the language parameter.
    // Some languages start the week on Monday while others start on Sunday.
    private final String[] weekday_text;
    //an array of opening periods covering seven days, starting from Sunday, in chronological order.
    private final Period[] periods;

    public PlaceHours(String open_now, String[] weekday_text, Period[] periods) {
        this.open_now = open_now;
        this.weekday_text = weekday_text;
        this.periods = periods;
    }

    public String getOpen_now() {
        return open_now;
    }

    public boolean isOpenNow() {
        return open_now.equalsIgnoreCase("true");
    }

    public String[] getWeekday_text() {
        return weekday_text;
    }

    public Period[] getPeriods() {
        return periods;
    }

    @Override
    public String toString() {
        return String.format("OpenNow:%s, WeekdayText:%s, Periods:%s",
                open_now, Arrays.toString(weekday_text), Arrays.toString(periods));
    }


    /**
     * Period class
     * Each Period consists of 2 subperiods, one for open and one for close.
     * A day can have more than 1 period if there are breaks in between when it's open and closed.
     *
     * Note:
     * If a place is always open, the close section will be missing from the response.
     * Clients can rely on always-open being represented as an open period containing day with
     * value 0 and time with value 0000, and no close.
     */
    public static class Period {
        //contains a pair of day and time objects describing when the place closes
        private final SubPeriod close;
        //contains a pair of day and time objects describing when the place opens
        private final SubPeriod open;

        public Period(SubPeriod close, SubPeriod open) {
            this.close = close;
            this.open = open;
        }

        public SubPeriod getClose() {
            return close;
        }

        public SubPeriod getOpen() {
            return open;
        }
    }

    /**
     * SubPeriod class
     * Each subperiod consists of a day and time.  2 subperiods make 1 period
     */
    public static class SubPeriod {
        //a number from 0–6, corresponding to the days of the week, starting on Sunday. For example, 2 means Tuesday.
        private final int day;
        //contain a time of day in 24-hour hhmm format. Values are in the range 0000–2359. The time will be reported in the place’s time zone.
        private final int time;

        public SubPeriod(int day, int time) {
            this.day = day;
            this.time = time;
        }

        public int getDay() {
            return day;
        }

        public int getTime() {
            return time;
        }
    }
}