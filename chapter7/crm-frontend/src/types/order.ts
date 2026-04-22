export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'COMPLETED' | 'CANCELLED'

export interface Order {
  id?: number
  orderNo?: string
  customerId: number
  opportunityId: number
  totalAmount?: number
  status?: OrderStatus
  ownerName?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface OrderQuery {
  orderNo?: string
  status?: OrderStatus
  customerId?: number
  opportunityId?: number
  page?: number
  size?: number
}
