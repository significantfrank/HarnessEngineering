package com.harness.crm.infrastructure.customer.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harness.crm.domain.customer.data.CustomerCenterData;
import com.harness.crm.domain.customer.gateway.CustomerCenterGatewayI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class CustomerCenterGatewayImpl implements CustomerCenterGatewayI {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public CustomerCenterGatewayImpl(@Value("${customer-center.base-url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl;
    }

    @Override
    public void createOrSync(String name, String phone, String email, String idType, String idNumber) {
        int[] delays = {1000, 2000, 4000};
        Exception lastException = null;
        for (int i = 0; i <= delays.length; i++) {
            try {
                doCreateOrSync(name, phone, email, idType, idNumber);
                return;
            } catch (Exception e) {
                lastException = e;
                log.warn("center调用失败(第{}次): {}", i + 1, e.getMessage());
                if (i < delays.length) sleepQuietly(delays[i]);
            }
        }
        throw new RuntimeException("center调用失败，重试3次后仍不成功: " + lastException.getMessage(), lastException);
    }

    @Override
    public void update(String idNumber, String name, String phone, String email, String idType) {
        int[] delays = {1000, 2000, 4000};
        Exception lastException = null;
        for (int i = 0; i <= delays.length; i++) {
            try {
                doUpdate(idNumber, name, phone, email, idType);
                return;
            } catch (Exception e) {
                lastException = e;
                log.warn("center调用失败(第{}次): {}", i + 1, e.getMessage());
                if (i < delays.length) sleepQuietly(delays[i]);
            }
        }
        throw new RuntimeException("center调用失败，重试3次后仍不成功: " + lastException.getMessage(), lastException);
    }

    private void doCreateOrSync(String name, String phone, String email, String idType, String idNumber) {
        Optional<CustomerCenterData> existing = findInCenterByIdNumber(idNumber);
        if (existing.isPresent()) {
            updateCenterCustomer(existing.get().getId(), name, phone, email, idType, idNumber);
        } else {
            createCenterCustomer(name, phone, email, idType, idNumber);
        }
    }

    private void doUpdate(String idNumber, String name, String phone, String email, String idType) {
        Optional<CustomerCenterData> existing = findInCenterByIdNumber(idNumber);
        if (existing.isPresent()) {
            updateCenterCustomer(existing.get().getId(), name, phone, email, idType, idNumber);
        } else {
            createCenterCustomer(name, phone, email, idType, idNumber);
        }
    }

    @Override
    public Optional<CustomerCenterData> findByIdNumber(String idNumber) {
        return findInCenterByIdNumber(idNumber);
    }

    @Override
    public boolean isAvailable() {
        try {
            restTemplate.getForEntity(baseUrl + "/api/customers", String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Optional<CustomerCenterData> findInCenterByIdNumber(String idNumber) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/api/customers/idNumber?idNumber={idNumber}",
                    HttpMethod.GET, null, String.class, Map.of("idNumber", idNumber)
            );
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode codeNode = root.get("code");
            if (codeNode == null || !"200".equals(codeNode.asText())) {
                return Optional.empty();
            }
            JsonNode dataNode = root.get("data");
            if (dataNode == null || dataNode.isNull()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.convertValue(dataNode, CustomerCenterData.class));
        } catch (Exception e) {
            log.warn("查询center客户失败(idNumber={}): {}", idNumber, e.getMessage());
            return Optional.empty();
        }
    }

    private void createCenterCustomer(String name, String phone, String email, String idType, String idNumber) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = Map.of(
                "name", nvl(name), "phone", nvl(phone), "email", nvl(email),
                "idType", nvl(idType), "idNumber", idNumber
        );
        restTemplate.postForEntity(baseUrl + "/api/customers", new HttpEntity<>(body, headers), Map.class);
    }

    private void updateCenterCustomer(Long centerId, String name, String phone, String email, String idType, String idNumber) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = Map.of(
                "name", nvl(name), "phone", nvl(phone), "email", nvl(email),
                "idType", nvl(idType), "idNumber", idNumber
        );
        restTemplate.exchange(baseUrl + "/api/customers/" + centerId,
                HttpMethod.PUT, new HttpEntity<>(body, headers), Map.class);
    }

    private void sleepQuietly(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("重试被中断", ie);
        }
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
