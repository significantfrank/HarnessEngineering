<template>
  <div style="padding: 24px">
    <a-card :bordered="false" :loading="customerLoading">
      <template #title>
        <span>客户详情</span>
        <a-tag v-if="customer?.status" :color="statusColorMap[customer.status]">{{ statusMap[customer.status] || customer.status }}</a-tag>
      </template>
      <template #extra>
        <a-button @click="$router.back()">返回</a-button>
      </template>

      <template v-if="customer">
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="客户名称">{{ customer.name }}</a-descriptions-item>
          <a-descriptions-item label="联系人">{{ customer.contactPerson || '-' }}</a-descriptions-item>
          <a-descriptions-item label="电话">{{ customer.phone || '-' }}</a-descriptions-item>
          <a-descriptions-item label="邮箱">{{ customer.email || '-' }}</a-descriptions-item>
          <a-descriptions-item label="公司">{{ customer.company || '-' }}</a-descriptions-item>
          <a-descriptions-item label="行业">{{ customer.industry || '-' }}</a-descriptions-item>
          <a-descriptions-item label="来源">{{ sourceMap[customer.source ?? ''] || '-' }}</a-descriptions-item>
          <a-descriptions-item label="等级">{{ levelMap[customer.level ?? ''] || '-' }}</a-descriptions-item>
          <a-descriptions-item label="标签" :span="2">
            <div style="display: flex; align-items: center; gap: 4px; flex-wrap: wrap">
              <a-tag v-for="tag in (customer.tags || [])" :key="tag.id" :color="tag.color">{{ tag.name }}</a-tag>
              <span v-if="!customer.tags || customer.tags.length === 0" style="color: #999">-</span>
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="地址" :span="2">{{ customer.address || '-' }}</a-descriptions-item>
          <a-descriptions-item label="最后跟进">{{ customer.lastFollowUp || '-' }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ customer.createTime || '-' }}</a-descriptions-item>
          <a-descriptions-item label="备注" :span="2">{{ customer.remark || '-' }}</a-descriptions-item>
        </a-descriptions>
      </template>
    </a-card>

    <a-card title="跟进小记" :bordered="false" style="margin-top: 16px">
      <div style="margin-bottom: 16px; display: flex; gap: 12px; align-items: flex-start">
        <a-select v-model:value="noteForm.category" placeholder="跟进类型" style="width: 140px">
          <a-select-option value="PHONE_CALL">电话沟通</a-select-option>
          <a-select-option value="VISIT">上门拜访</a-select-option>
          <a-select-option value="EMAIL">邮件沟通</a-select-option>
          <a-select-option value="WECHAT">微信沟通</a-select-option>
          <a-select-option value="OTHER">其他</a-select-option>
        </a-select>
        <a-textarea v-model:value="noteForm.content" placeholder="输入跟进内容..." :rows="2" style="flex: 1" />
        <a-button type="primary" :loading="submitting" @click="handleAddNote">提交</a-button>
      </div>

      <a-timeline v-if="noteStore.notes.length > 0">
        <a-timeline-item v-for="note in noteStore.notes" :key="note.id" :color="categoryColorMap[note.category]">
          <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px">
            <component :is="categoryIcon[note.category]" v-if="categoryIcon[note.category]" style="font-size: 16px" />
            <a-tag :color="categoryColorMap[note.category]">{{ categoryLabel[note.category] || note.category }}</a-tag>
            <span style="color: #999; font-size: 12px">{{ note.createTime }}</span>
          </div>
          <div>{{ note.content }}</div>
        </a-timeline-item>
      </a-timeline>
      <a-empty v-else description="暂无跟进记录" />

      <div v-if="noteStore.total > 10" style="margin-top: 16px; text-align: center">
        <a-pagination
          :current="notePage + 1"
          :total="noteStore.total"
          :page-size="10"
          @change="(p: number) => { notePage = p - 1; noteStore.fetchNotes(customerId, notePage) }"
        />
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useCustomerNoteStore } from '@/stores/customerNote'
import { getCustomer } from '@/api/customer'
import type { Customer, NoteCategory } from '@/types/common'
import { PhoneOutlined, HomeOutlined, MailOutlined, WechatOutlined, MoreOutlined } from '@ant-design/icons-vue'

const route = useRoute()
const customerId = Number(route.params.id)

const customerLoading = ref(false)
const customer = ref<Customer | null>(null)

const noteStore = useCustomerNoteStore()
const notePage = ref(0)
const submitting = ref(false)
const noteForm = ref<{ category: NoteCategory | undefined; content: string }>({
  category: undefined,
  content: '',
})

const sourceMap: Record<string, string> = { REFERRAL: '推荐', WEBSITE: '网站', AD: '广告', COLD_CALL: '电销', OTHER: '其他' }
const levelMap: Record<string, string> = { VIP: 'VIP', IMPORTANT: '重要', NORMAL: '普通', POTENTIAL: '潜在' }
const statusMap: Record<string, string> = { ACTIVE: '活跃', INACTIVE: '停用', LOST: '流失' }
const statusColorMap: Record<string, string> = { ACTIVE: 'green', INACTIVE: 'orange', LOST: 'red' }

const categoryLabel: Record<string, string> = { PHONE_CALL: '电话沟通', VISIT: '上门拜访', EMAIL: '邮件沟通', WECHAT: '微信沟通', OTHER: '其他' }
const categoryColorMap: Record<string, string> = { PHONE_CALL: 'blue', VISIT: 'green', EMAIL: 'orange', WECHAT: 'cyan', OTHER: 'default' }
const categoryIcon: Record<string, any> = { PHONE_CALL: PhoneOutlined, VISIT: HomeOutlined, EMAIL: MailOutlined, WECHAT: WechatOutlined, OTHER: MoreOutlined }

async function fetchCustomer() {
  customerLoading.value = true
  try {
    const res = await getCustomer(customerId)
    customer.value = res.data
  } finally {
    customerLoading.value = false
  }
}

async function handleAddNote() {
  if (!noteForm.value.category || !noteForm.value.content.trim()) return
  submitting.value = true
  try {
    await noteStore.addNote(customerId, {
      category: noteForm.value.category,
      content: noteForm.value.content,
    })
    noteForm.value = { category: undefined, content: '' }
    await fetchCustomer()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchCustomer()
  noteStore.fetchNotes(customerId)
})
</script>
