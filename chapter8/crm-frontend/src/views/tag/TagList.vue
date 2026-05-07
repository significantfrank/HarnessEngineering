<template>
  <div style="padding: 24px">
    <a-card title="标签管理" :bordered="false">
      <div style="margin-bottom: 16px; display: flex; justify-content: flex-end">
        <a-button type="primary" @click="showForm()">新建标签</a-button>
      </div>

      <a-table
        :columns="columns"
        :data-source="store.tags"
        :loading="store.loading"
        row-key="id"
        :pagination="false"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'color'">
            <a-tag :color="record.color">{{ record.color }}</a-tag>
          </template>
          <template v-if="column.key === 'customerCount'">
            {{ record.customerCount ?? 0 }}
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" @click="showForm(record)">编辑</a-button>
            <a-popconfirm
              :title="`确定删除标签「${record.name}」？`"
              @confirm="handleDelete(record)"
            >
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      :open="formVisible"
      :title="editingTag?.id ? '编辑标签' : '新建标签'"
      @cancel="formVisible = false"
      @ok="handleOk"
      :confirm-loading="submitting"
      width="400px"
    >
      <a-form :model="formState" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="标签名称" name="name" :rules="[{ required: true, message: '请输入标签名称' }]">
          <a-input v-model:value="formState.name" />
        </a-form-item>
        <a-form-item label="颜色" name="color" :rules="[{ required: true, message: '请选择颜色' }]">
          <input type="color" v-model="formState.color" style="width: 60px; height: 32px; border: 1px solid #d9d9d9; border-radius: 4px; cursor: pointer; padding: 2px" />
          <span style="margin-left: 8px; color: #999">{{ formState.color }}</span>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useTagStore } from '@/stores/tag'
import type { Tag } from '@/types/tag'

const store = useTagStore()

const columns = [
  { title: '标签名称', dataIndex: 'name', key: 'name', width: 200 },
  { title: '颜色', dataIndex: 'color', key: 'color', width: 120 },
  { title: '使用客户数', dataIndex: 'customerCount', key: 'customerCount', width: 120 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 140 },
]

const formVisible = ref(false)
const editingTag = ref<Tag | undefined>()
const submitting = ref(false)
const formState = ref<Tag>({ name: '', color: '#1890ff' })

function showForm(tag?: Tag) {
  editingTag.value = tag
  if (tag) {
    formState.value = { ...tag }
  } else {
    formState.value = { name: '', color: '#1890ff' }
  }
  formVisible.value = true
}

async function handleOk() {
  submitting.value = true
  try {
    if (editingTag.value?.id) {
      await store.editTag(editingTag.value.id, formState.value)
      message.success('更新成功')
    } else {
      await store.addTag(formState.value)
      message.success('创建成功')
    }
    formVisible.value = false
  } finally {
    submitting.value = false
  }
}

async function handleDelete(tag: Tag) {
  try {
    await store.removeTag(tag.id!)
  } catch {
    // error handled by interceptor
  }
}

onMounted(() => {
  store.fetchTags()
})
</script>
