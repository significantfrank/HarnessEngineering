<template>
  <div style="padding: 24px">
    <a-card title="订单管理" :bordered="false">
      <div style="margin-bottom: 16px; display: flex; gap: 12px; align-items: center; flex-wrap: wrap">
        <a-input v-model:value="searchOrderNo" placeholder="订单号" style="width: 200px" allow-clear @pressEnter="handleSearch" />
        <a-select v-model:value="searchStatus" placeholder="状态" style="width: 120px" allow-clear>
          <a-select-option value="PENDING">待确认</a-select-option>
          <a-select-option value="CONFIRMED">已确认</a-select-option>
          <a-select-option value="PROCESSING">处理中</a-select-option>
          <a-select-option value="COMPLETED">已完成</a-select-option>
          <a-select-option value="CANCELLED">已取消</a-select-option>
        </a-select>
        <a-button type="primary" @click="handleSearch">查询</a-button>
        <a-button @click="handleReset">重置</a-button>
        <a-button type="primary" @click="showForm()">新增订单</a-button>
      </div>

      <a-table
        :columns="columns"
        :data-source="store.orders"
        :loading="store.loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'totalAmount'">
            ¥{{ Number(record.totalAmount).toLocaleString() }}
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColorMap[record.status]">{{ statusMap[record.status] || record.status }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" @click="showDetail(record)">详情</a-button>
            <a-button type="link" @click="showForm(record)">编辑</a-button>
            <a-popconfirm title="确定删除该订单？" @confirm="handleDelete(record.id)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>

    <OrderForm
      v-model:open="formVisible"
      :order="formOrder"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useOrderStore } from '@/stores/order'
import OrderForm from './OrderForm.vue'
import type { Order, OrderStatus } from '@/types/order'

const store = useOrderStore()
const router = useRouter()

const searchOrderNo = ref<string | undefined>()
const searchStatus = ref<OrderStatus | undefined>()

const formVisible = ref(false)
const formOrder = ref<Order | undefined>()

const statusMap: Record<string, string> = { PENDING: '待确认', CONFIRMED: '已确认', PROCESSING: '处理中', COMPLETED: '已完成', CANCELLED: '已取消' }
const statusColorMap: Record<string, string> = { PENDING: 'default', CONFIRMED: 'blue', PROCESSING: 'orange', COMPLETED: 'green', CANCELLED: 'red' }

const columns = [
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', width: 160 },
  { title: '金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120 },
  { title: '客户ID', dataIndex: 'customerId', key: 'customerId', width: 80 },
  { title: '机会ID', dataIndex: 'opportunityId', key: 'opportunityId', width: 80 },
  { title: '负责人', dataIndex: 'ownerName', key: 'ownerName', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' as const },
]

const pagination = computed(() => ({
  current: (store.query.page ?? 0) + 1,
  pageSize: store.query.size ?? 10,
  total: store.total,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
}))

function handleSearch() {
  store.updateQuery({ orderNo: searchOrderNo.value, status: searchStatus.value, page: 0 })
  store.fetchList()
}

function handleReset() {
  searchOrderNo.value = undefined
  searchStatus.value = undefined
  store.resetQuery()
  store.fetchList()
}

function handleTableChange(pag: any) {
  store.updateQuery({ page: pag.current - 1, size: pag.pageSize })
  store.fetchList()
}

function showForm(order?: Order) {
  formOrder.value = order ? { ...order } : undefined
  formVisible.value = true
}

function showDetail(order: Order) {
  router.push(`/orders/${order.id}`)
}

async function handleSubmit(data: Order) {
  if (data.id) {
    await store.editOrder(data.id, data)
  } else {
    await store.addOrder(data)
  }
  formVisible.value = false
}

async function handleDelete(id: number) {
  await store.removeOrder(id)
}

onMounted(() => {
  store.fetchList()
})
</script>
