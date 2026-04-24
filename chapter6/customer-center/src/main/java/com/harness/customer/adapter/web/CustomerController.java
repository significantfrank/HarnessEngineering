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
    public ApiResponse<CustomerDTO> create(@Valid @RequestBody CustomerDTO dto) {
        Customer customer = CustomerAssembler.toEntity(dto);
        Customer saved = customerApplicationService.createCustomer(customer);
        return ApiResponse.success(CustomerAssembler.toDTO(saved));
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerDTO> get(@PathVariable Long id) {
        Customer customer = customerApplicationService.getCustomer(id);
        return ApiResponse.success(CustomerAssembler.toDTO(customer));
    }

    @GetMapping("/idNumber")
    public ApiResponse<CustomerDTO> getByIdNumber(@RequestParam String idNumber) {
        Customer customer = customerApplicationService.getCustomerByIdNumber(idNumber);
        return ApiResponse.success(CustomerAssembler.toDTO(customer));
    }

    @GetMapping
    public ApiResponse<List<CustomerDTO>> list(@RequestParam(required = false) String name,
                                               @RequestParam(required = false) String phone,
                                               @RequestParam(required = false) AccountStatus accountStatus) {
        List<Customer> customers = customerApplicationService.listCustomers(name, phone, accountStatus);
        return ApiResponse.success(customers.stream().map(CustomerAssembler::toDTO).toList());
    }

    @PutMapping("/{id}")
    public ApiResponse<CustomerDTO> update(@PathVariable Long id, @Valid @RequestBody CustomerDTO dto) {
        Customer customer = CustomerAssembler.toEntity(dto);
        Customer updated = customerApplicationService.updateCustomer(id, customer);
        return ApiResponse.success(CustomerAssembler.toDTO(updated));
    }

    @PutMapping("/{id}/freeze")
    public ApiResponse<CustomerDTO> freeze(@PathVariable Long id, @Valid @RequestBody FreezeRequestDTO request) {
        Customer customer = customerApplicationService.freezeCustomer(id, request.getReason());
        return ApiResponse.success(CustomerAssembler.toDTO(customer));
    }

    @PutMapping("/{id}/unfreeze")
    public ApiResponse<CustomerDTO> unfreeze(@PathVariable Long id, @Valid @RequestBody UnfreezeRequestDTO request) {
        Customer customer = customerApplicationService.unfreezeCustomer(id, request.getReason(), request.getAuthorizationDocument());
        return ApiResponse.success(CustomerAssembler.toDTO(customer));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        customerApplicationService.deleteCustomer(id);
    }
}
