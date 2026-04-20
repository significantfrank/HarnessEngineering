package com.harness.customer.adapter.web;

import com.harness.customer.app.service.CustomerApplicationService;
import com.harness.customer.domain.entity.AccountStatus;
import com.harness.customer.domain.entity.Customer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerApplicationService customerApplicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDTO create(@Valid @RequestBody CustomerDTO dto) {
        Customer customer = CustomerAssembler.toEntity(dto);
        Customer saved = customerApplicationService.createCustomer(customer);
        return CustomerAssembler.toDTO(saved);
    }

    @GetMapping("/{id}")
    public CustomerDTO get(@PathVariable Long id) {
        Customer customer = customerApplicationService.getCustomer(id);
        return CustomerAssembler.toDTO(customer);
    }

    @GetMapping
    public List<CustomerDTO> list(@RequestParam(required = false) String name,
                                  @RequestParam(required = false) String phone,
                                  @RequestParam(required = false) AccountStatus accountStatus) {
        List<Customer> customers = customerApplicationService.listCustomers(name, phone, accountStatus);
        return customers.stream().map(CustomerAssembler::toDTO).toList();
    }

    @PutMapping("/{id}")
    public CustomerDTO update(@PathVariable Long id, @Valid @RequestBody CustomerDTO dto) {
        Customer customer = CustomerAssembler.toEntity(dto);
        Customer updated = customerApplicationService.updateCustomer(id, customer);
        return CustomerAssembler.toDTO(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        customerApplicationService.deleteCustomer(id);
    }
}
