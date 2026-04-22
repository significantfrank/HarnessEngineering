import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Opportunity, OpportunityQuery, KanbanData } from '@/types/opportunity'
import { getOpportunities, createOpportunity, updateOpportunity, deleteOpportunity, getKanbanData } from '@/api/opportunity'

export const useOpportunityStore = defineStore('opportunity', () => {
  const opportunities = ref<Opportunity[]>([])
  const total = ref(0)
  const loading = ref(false)
  const query = ref<OpportunityQuery>({
    page: 0,
    size: 10,
  })
  const kanbanData = ref<KanbanData | null>(null)

  async function fetchList() {
    loading.value = true
    try {
      const res = await getOpportunities(query.value)
      opportunities.value = res.data.content
      total.value = res.data.totalElements
    } finally {
      loading.value = false
    }
  }

  async function fetchKanban() {
    loading.value = true
    try {
      const res = await getKanbanData()
      kanbanData.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function addOpportunity(data: Opportunity) {
    await createOpportunity(data)
    await fetchList()
  }

  async function editOpportunity(id: number, data: Opportunity) {
    await updateOpportunity(id, data)
    await fetchList()
  }

  async function removeOpportunity(id: number) {
    await deleteOpportunity(id)
    await fetchList()
  }

  function updateQuery(newQuery: Partial<OpportunityQuery>) {
    query.value = { ...query.value, ...newQuery }
  }

  function resetQuery() {
    query.value = { page: 0, size: 10 }
  }

  return {
    opportunities,
    total,
    loading,
    query,
    kanbanData,
    fetchList,
    fetchKanban,
    addOpportunity,
    editOpportunity,
    removeOpportunity,
    updateQuery,
    resetQuery,
  }
})
