package com.artseek.common;

import com.artseek.infrastructure.config.PostgresTestConfiguration;
import org.springframework.context.annotation.Import;

/** Shared database support; subclasses choose their own Spring test slice. */
@Import(PostgresTestConfiguration.class)
public abstract class IntegrationTest {
}
