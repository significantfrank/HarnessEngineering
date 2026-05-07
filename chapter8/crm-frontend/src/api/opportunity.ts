import request from './request'
import type { Opportunity, OpportunityQuery, KanbanData } from '@/types/opportunity'
import type { PageResult, ApiResponse } from '@/types/common'

export function getOpportunities(params: OpportunityQuery): Promise<ApiResponse<PageResult<Opportunity>>> {
  return request.get('/api/opportunities', { params })
}

export function getOpportunity(id: number): Promise<ApiResponse<Opportunity>> {
  return request.get(`/api/opportunities/${id}`)
}

export function createOpportunity(data: Opportunity): Promise<ApiResponse<Opportunity>> {
  return request.post('/api/opportunities', data)
}

export function updateOpportunity(id: number, data: Opportunity): Promise<ApiResponse<Opportunity>> {
  return request.put(`/api/opportunities/${id}`, data)
}

export function deleteOpportunity(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/api/opportunities/${id}`)
}

export function getKanbanData(): Promise<ApiResponse<KanbanData>> {
  return request.get('/api/opportunities/kanban')
}

export function updateOppStage(id: number, stage: string): Promise<ApiResponse<Opportunity>> {
  return request.patch(`/api/opportunities/${id}/stage`, { stage })
}

export function winOpportunity(id: number, data: { amount: number; remark?: string }): Promise<ApiResponse<any>> {
  return request.post(`/api/opportunities/${id}/win`, data)
}
