package org.limir.expensetracker;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/** Clock for tests whose time can be moved, e.g. to reproduce the scenarios from the assignment table. */
public class MutableClock extends Clock {

    private volatile Instant instant;
    private final ZoneId zone;

    public MutableClock(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    /** Moves the clock, e.g. {@code setTime("2022-01-10T00:00:00+06:00")}. */
    public void setTime(String isoDateTime) {
        this.instant = OffsetDateTime.parse(isoDateTime).toInstant();
    }

    @Override
    public Instant instant() {
        return instant;
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(instant, zone);
    }
}
