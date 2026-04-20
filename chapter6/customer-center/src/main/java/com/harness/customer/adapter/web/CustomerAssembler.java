package com.harness.customer.adapter.web;

import com.harness.customer.domain.entity.Customer;
import com.harness.customer.domain.entity.HoldingProduct;

import java.util.Collections;
import java.util.List;

public class CustomerAssembler {

    public static Customer toEntity(CustomerDTO dto) {
        return Customer.builder()
                .name(dto.getName())
                .idType(dto.getIdType())
                .idNumber(dto.getIdNumber())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .gender(dto.getGender())
                .birthday(dto.getBirthday())
                .memberLevel(dto.getMemberLevel())
                .authLevel(dto.getAuthLevel())
                .idPhotoUrl(dto.getIdPhotoUrl())
                .riskProfile(dto.getRiskProfile())
                .occupation(dto.getOccupation())
                .incomeRange(dto.getIncomeRange())
                .aum(dto.getAum())
                .availableBalance(dto.getAvailableBalance())
                .totalReturn(dto.getTotalReturn())
                .holdingProducts(toHoldingProductList(dto.getHoldingProducts()))
                .build();
    }

    public static CustomerDTO toDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setIdType(customer.getIdType());
        dto.setIdNumber(customer.getIdNumber());
        dto.setPhone(customer.getPhone());
        dto.setEmail(customer.getEmail());
        dto.setGender(customer.getGender());
        dto.setBirthday(customer.getBirthday());
        dto.setAccountStatus(customer.getAccountStatus());
        dto.setMemberLevel(customer.getMemberLevel());
        dto.setAuthLevel(customer.getAuthLevel());
        dto.setIdPhotoUrl(customer.getIdPhotoUrl());
        dto.setRiskProfile(customer.getRiskProfile());
        dto.setOccupation(customer.getOccupation());
        dto.setIncomeRange(customer.getIncomeRange());
        dto.setAum(customer.getAum());
        dto.setAvailableBalance(customer.getAvailableBalance());
        dto.setTotalReturn(customer.getTotalReturn());
        dto.setHoldingProducts(toHoldingProductDTOList(customer.getHoldingProducts()));
        dto.setCreateTime(customer.getCreateTime());
        dto.setUpdateTime(customer.getUpdateTime());
        return dto;
    }

    private static List<HoldingProduct> toHoldingProductList(List<CustomerDTO.HoldingProductDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream().map(d -> {
            HoldingProduct hp = new HoldingProduct();
            hp.setProductType(d.getProductType());
            hp.setProductCode(d.getProductCode());
            hp.setProductName(d.getProductName());
            hp.setAmount(d.getAmount());
            return hp;
        }).toList();
    }

    private static List<CustomerDTO.HoldingProductDTO> toHoldingProductDTOList(List<HoldingProduct> products) {
        if (products == null) return Collections.emptyList();
        return products.stream().map(p -> new CustomerDTO.HoldingProductDTO(
                p.getProductType(), p.getProductCode(), p.getProductName(), p.getAmount()
        )).toList();
    }
}
