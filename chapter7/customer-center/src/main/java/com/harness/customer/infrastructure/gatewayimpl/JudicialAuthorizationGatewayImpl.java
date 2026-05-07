package com.harness.customer.infrastructure.gatewayimpl;

import com.harness.customer.domain.gateway.JudicialAuthorizationGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JudicialAuthorizationGatewayImpl implements JudicialAuthorizationGateway {

    @Override
    public boolean verify(String authorizationDocument) {
        log.info("Verifying judicial authorization document: {}", authorizationDocument);
        if (authorizationDocument == null || authorizationDocument.isBlank()) {
            return false;
        }
        return authorizationDocument.matches("^JUD-\\d{4}-\\w{8,}$");
    }
}
