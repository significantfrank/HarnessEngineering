<template>
  <div style="display: inline-flex">
    <a-select
      v-bind="$attrs"
      :value="selectedTagIds"
      mode="multiple"
      :placeholder="placeholder"
      :filter-option="filterOption"
      allow-clear
      @search="handleSearch"
      @change="handleChange"
    >
      <a-select-option v-for="tag in store.tags" :key="tag.id" :value="tag.id">
        <a-tag :color="tag.color" style="margin: 0">{{ tag.name }}</a-tag>
      </a-select-option>
      <a-select-option v-if="searchText && !isExactMatch" :value="'__create__'" disabled>
        <a-button type="link" size="small" @click.stop="showCreateModal" style="padding: 0">
          + 创建「{{ searchText }}」
        </a-button>
      </a-select-option>
    </a-select>

    <a-modal
    :open="createVisible"
    title="创建标签"
    @cancel="createVisible = false"
    @ok="handleCreate"
    :confirm-loading="creating"
    width="360px"
  >
    <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
      <a-form-item label="名称">
        <a-input v-model:value="createForm.name" disabled />
      </a-form-item>
      <a-form-item label="颜色">
        <input type="color" v-model="createForm.color" style="width: 60px; height: 32px; border: 1px solid #d9d9d9; border-radius: 4px; cursor: pointer; padding: 2px" />
        <span style="margin-left: 8px; color: #999">{{ createForm.color }}</span>
      </a-form-item>
    </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useTagStore } from '@/stores/tag'
import type { Tag } from '@/types/tag'

defineOptions({ inheritAttrs: false })

const props = withDefaults(defineProps<{
  modelValue?: number[]
  placeholder?: string
}>(), {
  placeholder: '选择标签',
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: number[]): void
}>()

const store = useTagStore()
const searchText = ref('')
const createVisible = ref(false)
const creating = ref(false)
const createForm = ref<Tag>({ name: '', color: '#1890ff' })

const selectedTagIds = computed(() => props.modelValue ?? [])

const isExactMatch = computed(() => {
  if (!searchText.value) return false
  return store.tags.some(t => t.name === searchText.value)
})

function filterOption(input: string, option: any) {
  const tag = store.tags.find(t => t.id === option.value)
  return tag ? tag.name.toLowerCase().includes(input.toLowerCase()) : false
}

function handleSearch(val: string) {
  searchText.value = val
}

function handleChange(values: (number | string)[]) {
  const filtered = values.filter(v => typeof v === 'number').map(v => v as number)
  emit('update:modelValue', filtered)
  searchText.value = ''
}

function showCreateModal() {
  createForm.value = { name: searchText.value, color: '#1890ff' }
  createVisible.value = true
}

async function handleCreate() {
  creating.value = true
  try {
    const newTag = await store.addTag(createForm.value)
    const current = props.modelValue ?? []
    emit('update:modelValue', [...current, newTag.id!])
    createVisible.value = false
    searchText.value = ''
  } finally {
    creating.value = false
  }
}

onMounted(() => {
  store.fetchTags()
})
</script>
