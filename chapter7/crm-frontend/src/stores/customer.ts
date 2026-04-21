import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Customer, CustomerQuery } from '@/types/customer'
import { getCustomers, createCustomer, updateCustomer, deleteCustomer } from '@/api/customer'

export const useCustomerStore = defineStore('customer', () => {
  const customers = ref<Customer[]>([])
  const total = ref(0)
  const loading = ref(false)
  const query = ref<CustomerQuery>({
    page: 0,
    size: 10,
  })

  async function fetchList() {
    loading.value = true
    try {
      const res = await getCustomers(query.value)
      customers.value = res.data.content
      total.value = res.data.totalElements
    } finally {
      loading.value = false
    }
  }

  async function addCustomer(data: Customer) {
    await createCustomer(data)
    await fetchList()
  }

  async function editCustomer(id: number, data: Customer) {
    await updateCustomer(id, data)
    await fetchList()
  }

  async function removeCustomer(id: number) {
    await deleteCustomer(id)
    await fetchList()
  }

  function updateQuery(newQuery: Partial<CustomerQuery>) {
    query.value = { ...query.value, ...newQuery }
  }

  function resetQuery() {
    query.value = { page: 0, size: 10 }
  }

  return {
    customers,
    total,
    loading,
    query,
    fetchList,
    addCustomer,
    editCustomer,
    removeCustomer,
    updateQuery,
    resetQuery,
  }
})
