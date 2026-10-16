package org.limir.expensetracker.common;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** Month boundaries of a moment in a given time zone. */
public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    /** First moment of the month that contains the instant, e.g. 2022-01-01T00:00 in the zone. */
    public static Instant startOfMonth(Instant instant, ZoneId zone) {
        return firstDayOfMonth(instant, zone).toInstant();
    }

    /** First moment of the next month: the exclusive upper bound of the instant's month. */
    public static Instant startOfNextMonth(Instant instant, ZoneId zone) {
        return firstDayOfMonth(instant, zone).plusMonths(1).toInstant();
    }

    private static ZonedDateTime firstDayOfMonth(Instant instant, ZoneId zone) {
        return instant.atZone(zone).toLocalDate().withDayOfMonth(1).atStartOfDay(zone);
    }
}
