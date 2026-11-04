package com.artseek.infrastructure.api.health;

import org.junit.jupiter.api.Test;

import com.artseek.shared.SystemTest;

class HealthEndpointSystemTests extends SystemTest {

    @Test
    void reportsHealthThroughTheRunningHttpServer() {
        restTestClient.get()
                .uri("/api/health")
                .exchange()
                .expectStatus().isOk();
    }
}
