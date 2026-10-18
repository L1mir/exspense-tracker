package org.limir.expensetracker.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * springdoc builds schemas with swagger-core on Jackson 2, which does not see the application's
     * SNAKE_CASE setting (Jackson 3). Without this Swagger shows {@code accountFrom} instead of {@code account_from}.
     */
    @Bean
    public ModelResolver modelResolver() {
        return new ModelResolver(Json.mapper().copy().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE));
    }

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Expense Tracker")
                .version("v1")
                .description("Tracks expense transactions against monthly USD limits per category. "
                        + "Dates in responses are in the Asia/Almaty time zone."));
    }

    @Bean
    public GroupedOpenApi integrationApi() {
        return GroupedOpenApi.builder()
                .group("integration")
                .displayName("Integration API (bank systems)")
                .pathsToMatch("/integration/**")
                .build();
    }

    @Bean
    public GroupedOpenApi clientApi() {
        return GroupedOpenApi.builder()
                .group("client")
                .displayName("Client API")
                .pathsToMatch("/client/**")
                .build();
    }
}
