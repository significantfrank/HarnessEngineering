package com.harness.customer.infrastructure.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harness.customer.domain.entity.HoldingProduct;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;

@Converter
public class HoldingProductListConverter implements AttributeConverter<List<HoldingProduct>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<HoldingProduct> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert HoldingProduct list to JSON", e);
        }
    }

    @Override
    public List<HoldingProduct> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(dbData, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert JSON to HoldingProduct list", e);
        }
    }
}
