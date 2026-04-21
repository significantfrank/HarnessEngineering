package com.harness.customer.domain.gateway;

public interface JudicialAuthorizationGateway {

    boolean verify(String authorizationDocument);
}
