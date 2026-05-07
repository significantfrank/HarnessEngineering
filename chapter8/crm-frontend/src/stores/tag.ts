import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Tag } from '@/types/tag'
import { getTags, createTag, updateTag, deleteTag } from '@/api/tag'

export const useTagStore = defineStore('tag', () => {
  const tags = ref<Tag[]>([])
  const loading = ref(false)

  async function fetchTags() {
    loading.value = true
    try {
      const res = await getTags()
      tags.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function addTag(data: Tag): Promise<Tag> {
    const res = await createTag(data)
    await fetchTags()
    return res.data
  }

  async function editTag(id: number, data: Tag) {
    await updateTag(id, data)
    await fetchTags()
  }

  async function removeTag(id: number) {
    await deleteTag(id)
    await fetchTags()
  }

  return {
    tags,
    loading,
    fetchTags,
    addTag,
    editTag,
    removeTag,
  }
})
