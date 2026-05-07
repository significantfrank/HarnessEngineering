import request from './request'
import type { Lead, LeadQuery, LeadConvertData } from '@/types/lead'
import type { PageResult, ApiResponse } from '@/types/common'

export function getLeads(params: LeadQuery): Promise<ApiResponse<PageResult<Lead>>> {
  return request.get('/api/leads', { params })
}

export function getLead(id: number): Promise<ApiResponse<Lead>> {
  return request.get(`/api/leads/${id}`)
}

export function createLead(data: Lead): Promise<ApiResponse<Lead>> {
  return request.post('/api/leads', data)
}

export function updateLead(id: number, data: Lead): Promise<ApiResponse<Lead>> {
  return request.put(`/api/leads/${id}`, data)
}

export function deleteLead(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/api/leads/${id}`)
}

export function convertLead(id: number, data: LeadConvertData): Promise<ApiResponse<any>> {
  return request.post(`/api/leads/${id}/convert`, data)
}
