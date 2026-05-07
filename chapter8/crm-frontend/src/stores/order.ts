import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Order, OrderQuery } from '@/types/order'
import { getOrders, createOrder, updateOrder, deleteOrder } from '@/api/order'

export const useOrderStore = defineStore('order', () => {
  const orders = ref<Order[]>([])
  const total = ref(0)
  const loading = ref(false)
  const query = ref<OrderQuery>({
    page: 0,
    size: 10,
  })

  async function fetchList() {
    loading.value = true
    try {
      const res = await getOrders(query.value)
      orders.value = res.data.content
      total.value = res.data.totalElements
    } finally {
      loading.value = false
    }
  }

  async function addOrder(data: Order) {
    await createOrder(data)
    await fetchList()
  }

  async function editOrder(id: number, data: Order) {
    await updateOrder(id, data)
    await fetchList()
  }

  async function removeOrder(id: number) {
    await deleteOrder(id)
    await fetchList()
  }

  function updateQuery(newQuery: Partial<OrderQuery>) {
    query.value = { ...query.value, ...newQuery }
  }

  function resetQuery() {
    query.value = { page: 0, size: 10 }
  }

  return {
    orders,
    total,
    loading,
    query,
    fetchList,
    addOrder,
    editOrder,
    removeOrder,
    updateQuery,
    resetQuery,
  }
})
