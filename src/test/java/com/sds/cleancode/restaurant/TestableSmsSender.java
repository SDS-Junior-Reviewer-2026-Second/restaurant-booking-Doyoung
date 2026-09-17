package com.sds.cleancode.restaurant;

public class TestableSmsSender extends SmsSender {

    private boolean sentSms = false;

    @Override
    public void send(Schedule schedule) {
        System.out.println("Test SMS sent");
        sentSms = true;
    }

    public boolean hasSentSms() {
        return sentSms;
    }
}
