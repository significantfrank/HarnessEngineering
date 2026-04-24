package com.harness.crm.app.customer;

import com.harness.crm.domain.customer.data.CustomerCenterData;
import com.harness.crm.infrastructure.customer.gateway.CustomerCenterGatewayImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CustomerCenterGatewayImplTest {

    private RestTemplate restTemplate;
    private CustomerCenterGatewayImpl gateway;

    @BeforeEach
    void setUp() throws Exception {
        restTemplate = mock(RestTemplate.class);
        gateway = new CustomerCenterGatewayImpl("http://localhost:8081");
        Field field = CustomerCenterGatewayImpl.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(gateway, restTemplate);
    }

    @SuppressWarnings("unchecked")
    private void mockListResponse(List<Map<String, Object>> customers) {
        Map<String, Object> apiResponse = Map.of("code", "200", "data", Map.of("content", customers));
        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));
    }

    @Test
    void findByIdNumber_shouldReturnMatchedCustomer() {
        mockListResponse(List.of(
                Map.of("id", 1, "name", "张三", "phone", "13800001111", "email", "z@test.com",
                        "idType", "ID_CARD", "idNumber", "110101199001011234")
        ));

        Optional<CustomerCenterData> result = gateway.findByIdNumber("110101199001011234");

        assertTrue(result.isPresent());
        assertEquals("张三", result.get().getName());
        assertEquals("110101199001011234", result.get().getIdNumber());
    }

    @Test
    void findByIdNumber_shouldReturnEmptyWhenNotMatched() {
        mockListResponse(List.of(
                Map.of("id", 1, "name", "张三", "idNumber", "110101199001011234")
        ));

        Optional<CustomerCenterData> result = gateway.findByIdNumber("999999999999999999");

        assertTrue(result.isEmpty());
    }

    @Test
    void isAvailable_shouldReturnTrueWhenReachable() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        assertTrue(gateway.isAvailable());
    }

    @Test
    void isAvailable_shouldReturnFalseWhenUnreachable() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        assertFalse(gateway.isAvailable());
    }

    @Test
    void createOrSync_shouldCreateWhenNotExists() {
        mockListResponse(List.of());
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("code", "200")));

        assertDoesNotThrow(() -> gateway.createOrSync("张三", "13800001111", "z@test.com", "ID_CARD", "110101199001011234"));
        verify(restTemplate).postForEntity(contains("/api/customers"), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void createOrSync_shouldRetryAndThrowOnFailure() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThrows(RuntimeException.class,
                () -> gateway.createOrSync("张三", "13800001111", "z@test.com", "ID_CARD", "110101199001011234"));
    }
}
