package org.limir.expensetracker.common;

import java.time.Instant;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.limir.expensetracker.config.AppProperties;
import org.springframework.stereotype.Component;

/**
 * Converts stored moments to timestamps for API responses. The database keeps an instant without
 * the original offset, so responses show it in the service time zone (Asia/Almaty).
 * Used by MapStruct mappers for every Instant to OffsetDateTime field.
 */
@Component
@RequiredArgsConstructor
public class DateTimeMapper {

    private final AppProperties properties;

    public OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null : instant.atZone(properties.timeZone()).toOffsetDateTime();
    }
}
