import type { CustomerSource } from './common'

export type { CustomerSource }
export type LeadStatus = 'NEW' | 'CONTACTED' | 'QUALIFIED' | 'UNQUALIFIED' | 'CONVERTED'

export interface Lead {
  id?: number
  name: string
  phone?: string
  email?: string
  company?: string
  source?: CustomerSource
  status?: LeadStatus
  customerId?: number
  ownerName?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface LeadQuery {
  name?: string
  status?: LeadStatus
  source?: CustomerSource
  page?: number
  size?: number
}

export interface LeadConvertData {
  opportunityTitle: string
  amount?: number
  expectedCloseDate?: string
}
