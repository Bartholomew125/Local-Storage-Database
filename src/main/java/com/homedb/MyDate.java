package com.homedb;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MyDate extends Date {

    private final Calendar c;

    /*
     * The timetamp should be in milliseconds since 
     */
    public MyDate(long timestamp, TimeUnit tu) {
        this.c = Calendar.getInstance();
        int multiplier;
        switch (tu) {
            case SECONDS:
                multiplier = 1000;
                break;
            default:
                multiplier = 1;
        }
        super(timestamp*multiplier);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.c.setTime(this);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH)+1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        sb.append(year);
        sb.append("/");
        if (month < 10) {
            sb.append(0);
        }
        sb.append(month);
        sb.append("/");
        if (day < 10) {
            sb.append(0);
        }
        sb.append(day);
        sb.append(" ");
        sb.append(hour);
        sb.append(":");
        if (minute < 10) {
            sb.append(0);
        }
        sb.append(minute);
        sb.append(":");
        if (second < 10) {
            sb.append(0);
        }
        sb.append(second);
        return sb.toString();
    }
    
}
