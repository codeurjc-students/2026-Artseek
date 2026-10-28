package com.artseek.common;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** Full application context with PostgreSQL and rollback of test-thread writes. */
@SpringBootTest
@Transactional
public abstract class FullContextIntegrationTest extends IntegrationTest {
}
