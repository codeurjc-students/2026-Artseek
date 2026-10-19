package com.artseek.infrastructure.api.health;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class HealthEndpointSystemTests {

    @Autowired
    private RestTestClient restTestClient;

    @Test
    void reportsHealthThroughTheRunningHttpServer() {
        restTestClient.get()
                .uri("/api/health")
                .exchange()
                .expectStatus().isOk();
    }
}
