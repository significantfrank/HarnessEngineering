import request from './request'
import type { Order, OrderQuery } from '@/types/order'
import type { PageResult, ApiResponse } from '@/types/common'

export function getOrders(params: OrderQuery): Promise<ApiResponse<PageResult<Order>>> {
  return request.get('/api/orders', { params })
}

export function getOrder(id: number): Promise<ApiResponse<Order>> {
  return request.get(`/api/orders/${id}`)
}

export function createOrder(data: Order): Promise<ApiResponse<Order>> {
  return request.post('/api/orders', data)
}

export function updateOrder(id: number, data: Order): Promise<ApiResponse<Order>> {
  return request.put(`/api/orders/${id}`, data)
}

export function deleteOrder(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/api/orders/${id}`)
}
