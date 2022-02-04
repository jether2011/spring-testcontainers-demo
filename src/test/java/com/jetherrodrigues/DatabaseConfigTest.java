package com.jetherrodrigues;

import org.junit.jupiter.api.AfterAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public abstract class DatabaseConfigTest {
    @Container
    static PostgreSQLContainer<?> pgsql = new PostgreSQLContainer<>("postgres:12");

    @DynamicPropertySource
    static void postgresTestContainersProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pgsql::getJdbcUrl);
        registry.add("spring.datasource.username", pgsql::getUsername);
        registry.add("spring.datasource.password", pgsql::getPassword);
    }

    @AfterAll
    static void afterAll() {
        pgsql.close();
    }
}
