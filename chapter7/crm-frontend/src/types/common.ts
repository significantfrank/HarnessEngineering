export type CustomerSource = 'REFERRAL' | 'WEBSITE' | 'AD' | 'COLD_CALL' | 'OTHER'
export type CustomerLevel = 'VIP' | 'IMPORTANT' | 'NORMAL' | 'POTENTIAL'
export type CustomerStatus = 'ACTIVE' | 'INACTIVE' | 'LOST'
export type CcSyncStatus = 'PENDING' | 'SYNCED' | 'FAILED'
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
  idType?: string
  idNumber?: string
  ccSyncStatus?: CcSyncStatus
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

export interface HoldingProduct {
  productType?: string
  productCode?: string
  productName?: string
  amount?: number
}

export interface Customer360 {
  id?: number
  name: string
  phone?: string
  email?: string
  idType?: string
  idNumber?: string
  ccSyncStatus?: CcSyncStatus
  source?: CustomerSource
  level?: CustomerLevel
  status?: CustomerStatus
  lastFollowUp?: string
  remark?: string
  company?: string
  address?: string
  industry?: string
  website?: string
  contactPerson?: string
  tags?: Tag[]
  notes?: CustomerNote[]
  gender?: string
  birthday?: string
  occupation?: string
  accountStatus?: string
  memberLevel?: string
  authLevel?: string
  riskProfile?: string
  incomeRange?: string
  aum?: number
  availableBalance?: number
  totalReturn?: number
  holdingProducts?: HoldingProduct[]
  degraded?: boolean
  createTime?: string
  updateTime?: string
}
