<template>
  <div style="padding: 24px">
    <a-card :bordered="false" :loading="loading">
      <template #title>
        <span>客户360视图</span>
        <a-tag v-if="data?.status" :color="statusColorMap[data.status]">{{ statusMap[data.status] || data.status }}</a-tag>
        <a-tag v-if="data?.ccSyncStatus" :color="syncColorMap[data.ccSyncStatus]">{{ syncLabel[data.ccSyncStatus] || data.ccSyncStatus }}</a-tag>
      </template>
      <template #extra>
        <a-button @click="$router.back()">返回</a-button>
      </template>

      <template v-if="data">
        <a-alert
          v-if="data.degraded"
          message="主数据服务暂时不可用，部分信息可能不是最新"
          type="warning"
          show-icon
          style="margin-bottom: 16px"
        />

        <a-descriptions :column="2" bordered size="small" style="margin-bottom: 16px">
          <a-descriptions-item label="客户名称">{{ data.name }}</a-descriptions-item>
          <a-descriptions-item label="联系人">{{ data.contactPerson || '-' }}</a-descriptions-item>
          <a-descriptions-item label="电话">{{ data.phone || '-' }}</a-descriptions-item>
          <a-descriptions-item label="邮箱">{{ data.email || '-' }}</a-descriptions-item>
          <a-descriptions-item label="证件类型">{{ idTypeMap[data.idType ?? ''] || data.idType || '-' }}</a-descriptions-item>
          <a-descriptions-item label="证件号码">{{ data.idNumber || '-' }}</a-descriptions-item>
          <a-descriptions-item label="公司">{{ data.company || '-' }}</a-descriptions-item>
          <a-descriptions-item label="行业">{{ data.industry || '-' }}</a-descriptions-item>
          <a-descriptions-item label="来源">{{ sourceMap[data.source ?? ''] || '-' }}</a-descriptions-item>
          <a-descriptions-item label="等级">{{ levelMap[data.level ?? ''] || '-' }}</a-descriptions-item>
          <a-descriptions-item label="标签" :span="2">
            <div style="display: flex; align-items: center; gap: 4px; flex-wrap: wrap">
              <a-tag v-for="tag in (data.tags || [])" :key="tag.id" :color="tag.color">{{ tag.name }}</a-tag>
              <span v-if="!data.tags || data.tags.length === 0" style="color: #999">-</span>
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="最后跟进">{{ data.lastFollowUp || '-' }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ data.createTime || '-' }}</a-descriptions-item>
          <a-descriptions-item label="备注" :span="2">{{ data.remark || '-' }}</a-descriptions-item>
        </a-descriptions>

        <a-tabs v-model:activeKey="activeTab">
          <a-tab-pane key="identity" tab="身份画像">
            <a-descriptions v-if="data" :column="2" bordered size="small" title="主数据信息">
              <a-descriptions-item label="性别">{{ data.gender || '-' }}</a-descriptions-item>
              <a-descriptions-item label="生日">{{ data.birthday || '-' }}</a-descriptions-item>
              <a-descriptions-item label="职业">{{ data.occupation || '-' }}</a-descriptions-item>
              <a-descriptions-item label="账户状态">{{ data.accountStatus || '-' }}</a-descriptions-item>
              <a-descriptions-item label="会员等级">{{ data.memberLevel || '-' }}</a-descriptions-item>
              <a-descriptions-item label="认证等级">{{ data.authLevel || '-' }}</a-descriptions-item>
              <a-descriptions-item label="风险偏好">{{ data.riskProfile || '-' }}</a-descriptions-item>
              <a-descriptions-item label="收入范围">{{ data.incomeRange || '-' }}</a-descriptions-item>
              <a-descriptions-item label="AUM">{{ data.aum != null ? data.aum.toLocaleString() : '-' }}</a-descriptions-item>
              <a-descriptions-item label="可用余额">{{ data.availableBalance != null ? data.availableBalance.toLocaleString() : '-' }}</a-descriptions-item>
              <a-descriptions-item label="总收益">{{ data.totalReturn != null ? data.totalReturn.toLocaleString() : '-' }}</a-descriptions-item>
            </a-descriptions>

            <a-table
              v-if="data.holdingProducts && data.holdingProducts.length > 0"
              :dataSource="data.holdingProducts"
              :columns="holdingColumns"
              :pagination="false"
              size="small"
              style="margin-top: 16px"
              :title="() => '持仓产品'"
              rowKey="productCode"
            />
            <a-empty v-else-if="!data.degraded" description="暂无持仓数据" style="margin-top: 16px" />
          </a-tab-pane>

          <a-tab-pane key="notes" tab="跟进记录">
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

            <a-timeline v-if="data.notes && data.notes.length > 0">
              <a-timeline-item v-for="note in data.notes" :key="note.id" :color="categoryColorMap[note.category]">
                <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px">
                  <component :is="categoryIcon[note.category]" v-if="categoryIcon[note.category]" style="font-size: 16px" />
                  <a-tag :color="categoryColorMap[note.category]">{{ categoryLabel[note.category] || note.category }}</a-tag>
                  <span style="color: #999; font-size: 12px">{{ note.createTime }}</span>
                </div>
                <div>{{ note.content }}</div>
              </a-timeline-item>
            </a-timeline>
            <a-empty v-else description="暂无跟进记录" />
          </a-tab-pane>

          <a-tab-pane key="opportunities" tab="商机">
            <a-table
              :dataSource="opportunities"
              :columns="oppColumns"
              :loading="oppLoading"
              :pagination="false"
              size="small"
              rowKey="id"
            />
            <a-empty v-if="!oppLoading && opportunities.length === 0" description="暂无商机" />
          </a-tab-pane>

          <a-tab-pane key="orders" tab="订单">
            <a-table
              :dataSource="orders"
              :columns="orderColumns"
              :loading="orderLoading"
              :pagination="false"
              size="small"
              rowKey="id"
            />
            <a-empty v-if="!orderLoading && orders.length === 0" description="暂无订单" />
          </a-tab-pane>
        </a-tabs>
      </template>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useCustomerNoteStore } from '@/stores/customerNote'
import { getCustomer360 } from '@/api/customer'
import { getOpportunities } from '@/api/opportunity'
import { getOrders } from '@/api/order'
import type { Customer360, CustomerNote, NoteCategory } from '@/types/common'
import type { Opportunity } from '@/types/opportunity'
import type { Order } from '@/types/order'
import { PhoneOutlined, HomeOutlined, MailOutlined, WechatOutlined, MoreOutlined } from '@ant-design/icons-vue'

const route = useRoute()
const customerId = Number(route.params.id)

const loading = ref(false)
const data = ref<Customer360 | null>(null)
const activeTab = ref('identity')

const noteStore = useCustomerNoteStore()
const submitting = ref(false)
const noteForm = ref<{ category: NoteCategory | undefined; content: string }>({
  category: undefined,
  content: '',
})

const oppLoading = ref(false)
const opportunities = ref<Opportunity[]>([])
const orderLoading = ref(false)
const orders = ref<Order[]>([])

const sourceMap: Record<string, string> = { REFERRAL: '推荐', WEBSITE: '网站', AD: '广告', COLD_CALL: '电销', OTHER: '其他' }
const levelMap: Record<string, string> = { VIP: 'VIP', IMPORTANT: '重要', NORMAL: '普通', POTENTIAL: '潜在' }
const statusMap: Record<string, string> = { ACTIVE: '活跃', INACTIVE: '停用', LOST: '流失' }
const statusColorMap: Record<string, string> = { ACTIVE: 'green', INACTIVE: 'orange', LOST: 'red' }
const syncLabel: Record<string, string> = { PENDING: '待同步', SYNCED: '已同步', FAILED: '同步失败' }
const syncColorMap: Record<string, string> = { PENDING: 'orange', SYNCED: 'green', FAILED: 'red' }
const idTypeMap: Record<string, string> = { ID_CARD: '身份证', PASSPORT: '护照', DRIVER_LICENSE: '驾驶证', OTHER: '其他' }

const categoryLabel: Record<string, string> = { PHONE_CALL: '电话沟通', VISIT: '上门拜访', EMAIL: '邮件沟通', WECHAT: '微信沟通', OTHER: '其他' }
const categoryColorMap: Record<string, string> = { PHONE_CALL: 'blue', VISIT: 'green', EMAIL: 'orange', WECHAT: 'cyan', OTHER: 'default' }
const categoryIcon: Record<string, any> = { PHONE_CALL: PhoneOutlined, VISIT: HomeOutlined, EMAIL: MailOutlined, WECHAT: WechatOutlined, OTHER: MoreOutlined }

const holdingColumns = [
  { title: '产品类型', dataIndex: 'productType', key: 'productType' },
  { title: '产品代码', dataIndex: 'productCode', key: 'productCode' },
  { title: '产品名称', dataIndex: 'productName', key: 'productName' },
  { title: '金额', dataIndex: 'amount', key: 'amount', customRender: ({ text }: { text: number }) => text?.toLocaleString() ?? '-' },
]

const oppColumns = [
  { title: '标题', dataIndex: 'title', key: 'title' },
  { title: '阶段', dataIndex: 'stage', key: 'stage' },
  { title: '金额', dataIndex: 'amount', key: 'amount', customRender: ({ text }: { text: number }) => text?.toLocaleString() ?? '-' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
]

const orderColumns = [
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '金额', dataIndex: 'totalAmount', key: 'totalAmount', customRender: ({ text }: { text: number }) => text?.toLocaleString() ?? '-' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
]

async function fetch360() {
  loading.value = true
  try {
    const res = await getCustomer360(customerId)
    data.value = res.data
  } finally {
    loading.value = false
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
    await fetch360()
  } finally {
    submitting.value = false
  }
}

async function fetchOpportunities() {
  oppLoading.value = true
  try {
    const res = await getOpportunities({ customerId, page: 0, size: 100 })
    opportunities.value = res.data.content
  } finally {
    oppLoading.value = false
  }
}

async function fetchOrders() {
  orderLoading.value = true
  try {
    const res = await getOrders({ customerId, page: 0, size: 100 })
    orders.value = res.data.content
  } finally {
    orderLoading.value = false
  }
}

watch(activeTab, (tab) => {
  if (tab === 'opportunities' && opportunities.value.length === 0) {
    fetchOpportunities()
  }
  if (tab === 'orders' && orders.value.length === 0) {
    fetchOrders()
  }
})

onMounted(() => {
  fetch360()
})
</script>
