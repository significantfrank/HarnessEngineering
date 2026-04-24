import request from './request'
import type { Customer, CustomerQuery } from '@/types/customer'
import type { Customer360, PageResult, ApiResponse } from '@/types/common'

export function getCustomers(params: CustomerQuery): Promise<ApiResponse<PageResult<Customer>>> {
  const requestParams: Record<string, any> = { ...params }
  if (params.tagIds && params.tagIds.length > 0) {
    requestParams.tagIds = params.tagIds
  } else {
    delete requestParams.tagIds
  }
  return request.get('/api/customers', { params: requestParams })
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

export function getCustomer360(id: number): Promise<ApiResponse<Customer360>> {
  return request.get(`/api/customers/${id}/360`)
}
