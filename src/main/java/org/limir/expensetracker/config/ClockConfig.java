package org.limir.expensetracker.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Current time is always taken from this {@link Clock} bean, never from {@code Instant.now()} directly.
 * Tests replace it with a fixed clock to reproduce scenarios on specific dates.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(AppProperties properties) {
        return Clock.system(properties.timeZone());
    }
}
