import request from './request'
import type { CustomerNote } from '@/types/common'
import type { PageResult, ApiResponse } from '@/types/common'

export function createCustomerNote(customerId: number, data: CustomerNote): Promise<ApiResponse<CustomerNote>> {
  return request.post(`/api/customers/${customerId}/notes`, data)
}

export function getCustomerNotes(customerId: number, params?: { page?: number; size?: number }): Promise<ApiResponse<PageResult<CustomerNote>>> {
  return request.get(`/api/customers/${customerId}/notes`, { params })
}
