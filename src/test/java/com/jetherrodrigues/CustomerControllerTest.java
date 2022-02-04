package com.jetherrodrigues;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AutoConfigureMockMvc
class CustomerControllerTest extends DatabaseConfigTest {
    private static final String API_V1_CUSTOMER = "/v1/api/customer";

    @Autowired private MockMvc mockMvc;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    @Transactional
    @DisplayName("Should create customer successfully")
    void createCustomer() throws Exception {
        final String customerName = "Jether Rodrigues";
        final Customer toCreate = new Customer(customerName);
        final String customerAsJson = mapper.writeValueAsString(toCreate);

        mockMvc.perform(MockMvcRequestBuilders.post(API_V1_CUSTOMER)
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(customerAsJson))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNumber())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(toCreate.getName()))
                .andDo(MockMvcResultHandlers.print());

        assertTrue(customerRepository.findAll().iterator().hasNext());
    }

    @Test
    @Transactional
    @DisplayName("Should get all customer successfully")
    void getAllCustomer() throws Exception {
        final int expectedSizeOfCustomers = 4;
        final Customer jether = new Customer("Jether");
        final Customer denise = new Customer("Denise");
        final Customer beatriz = new Customer("Beatriz");
        final Customer isabelly = new Customer("Isabelly");
        final List<Customer> customersToSave = Arrays.asList(jether, denise, beatriz, isabelly);

        customerRepository.saveAll(customersToSave);

        final MvcResult customersResult =
                mockMvc.perform(MockMvcRequestBuilders.get(API_V1_CUSTOMER)
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                        .andReturn();

        final List<Customer> customers = mapper.readValue(customersResult.getResponse().getContentAsString(),
                new TypeReference<>(){});

        assertEquals(customers.size(), expectedSizeOfCustomers);
        assertTrue(customers.stream().anyMatch(customer -> customer.getName().equals(jether.getName())));
    }

    @Test
    @Transactional
    @DisplayName("Should get customer by id successfully")
    void getCustomerById() throws Exception {
        final Customer jether = new Customer("Jether");
        final String jetherAsJson = mapper.writeValueAsString(jether);

        final MvcResult customerCreatedResult =
                mockMvc.perform(MockMvcRequestBuilders.post(API_V1_CUSTOMER)
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON)
                                .content(jetherAsJson))
                        .andExpect(MockMvcResultMatchers.status().isCreated())
                        .andDo(MockMvcResultHandlers.print())
                        .andReturn();
        final Customer createdCustomer = mapper.readValue(customerCreatedResult.getResponse().getContentAsString(),
                Customer.class);

        final MvcResult customerResult =
                mockMvc.perform(MockMvcRequestBuilders.get(API_V1_CUSTOMER + "/" + createdCustomer.getId())
                                .accept(APPLICATION_JSON)
                                .contentType(APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andDo(MockMvcResultHandlers.print())
                        .andReturn();

        final Customer customer = mapper.readValue(customerResult.getResponse().getContentAsString(),
                Customer.class);

        assertEquals(customer.getId(), createdCustomer.getId());
        assertEquals(customer.getName(), createdCustomer.getName());
    }

    @Test
    @Transactional(readOnly = true)
    @DisplayName("Should return not found when try to get customer by id")
    void shouldReturnNotFoundWhenTryToGetCustomerById() throws Exception {
        final long fakeId = 999;
        final int expectedNotFoundStatus = 404;
        final String expectedError = "NOT_FOUND";
        final String expectedErrorMessage = "Customer not found for [ " + fakeId + " ]";

        final MvcResult errorResult =
                mockMvc.perform(MockMvcRequestBuilders.get(API_V1_CUSTOMER + "/" + fakeId)
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        final ErrorResponse error = mapper.readValue(errorResult.getResponse().getContentAsString(),
                ErrorResponse.class);

        assertEquals(error.getError(), expectedError);
        assertEquals(error.getMessage(), expectedErrorMessage);
        assertEquals(error.getStatus(), expectedNotFoundStatus);
    }
}
