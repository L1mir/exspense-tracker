package org.limir.expensetracker.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static Instant startOfMonth(Instant instant, ZoneId zone) {
        return firstDayOfMonth(instant, zone).toInstant();
    }

    public static Instant startOfNextMonth(Instant instant, ZoneId zone) {
        return firstDayOfMonth(instant, zone).plusMonths(1).toInstant();
    }

    private static ZonedDateTime firstDayOfMonth(Instant instant, ZoneId zone) {
        return instant.atZone(zone).toLocalDate().withDayOfMonth(1).atStartOfDay(zone);
    }
}
