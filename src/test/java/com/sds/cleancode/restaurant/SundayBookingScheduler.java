package com.sds.cleancode.restaurant;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SundayBookingScheduler extends BookingScheduler {

    public SundayBookingScheduler(int capacityPerHour) {
        super(capacityPerHour);
    }

    @Override
    public LocalDateTime getNow() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
            "yyyy/MM/dd HH:mm"
        );
        return LocalDateTime.parse("2026/09/20 12:00", formatter);
    }
}
