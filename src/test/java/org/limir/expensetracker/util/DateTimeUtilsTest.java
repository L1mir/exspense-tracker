package org.limir.expensetracker.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class DateTimeUtilsTest {

    private static final ZoneId ALMATY = ZoneId.of("Asia/Almaty");

    @Test
    void monthBoundariesAreInTheGivenZone() {
        Instant instant = OffsetDateTime.parse("2022-01-13T15:00:00+06:00").toInstant();

        assertThat(DateTimeUtils.startOfMonth(instant, ALMATY))
                .isEqualTo(OffsetDateTime.parse("2022-01-01T00:00:00+06:00").toInstant());
        assertThat(DateTimeUtils.startOfNextMonth(instant, ALMATY))
                .isEqualTo(OffsetDateTime.parse("2022-02-01T00:00:00+06:00").toInstant());
    }

    @Test
    void earlyMorningInAlmatyBelongsToNewMonthAlthoughUtcIsStillPreviousMonth() {
        // 2022-02-01 00:30 in Almaty is 2022-01-31 18:30 UTC
        Instant instant = OffsetDateTime.parse("2022-02-01T00:30:00+06:00").toInstant();

        assertThat(DateTimeUtils.startOfMonth(instant, ALMATY))
                .isEqualTo(OffsetDateTime.parse("2022-02-01T00:00:00+06:00").toInstant());
    }

    @Test
    void usesHistoricalOffsetOfTheZone() {
        // Kazakhstan moved from UTC+6 to UTC+5 in March 2024
        Instant instant = OffsetDateTime.parse("2025-01-10T12:00:00+05:00").toInstant();

        assertThat(DateTimeUtils.startOfMonth(instant, ALMATY))
                .isEqualTo(OffsetDateTime.parse("2025-01-01T00:00:00+05:00").toInstant());
    }
}
