export type CustomerSource = 'REFERRAL' | 'WEBSITE' | 'AD' | 'COLD_CALL' | 'OTHER'
export type CustomerLevel = 'VIP' | 'IMPORTANT' | 'NORMAL' | 'POTENTIAL'
export type CustomerStatus = 'ACTIVE' | 'INACTIVE' | 'LOST'
export type NoteCategory = 'PHONE_CALL' | 'VISIT' | 'EMAIL' | 'WECHAT' | 'OTHER'

export interface Tag {
  id?: number
  name: string
  color: string
  customerCount?: number
  createTime?: string
  updateTime?: string
}

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
  tags?: Tag[]
  tagIds?: number[]
  createTime?: string
  updateTime?: string
}

export interface CustomerQuery {
  name?: string
  status?: CustomerStatus
  source?: CustomerSource
  level?: CustomerLevel
  tagIds?: number[]
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

export interface CustomerNote {
  id?: number
  customerId?: number
  category: NoteCategory
  content: string
  createTime?: string
  updateTime?: string
}
