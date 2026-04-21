<template>
  <div style="padding: 24px">
    <a-card title="客户管理" :bordered="false">
      <div style="margin-bottom: 16px; display: flex; gap: 12px; align-items: center; flex-wrap: wrap">
        <a-input v-model:value="searchName" placeholder="客户名称" style="width: 200px" allow-clear @pressEnter="handleSearch" />
        <a-select v-model:value="searchStatus" placeholder="状态" style="width: 120px" allow-clear>
          <a-select-option value="ACTIVE">活跃</a-select-option>
          <a-select-option value="INACTIVE">停用</a-select-option>
          <a-select-option value="LOST">流失</a-select-option>
        </a-select>
        <a-select v-model:value="searchSource" placeholder="来源" style="width: 120px" allow-clear>
          <a-select-option value="REFERRAL">推荐</a-select-option>
          <a-select-option value="WEBSITE">网站</a-select-option>
          <a-select-option value="AD">广告</a-select-option>
          <a-select-option value="COLD_CALL">电销</a-select-option>
          <a-select-option value="OTHER">其他</a-select-option>
        </a-select>
        <a-select v-model:value="searchLevel" placeholder="等级" style="width: 120px" allow-clear>
          <a-select-option value="VIP">VIP</a-select-option>
          <a-select-option value="IMPORTANT">重要</a-select-option>
          <a-select-option value="NORMAL">普通</a-select-option>
          <a-select-option value="POTENTIAL">潜在</a-select-option>
        </a-select>
        <a-button type="primary" @click="handleSearch">查询</a-button>
        <a-button @click="handleReset">重置</a-button>
        <a-button type="primary" @click="showForm()">新增客户</a-button>
      </div>

      <a-table
        :columns="columns"
        :data-source="store.customers"
        :loading="store.loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'source'">
            {{ sourceMap[record.source] || record.source }}
          </template>
          <template v-if="column.key === 'level'">
            {{ levelMap[record.level] || record.level }}
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColorMap[record.status]">{{ statusMap[record.status] || record.status }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" @click="showForm(record)">编辑</a-button>
            <a-popconfirm title="确定删除该客户？" @confirm="handleDelete(record.id)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>

    <CustomerForm
      v-model:open="formVisible"
      :customer="formCustomer"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useCustomerStore } from '@/stores/customer'
import CustomerForm from './CustomerForm.vue'
import type { Customer, CustomerSource, CustomerLevel, CustomerStatus } from '@/types/customer'

const store = useCustomerStore()

const searchName = ref<string | undefined>()
const searchStatus = ref<CustomerStatus | undefined>()
const searchSource = ref<CustomerSource | undefined>()
const searchLevel = ref<CustomerLevel | undefined>()

const formVisible = ref(false)
const formCustomer = ref<Customer | undefined>()

const sourceMap: Record<string, string> = { REFERRAL: '推荐', WEBSITE: '网站', AD: '广告', COLD_CALL: '电销', OTHER: '其他' }
const levelMap: Record<string, string> = { VIP: 'VIP', IMPORTANT: '重要', NORMAL: '普通', POTENTIAL: '潜在' }
const statusMap: Record<string, string> = { ACTIVE: '活跃', INACTIVE: '停用', LOST: '流失' }
const statusColorMap: Record<string, string> = { ACTIVE: 'green', INACTIVE: 'orange', LOST: 'red' }

const columns = [
  { title: '客户名称', dataIndex: 'name', key: 'name', width: 150 },
  { title: '联系人', dataIndex: 'contactPerson', key: 'contactPerson', width: 100 },
  { title: '电话', dataIndex: 'phone', key: 'phone', width: 130 },
  { title: '公司', dataIndex: 'company', key: 'company', width: 150 },
  { title: '等级', dataIndex: 'level', key: 'level', width: 80 },
  { title: '来源', dataIndex: 'source', key: 'source', width: 80 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const },
]

const pagination = computed(() => ({
  current: (store.query.page ?? 0) + 1,
  pageSize: store.query.size ?? 10,
  total: store.total,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
}))

function handleSearch() {
  store.updateQuery({
    name: searchName.value,
    status: searchStatus.value,
    source: searchSource.value,
    level: searchLevel.value,
    page: 0,
  })
  store.fetchList()
}

function handleReset() {
  searchName.value = undefined
  searchStatus.value = undefined
  searchSource.value = undefined
  searchLevel.value = undefined
  store.resetQuery()
  store.fetchList()
}

function handleTableChange(pag: any) {
  store.updateQuery({
    page: pag.current - 1,
    size: pag.pageSize,
  })
  store.fetchList()
}

function showForm(customer?: Customer) {
  formCustomer.value = customer ? { ...customer } : undefined
  formVisible.value = true
}

async function handleSubmit(data: Customer) {
  if (data.id) {
    await store.editCustomer(data.id, data)
  } else {
    await store.addCustomer(data)
  }
  formVisible.value = false
}

async function handleDelete(id: number) {
  await store.removeCustomer(id)
}

onMounted(() => {
  store.fetchList()
})
</script>
