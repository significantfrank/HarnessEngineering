package com.harness.crm.app.order.convertor;

import com.harness.crm.app.order.dto.LegacyOrderDTO;
import com.harness.crm.app.order.dto.OrderDTO;

public class OrderConvertor {

    /** 将遗留系统DTO转换为新系统DTO */
    public static OrderDTO toOrderDTO(LegacyOrderDTO legacyOrderDTO) {
        return OrderDTO.builder()
                .customerId(legacyOrderDTO.getCustomerId())
                .opportunityId(legacyOrderDTO.getOpportunityId())
                .totalAmount(legacyOrderDTO.getTotalAmount())
                .email(legacyOrderDTO.getEmail())
                .customerLevel(legacyOrderDTO.getCustomerLevel())
                .productType(legacyOrderDTO.getProductType())
                .ownerName(legacyOrderDTO.getOwnerName())
                .remark(legacyOrderDTO.getRemark())
                .build();
    }

    /** 将新系统DTO转换为遗留系统DTO */
    public static LegacyOrderDTO toLegacyOrderDTO(OrderDTO orderDTO) {
        return LegacyOrderDTO.builder()
                .customerId(orderDTO.getCustomerId())
                .opportunityId(orderDTO.getOpportunityId())
                .totalAmount(orderDTO.getTotalAmount())
                .email(orderDTO.getEmail())
                .customerLevel(orderDTO.getCustomerLevel())
                .productType(orderDTO.getProductType())
                .ownerName(orderDTO.getOwnerName())
                .remark(orderDTO.getRemark())
                .build();
    }

}
