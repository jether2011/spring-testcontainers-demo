package com.jetherrodrigues;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@SpringBootApplication
public class SpringDemoTestContainersApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringDemoTestContainersApplication.class, args);
	}

}

@Entity
class Customer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@NotNull
	@NotEmpty
	private String name;

	Customer() {}

	Customer(final String name) {
		this.id = null;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

@Repository
interface CustomerRepository extends CrudRepository<Customer, Long> {}

@RestController
@RequestMapping("/v1/api/customer")
class CustomerController {
	private final CustomerRepository customerRepository;

	CustomerController(final CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@PostMapping
	public ResponseEntity<Customer> createCustomer(@RequestBody @Validated Customer customer) {
		final Customer created = customerRepository.save(customer);
		return ResponseEntity.created(URI.create("/v1/api/customer/" + customer.getId())).body(created);
	}

	@GetMapping
	public  ResponseEntity<Iterable<Customer>> getAll() {
		return ResponseEntity.ok(customerRepository.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Customer> getById(@PathVariable("id") final long id) {
		final Customer customer = customerRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Customer not found for [ " + id + " ]"));
		return ResponseEntity.ok().body(customer);
	}
}

@RestControllerAdvice
class RestExceptionHandler {
	@ExceptionHandler(NotFoundException.class)
	ResponseEntity<ErrorResponse> handleNotFoundException(final NotFoundException ex) {
		final ErrorResponse error = new ErrorResponse("NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND.value());
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}
}

final class ErrorResponse {
	private final String error;
	private final String message;
	private final int status;
	private final long timestamp;

	ErrorResponse(final String error, final String message, final int status) {
		this.error = error;
		this.message = message;
		this.status = status;
		this.timestamp = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
	}

	public String getError() {
		return error;
	}

	public String getMessage() {
		return message;
	}

	public int getStatus() {
		return status;
	}

	public long getTimestamp() {
		return timestamp;
	}
}

class NotFoundException extends RuntimeException {
	NotFoundException(final String message) {
		super(message);
	}
}