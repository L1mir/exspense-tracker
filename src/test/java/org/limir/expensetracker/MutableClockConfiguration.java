package org.limir.expensetracker;

import java.time.Instant;
import org.limir.expensetracker.config.AppProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Replaces the system clock with a {@link MutableClock} that tests can move. */
@TestConfiguration(proxyBeanMethods = false)
public class MutableClockConfiguration {

    @Bean
    @Primary
    MutableClock mutableClock(AppProperties properties) {
        return new MutableClock(Instant.now(), properties.timeZone());
    }
}
