package org.limir.expensetracker;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

@IntegrationTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ExpenseTrackerApplicationIT {

    @LocalServerPort
    private int port;

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Test
    void healthIsUpIncludingDatabase() {
        String health = restClientBuilder.baseUrl("http://localhost:" + port).build()
                .get().uri("/actuator/health")
                .retrieve()
                .body(String.class);

        assertThat(health).contains("\"status\":\"UP\"").contains("\"db\"");
    }
}
