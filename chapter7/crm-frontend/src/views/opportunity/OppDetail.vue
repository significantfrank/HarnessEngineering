<template>
  <div style="padding: 24px">
    <a-card :bordered="false" :loading="loading">
      <template #title>
        <span>机会详情</span>
        <a-tag v-if="opp?.stage" :color="stageColorMap[opp.stage]">{{ stageMap[opp.stage] || opp.stage }}</a-tag>
      </template>
      <template #extra>
        <a-button @click="$router.back()">返回</a-button>
      </template>

      <template v-if="opp">
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="机会标题">{{ opp.title }}</a-descriptions-item>
          <a-descriptions-item label="客户ID">{{ opp.customerId }}</a-descriptions-item>
          <a-descriptions-item label="预计金额">{{ opp.amount ? '¥' + Number(opp.amount).toLocaleString() : '-' }}</a-descriptions-item>
          <a-descriptions-item label="赢单概率">{{ opp.probability ? opp.probability + '%' : '-' }}</a-descriptions-item>
          <a-descriptions-item label="预计结单日期">{{ opp.expectedCloseDate || '-' }}</a-descriptions-item>
          <a-descriptions-item label="负责人">{{ opp.ownerName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="来源线索ID">{{ opp.leadId || '-' }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ opp.createTime }}</a-descriptions-item>
          <a-descriptions-item label="备注" :span="2">{{ opp.remark || '-' }}</a-descriptions-item>
        </a-descriptions>

        <div style="margin-top: 24px; display: flex; gap: 12px">
          <a-button v-if="opp.stage !== 'WON' && opp.stage !== 'LOST'" type="primary" @click="showWinDialog">赢单</a-button>
          <a-button v-if="opp.stage !== 'WON' && opp.stage !== 'LOST'" danger @click="handleLost">丢单</a-button>
          <a-button v-if="opp.stage === 'WON'" type="primary" @click="showOrderForm()">创建订单</a-button>
        </div>

        <a-divider>关联订单</a-divider>
        <a-table
          :columns="orderColumns"
          :data-source="orders"
          :loading="ordersLoading"
          row-key="id"
          size="small"
          :pagination="false"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="orderStatusColorMap[record.status]">{{ orderStatusMap[record.status] || record.status }}</a-tag>
            </template>
            <template v-if="column.key === 'totalAmount'">
              ¥{{ Number(record.totalAmount).toLocaleString() }}
            </template>
          </template>
        </a-table>
      </template>
    </a-card>

    <a-modal v-model:open="winVisible" title="确认赢单" @ok="handleWin" :confirm-loading="winSubmitting">
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="订单金额" required>
          <a-input-number v-model:value="winAmount" style="width: 100%" :min="0" :precision="2" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="winRemark" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>

    <OrderForm
      v-model:open="orderFormVisible"
      :order="orderFormInit"
      @submit="handleCreateOrder"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOpportunity, winOpportunity, updateOppStage } from '@/api/opportunity'
import { getOrders, createOrder } from '@/api/order'
import type { Opportunity } from '@/types/opportunity'
import type { Order } from '@/types/order'
import OrderForm from '@/views/order/OrderForm.vue'
import { message } from 'ant-design-vue'

const route = useRoute()
const oppId = Number(route.params.id)

const loading = ref(false)
const opp = ref<Opportunity | null>(null)
const orders = ref<Order[]>([])
const ordersLoading = ref(false)

const winVisible = ref(false)
const winAmount = ref<number>(0)
const winRemark = ref('')
const winSubmitting = ref(false)
const orderFormVisible = ref(false)
const orderFormInit = ref<Order | undefined>()

const stageMap: Record<string, string> = { PROSPECTING: '寻访', QUALIFYING: '资格审查', PROPOSAL: '方案', NEGOTIATION: '谈判', WON: '赢单', LOST: '丢单' }
const stageColorMap: Record<string, string> = { PROSPECTING: 'blue', QUALIFYING: 'cyan', PROPOSAL: 'orange', NEGOTIATION: 'purple', WON: 'green', LOST: 'red' }
const orderStatusMap: Record<string, string> = { PENDING: '待确认', CONFIRMED: '已确认', PROCESSING: '处理中', COMPLETED: '已完成', CANCELLED: '已取消' }
const orderStatusColorMap: Record<string, string> = { PENDING: 'default', CONFIRMED: 'blue', PROCESSING: 'orange', COMPLETED: 'green', CANCELLED: 'red' }

const orderColumns = [
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', width: 160 },
  { title: '金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
]

async function fetchOpp() {
  loading.value = true
  try {
    const res = await getOpportunity(oppId)
    opp.value = res.data
  } finally {
    loading.value = false
  }
}

async function fetchOrders() {
  ordersLoading.value = true
  try {
    const res = await getOrders({ opportunityId: oppId, page: 0, size: 100 })
    orders.value = res.data.content
  } finally {
    ordersLoading.value = false
  }
}

function showWinDialog() {
  winAmount.value = opp.value?.amount ? Number(opp.value.amount) : 0
  winRemark.value = ''
  winVisible.value = true
}

async function handleWin() {
  winSubmitting.value = true
  try {
    await winOpportunity(oppId, { amount: winAmount.value, remark: winRemark.value })
    message.success('赢单成功，已创建订单')
    winVisible.value = false
    fetchOpp()
    fetchOrders()
  } finally {
    winSubmitting.value = false
  }
}

async function handleLost() {
  await updateOppStage(oppId, 'LOST')
  message.success('已标记为丢单')
  fetchOpp()
}

function showOrderForm() {
  orderFormInit.value = {
    customerId: opp.value!.customerId,
    opportunityId: opp.value!.id!,
    totalAmount: undefined,
    status: 'PENDING',
    ownerName: opp.value!.ownerName,
  }
  orderFormVisible.value = true
}

async function handleCreateOrder(data: Order) {
  await createOrder(data)
  message.success('订单创建成功')
  orderFormVisible.value = false
  fetchOrders()
}

onMounted(() => {
  fetchOpp()
  fetchOrders()
})
</script>
