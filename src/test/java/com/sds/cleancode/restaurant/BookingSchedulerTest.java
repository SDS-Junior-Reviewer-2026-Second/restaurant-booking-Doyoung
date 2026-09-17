package com.sds.cleancode.restaurant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class BookingSchedulerTest {

    private static final int CAPACITY_PER_HOUR = 3;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
        "yyyy/MM/dd HH:mm"
    );

    @Mock
    private Customer WITH_MAIL;

    @Mock
    private Customer WITHOUT_MAIL;

    private BookingScheduler scheduler;

    @Mock
    private SmsSender smsSender;

    @Mock
    private MailSender mailSender;

    @BeforeEach
    void setupScheduler() {
        scheduler = Mockito.spy(new BookingScheduler(CAPACITY_PER_HOUR));
        scheduler.setSmsSender(smsSender);
        scheduler.setMailSender(mailSender);
    }

    @Test
    public void 예약은_정시에만_가능하다_정시가_아닌경우_예약불가() {
        Schedule schedule = new Schedule(at("2026/09/17 09:05"), 1, WITH_MAIL);

        assertThatThrownBy(() -> {
            scheduler.addSchedule(schedule);
        }).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void 예약은_정시에만_가능하다_정시인_경우_예약가능() {
        Schedule schedule = new Schedule(at("2026/09/17 09:00"), 1, WITH_MAIL);

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
                    WITH_MAIL
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
                    WITH_MAIL
                )
            );

            scheduler.addSchedule(
                new Schedule(
                    at("2026/09/17 10:00"),
                    CAPACITY_PER_HOUR,
                    WITH_MAIL
                )
            );

            scheduler.addSchedule(
                new Schedule(
                    at("2026/09/17 11:00"),
                    CAPACITY_PER_HOUR,
                    WITH_MAIL
                )
            );
        });
    }

    @Test
    public void 예약완료시_SMS는_무조건_발송() {
        scheduler.addSchedule(
            new Schedule(at("2026/09/17 11:00"), 1, WITH_MAIL)
        );

        verify(smsSender, times(1)).send(any());
    }

    @Test
    public void 이메일이_없는_경우에는_이메일_미발송() {
        scheduler.addSchedule(
            new Schedule(at("2026/09/17 11:00"), 1, WITHOUT_MAIL)
        );

        verify(mailSender, times(0)).sendMail(any());
    }

    @Test
    public void 이메일이_있는_경우에는_이메일_발송() {
        when(WITH_MAIL.getEmail()).thenReturn("example@example.com");

        scheduler.addSchedule(
            new Schedule(at("2026/09/17 11:00"), 1, WITH_MAIL)
        );

        verify(mailSender, times(1)).sendMail(any());
    }

    @Test
    public void 현재날짜가_일요일인_경우_예약불가_예외처리() {
        doReturn(at("2026/09/20 09:00")).when(scheduler).getNow();

        assertThatThrownBy(() -> {
            scheduler.addSchedule(
                new Schedule(at("2026/09/23 11:00"), 1, WITH_MAIL)
            );
        }).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void 현재날짜가_일요일이_아닌경우_예약가능() {
        doReturn(at("2026/09/19 09:00")).when(scheduler).getNow();

        assertThatNoException().isThrownBy(() -> {
            scheduler.addSchedule(
                new Schedule(at("2026/09/20 11:00"), 1, WITH_MAIL)
            );
        });
    }

    private LocalDateTime at(String date) {
        return LocalDateTime.parse(date, formatter);
    }
}
