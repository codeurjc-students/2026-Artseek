package com.artseek.infrastructure.api.health;

import com.artseek.common.SystemTest;
import org.junit.jupiter.api.Test;

class HealthEndpointSystemTests extends SystemTest {

    @Test
    void reportsHealthThroughTheRunningHttpServer() {
        restTestClient.get()
                .uri("/api/health")
                .exchange()
                .expectStatus().isOk();
    }
}
