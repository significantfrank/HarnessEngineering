export type CustomerSource = 'REFERRAL' | 'WEBSITE' | 'AD' | 'COLD_CALL' | 'OTHER'
export type CustomerLevel = 'VIP' | 'IMPORTANT' | 'NORMAL' | 'POTENTIAL'
export type CustomerStatus = 'ACTIVE' | 'INACTIVE' | 'LOST'

export interface Customer {
  id?: number
  name: string
  phone?: string
  email?: string
  company?: string
  address?: string
  source?: CustomerSource
  level?: CustomerLevel
  industry?: string
  website?: string
  contactPerson?: string
  lastFollowUp?: string
  status?: CustomerStatus
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface CustomerQuery {
  name?: string
  status?: CustomerStatus
  source?: CustomerSource
  level?: CustomerLevel
  page?: number
  size?: number
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface ApiResponse<T> {
  code: string
  message: string
  data: T
}
