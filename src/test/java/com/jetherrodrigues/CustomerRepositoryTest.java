package com.jetherrodrigues;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class CustomerRepositoryTest {
	@Container
	static PostgreSQLContainer<?> pgsql = new PostgreSQLContainer<>("postgres:12");

	@DynamicPropertySource
	static void postgresTestContainersProperties(final DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", pgsql::getJdbcUrl);
		registry.add("spring.datasource.username", pgsql::getUsername);
		registry.add("spring.datasource.password", pgsql::getPassword);
	}

	@Autowired
	private CustomerRepository customerRepository;

	@Test
	@DisplayName("Should load postgres, insert customer and assert that insert successfully")
	void shouldInsertCustomerSuccessfully() {
		final String customerName = "Jether Rodrigues";

		assertFalse(customerRepository.findAll().iterator().hasNext(), "Must be no data!");

		final Customer customer = customerRepository.save(new Customer(customerName));

		assertTrue(customerRepository.findAll().iterator().hasNext(), "Must be some data!");
		assertNotNull(customer.getId());
		assertEquals(customer.getName(), customerName);
	}
}
