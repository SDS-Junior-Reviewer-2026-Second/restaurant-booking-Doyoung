package com.sds.cleancode.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class BookingSchedulerTest {

    private static final int CAPACITY_PER_HOUR = 3;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
        "yyyy/MM/dd HH:mm"
    );

    private Customer customer = new Customer(
        "Jone Doe",
        "010-1234-1234",
        "example@example.com"
    );

    private BookingScheduler scheduler = new BookingScheduler(
        CAPACITY_PER_HOUR
    );

    private TestableSmsSender testableSmsSender = new TestableSmsSender();
    private TestableMailSender testableMailSender = new TestableMailSender();

    @BeforeEach
    void setupScheduler() {
        testableSmsSender = new TestableSmsSender();
        testableMailSender = new TestableMailSender();

        scheduler = new BookingScheduler(3);
        scheduler.setSmsSender(testableSmsSender);
        scheduler.setMailSender(testableMailSender);
    }

    @AfterEach
    void clearScheduler() {
        scheduler = null;
    }

    @Test
    public void 예약은_정시에만_가능하다_정시가_아닌경우_예약불가() {
        Schedule schedule = new Schedule(at("2026/09/17 09:05"), 1, customer);

        assertThatThrownBy(() -> {
            scheduler.addSchedule(schedule);
        }).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void 예약은_정시에만_가능하다_정시인_경우_예약가능() {
        Schedule schedule = new Schedule(at("2026/09/17 09:00"), 1, customer);

        assertThatNoException().isThrownBy(() -> {
            scheduler.addSchedule(schedule);
        });
    }

    @Test
    public void 시간대별_인원제한이_있다_같은_시간대에_Capacity_초과할_경우_예외발생() {
        assertThatThrownBy(() -> {
            scheduler.addSchedule(
                new Schedule(
                    at("2026/09/17 09:00"),
                    CAPACITY_PER_HOUR + 1,
                    customer
                )
            );
        }).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void 시간대별_인원제한이_있다_같은_시간대가_다르면_Capacity_차있어도_스케쥴_추가_성공() {
        assertThatNoException().isThrownBy(() -> {
            scheduler.addSchedule(
                new Schedule(
                    at("2026/09/17 09:00"),
                    CAPACITY_PER_HOUR,
                    customer
                )
            );

            scheduler.addSchedule(
                new Schedule(
                    at("2026/09/17 10:00"),
                    CAPACITY_PER_HOUR,
                    customer
                )
            );

            scheduler.addSchedule(
                new Schedule(
                    at("2026/09/17 11:00"),
                    CAPACITY_PER_HOUR,
                    customer
                )
            );
        });
    }

    @Test
    public void 예약완료시_SMS는_무조건_발송() {
        scheduler.addSchedule(
            new Schedule(at("2026/09/17 11:00"), 1, customer)
        );

        assertThat(testableSmsSender.hasSentSms()).isTrue();
    }

    @Test
    public void 이메일이_없는_경우에는_이메일_미발송() {
        Customer withoutEmail = new Customer("Jane Doe", "010-1234-1234");

        scheduler.addSchedule(
            new Schedule(at("2026/09/17 11:00"), 1, withoutEmail)
        );

        assertThat(testableMailSender.hasSentMail()).isFalse();
    }

    @Test
    public void 이메일이_있는_경우에는_이메일_발송() {
        scheduler.addSchedule(
            new Schedule(at("2026/09/17 11:00"), 1, customer)
        );

        assertThat(testableMailSender.hasSentMail()).isTrue();
    }

    @Test
    public void 현재날짜가_일요일인_경우_예약불가_예외처리() {
        BookingScheduler scheduler = new SundayBookingScheduler(
            CAPACITY_PER_HOUR
        );

        assertThatThrownBy(() -> {
            scheduler.addSchedule(
                new Schedule(at("2026/09/17 11:00"), 1, customer)
            );
        }).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void 현재날짜가_일요일이_아닌경우_예약가능() {
        BookingScheduler scheduler = new SaturdayBookingScheduler(
            CAPACITY_PER_HOUR
        );

        assertThatNoException().isThrownBy(() -> {
            scheduler.addSchedule(
                new Schedule(at("2026/09/17 11:00"), 1, customer)
            );
        });
    }

    private LocalDateTime at(String date) {
        return LocalDateTime.parse(date, formatter);
    }
}
