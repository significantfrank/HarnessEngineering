<template>
  <div style="padding: 24px">
    <a-card :bordered="false" :loading="loading">
      <template #title>
        <span>订单详情</span>
        <a-tag v-if="order?.status" :color="statusColorMap[order.status]">{{ statusMap[order.status] || order.status }}</a-tag>
      </template>
      <template #extra>
        <a-button @click="$router.back()">返回</a-button>
      </template>

      <template v-if="order">
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="订单号">{{ order.orderNo }}</a-descriptions-item>
          <a-descriptions-item label="订单金额">¥{{ Number(order.totalAmount).toLocaleString() }}</a-descriptions-item>
          <a-descriptions-item label="客户ID">{{ order.customerId }}</a-descriptions-item>
          <a-descriptions-item label="机会ID">{{ order.opportunityId }}</a-descriptions-item>
          <a-descriptions-item label="负责人">{{ order.ownerName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="状态">{{ order.status ? statusMap[order.status] || order.status : '-' }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ order.createTime }}</a-descriptions-item>
          <a-descriptions-item label="更新时间">{{ order.updateTime }}</a-descriptions-item>
          <a-descriptions-item label="备注" :span="2">{{ order.remark || '-' }}</a-descriptions-item>
        </a-descriptions>
      </template>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOrder } from '@/api/order'
import type { Order } from '@/types/order'

const route = useRoute()
const orderId = Number(route.params.id)

const loading = ref(false)
const order = ref<Order | null>(null)

const statusMap: Record<string, string> = { PENDING: '待确认', CONFIRMED: '已确认', PROCESSING: '处理中', COMPLETED: '已完成', CANCELLED: '已取消' }
const statusColorMap: Record<string, string> = { PENDING: 'default', CONFIRMED: 'blue', PROCESSING: 'orange', COMPLETED: 'green', CANCELLED: 'red' }

async function fetchOrder() {
  loading.value = true
  try {
    const res = await getOrder(orderId)
    order.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOrder()
})
</script>
