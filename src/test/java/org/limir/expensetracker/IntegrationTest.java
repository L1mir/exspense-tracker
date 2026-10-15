package org.limir.expensetracker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;

/**
 * Marks a test class as an integration test: full Spring context, the {@code test} profile
 * and PostgreSQL in Testcontainers. Use it instead of a bare {@link SpringBootTest}, otherwise
 * the default {@code dev} profile would try to connect to a local database.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public @interface IntegrationTest {

    /** Web environment of the test context, e.g. {@code RANDOM_PORT} for end-to-end HTTP tests. */
    @AliasFor(annotation = SpringBootTest.class, attribute = "webEnvironment")
    SpringBootTest.WebEnvironment webEnvironment() default SpringBootTest.WebEnvironment.MOCK;
}
