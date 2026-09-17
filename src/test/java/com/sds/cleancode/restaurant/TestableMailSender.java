package com.sds.cleancode.restaurant;

public class TestableMailSender extends MailSender {

    private boolean sentMail = false;

    @Override
    public void sendMail(Schedule schedule) {
        System.out.println("Test Email sent");
        sentMail = true;
    }

    public boolean hasSentMail() {
        return sentMail;
    }
}
