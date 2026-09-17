package com.sds.cleancode.restaurant;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SaturdayBookingScheduler extends BookingScheduler {

    public SaturdayBookingScheduler(int capacityPerHour) {
        super(capacityPerHour);
    }

    @Override
    public LocalDateTime getNow() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
            "yyyy/MM/dd HH:mm"
        );
        return LocalDateTime.parse("2026/09/19 12:00", formatter);
    }
}
