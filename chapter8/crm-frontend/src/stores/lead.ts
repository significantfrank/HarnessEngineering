import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Lead, LeadQuery } from '@/types/lead'
import { getLeads, createLead, updateLead, deleteLead } from '@/api/lead'

export const useLeadStore = defineStore('lead', () => {
  const leads = ref<Lead[]>([])
  const total = ref(0)
  const loading = ref(false)
  const query = ref<LeadQuery>({
    page: 0,
    size: 10,
  })

  async function fetchList() {
    loading.value = true
    try {
      const res = await getLeads(query.value)
      leads.value = res.data.content
      total.value = res.data.totalElements
    } finally {
      loading.value = false
    }
  }

  async function addLead(data: Lead) {
    await createLead(data)
    await fetchList()
  }

  async function editLead(id: number, data: Lead) {
    await updateLead(id, data)
    await fetchList()
  }

  async function removeLead(id: number) {
    await deleteLead(id)
    await fetchList()
  }

  function updateQuery(newQuery: Partial<LeadQuery>) {
    query.value = { ...query.value, ...newQuery }
  }

  function resetQuery() {
    query.value = { page: 0, size: 10 }
  }

  return {
    leads,
    total,
    loading,
    query,
    fetchList,
    addLead,
    editLead,
    removeLead,
    updateQuery,
    resetQuery,
  }
})
