import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CustomerNote } from '@/types/common'
import { createCustomerNote, getCustomerNotes } from '@/api/customerNote'

export const useCustomerNoteStore = defineStore('customerNote', () => {
  const notes = ref<CustomerNote[]>([])
  const total = ref(0)
  const loading = ref(false)

  async function fetchNotes(customerId: number, page = 0, size = 10) {
    loading.value = true
    try {
      const res = await getCustomerNotes(customerId, { page, size })
      notes.value = res.data.content
      total.value = res.data.totalElements
    } finally {
      loading.value = false
    }
  }

  async function addNote(customerId: number, data: CustomerNote) {
    await createCustomerNote(customerId, data)
    await fetchNotes(customerId)
  }

  return {
    notes,
    total,
    loading,
    fetchNotes,
    addNote,
  }
})
