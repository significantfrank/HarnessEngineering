package com.harness.customer.infrastructure.repository;

import com.harness.customer.domain.entity.AccountStatus;
import com.harness.customer.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerJpaRepository extends JpaRepository<Customer, Long> {

    boolean existsByIdNumber(String idNumber);

    boolean existsByIdNumberAndIdNot(String idNumber, Long id);

    @Query("SELECT c FROM Customer c WHERE " +
            "(:name IS NULL OR c.name LIKE %:name%) AND " +
            "(:phone IS NULL OR c.phone LIKE %:phone%) AND " +
            "(:accountStatus IS NULL OR c.accountStatus = :accountStatus)")
    List<Customer> findByCondition(@Param("name") String name,
                                   @Param("phone") String phone,
                                   @Param("accountStatus") AccountStatus accountStatus);
}
