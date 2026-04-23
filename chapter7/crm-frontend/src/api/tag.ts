import request from './request'
import type { Tag } from '@/types/tag'
import type { ApiResponse } from '@/types/common'

export function getTags(): Promise<ApiResponse<Tag[]>> {
  return request.get('/api/tags')
}

export function getTag(id: number): Promise<ApiResponse<Tag>> {
  return request.get(`/api/tags/${id}`)
}

export function createTag(data: Tag): Promise<ApiResponse<Tag>> {
  return request.post('/api/tags', data)
}

export function updateTag(id: number, data: Tag): Promise<ApiResponse<Tag>> {
  return request.put(`/api/tags/${id}`, data)
}

export function deleteTag(id: number): Promise<ApiResponse<void>> {
  return request.delete(`/api/tags/${id}`)
}
