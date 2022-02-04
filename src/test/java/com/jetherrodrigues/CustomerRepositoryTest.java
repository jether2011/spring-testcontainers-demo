package com.jetherrodrigues;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class CustomerRepositoryTest extends DatabaseConfigTest {
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
