package org.limir.expensetracker.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.limir.expensetracker.config.AppProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DateTimeMapper {

    private final AppProperties properties;

    public OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null : instant.atZone(properties.timeZone()).toOffsetDateTime();
    }
}
