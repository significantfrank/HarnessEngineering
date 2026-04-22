import request from './request'
import type { Customer, CustomerQuery } from '@/types/customer'
import type { PageResult, ApiResponse } from '@/types/common'

export function getCustomers(params: CustomerQuery): Promise<ApiResponse<PageResult<Customer>>> {
  return request.get('/api/customers', { params })
}

export function getCustomer(id: number): Promise<ApiResponse<Customer>> {
  return request.get(`/api/customers/${id}`)
}

export function createCustomer(data: Customer): Promise<ApiResponse<Customer>> {
  return request.post('/api/customers', data)
}

export function updateCustomer(id: number, data: Customer): Promise<ApiResponse<Customer>> {
  return request.put(`/api/customers/${id}`, data)
}

export function deleteCustomer(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/api/customers/${id}`)
}
