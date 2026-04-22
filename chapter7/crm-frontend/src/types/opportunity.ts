import type { CustomerSource } from './common'

export type { CustomerSource }
export type OppStage = 'PROSPECTING' | 'QUALIFYING' | 'PROPOSAL' | 'NEGOTIATION' | 'WON' | 'LOST'

export interface Opportunity {
  id?: number
  title: string
  customerId: number
  amount?: number
  stage?: OppStage
  probability?: number
  expectedCloseDate?: string
  leadId?: number
  ownerName?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface OpportunityQuery {
  title?: string
  stage?: OppStage
  customerId?: number
  ownerName?: string
  page?: number
  size?: number
}

export interface KanbanData {
  PROSPECTING: Opportunity[]
  QUALIFYING: Opportunity[]
  PROPOSAL: Opportunity[]
  NEGOTIATION: Opportunity[]
  WON: Opportunity[]
  LOST: Opportunity[]
}
