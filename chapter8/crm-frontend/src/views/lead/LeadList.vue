<template>
  <div style="padding: 24px">
    <a-card title="线索管理" :bordered="false">
      <div style="margin-bottom: 16px; display: flex; gap: 12px; align-items: center; flex-wrap: wrap">
        <a-input v-model:value="searchName" placeholder="线索名称" style="width: 200px" allow-clear @pressEnter="handleSearch" />
        <a-select v-model:value="searchStatus" placeholder="状态" style="width: 120px" allow-clear>
          <a-select-option value="NEW">新线索</a-select-option>
          <a-select-option value="CONTACTED">已联系</a-select-option>
          <a-select-option value="QUALIFIED">已合格</a-select-option>
          <a-select-option value="UNQUALIFIED">不合格</a-select-option>
          <a-select-option value="CONVERTED">已转化</a-select-option>
        </a-select>
        <a-select v-model:value="searchSource" placeholder="来源" style="width: 120px" allow-clear>
          <a-select-option value="REFERRAL">推荐</a-select-option>
          <a-select-option value="WEBSITE">网站</a-select-option>
          <a-select-option value="AD">广告</a-select-option>
          <a-select-option value="COLD_CALL">电销</a-select-option>
          <a-select-option value="OTHER">其他</a-select-option>
        </a-select>
        <a-button type="primary" @click="handleSearch">查询</a-button>
        <a-button @click="handleReset">重置</a-button>
        <a-button type="primary" @click="showForm()">新增线索</a-button>
      </div>

      <a-table
        :columns="columns"
        :data-source="store.leads"
        :loading="store.loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'source'">
            {{ sourceMap[record.source] || record.source }}
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColorMap[record.status]">{{ statusMap[record.status] || record.status }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" @click="showForm(record)">编辑</a-button>
            <a-button type="link" @click="showConvert(record)" :disabled="record.status === 'CONVERTED' || record.status === 'UNQUALIFIED'">转化</a-button>
            <a-popconfirm title="确定删除该线索？" @confirm="handleDelete(record.id)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>

    <LeadForm
      v-model:open="formVisible"
      :lead="formLead"
      @submit="handleSubmit"
    />

    <ConvertDialog
      v-model:open="convertVisible"
      :lead="convertLeadRef"
      @submit="handleConvert"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useLeadStore } from '@/stores/lead'
import { convertLead as convertLeadApi } from '@/api/lead'
import LeadForm from './LeadForm.vue'
import ConvertDialog from './ConvertDialog.vue'
import type { Lead, CustomerSource, LeadStatus } from '@/types/lead'
import { message } from 'ant-design-vue'

const store = useLeadStore()

const searchName = ref<string | undefined>()
const searchStatus = ref<LeadStatus | undefined>()
const searchSource = ref<CustomerSource | undefined>()

const formVisible = ref(false)
const formLead = ref<Lead | undefined>()
const convertVisible = ref(false)
const convertLeadRef = ref<Lead | undefined>()

const sourceMap: Record<string, string> = { REFERRAL: '推荐', WEBSITE: '网站', AD: '广告', COLD_CALL: '电销', OTHER: '其他' }
const statusMap: Record<string, string> = { NEW: '新线索', CONTACTED: '已联系', QUALIFIED: '已合格', UNQUALIFIED: '不合格', CONVERTED: '已转化' }
const statusColorMap: Record<string, string> = { NEW: 'blue', CONTACTED: 'cyan', QUALIFIED: 'green', UNQUALIFIED: 'red', CONVERTED: 'purple' }

const columns = [
  { title: '线索名称', dataIndex: 'name', key: 'name', width: 150 },
  { title: '电话', dataIndex: 'phone', key: 'phone', width: 130 },
  { title: '邮箱', dataIndex: 'email', key: 'email', width: 180 },
  { title: '公司', dataIndex: 'company', key: 'company', width: 150 },
  { title: '来源', dataIndex: 'source', key: 'source', width: 80 },
  { title: '负责人', dataIndex: 'ownerName', key: 'ownerName', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
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
  store.updateQuery({ name: searchName.value, status: searchStatus.value, source: searchSource.value, page: 0 })
  store.fetchList()
}

function handleReset() {
  searchName.value = undefined
  searchStatus.value = undefined
  searchSource.value = undefined
  store.resetQuery()
  store.fetchList()
}

function handleTableChange(pag: any) {
  store.updateQuery({ page: pag.current - 1, size: pag.pageSize })
  store.fetchList()
}

function showForm(lead?: Lead) {
  formLead.value = lead ? { ...lead } : undefined
  formVisible.value = true
}

function showConvert(lead: Lead) {
  convertLeadRef.value = { ...lead }
  convertVisible.value = true
}

async function handleSubmit(data: Lead) {
  if (data.id) {
    await store.editLead(data.id, data)
  } else {
    await store.addLead(data)
  }
  formVisible.value = false
}

async function handleConvert(data: any) {
  if (!convertLeadRef.value?.id) return
  await convertLeadApi(convertLeadRef.value.id, data)
  message.success('转化成功')
  convertVisible.value = false
  store.fetchList()
}

async function handleDelete(id: number) {
  await store.removeLead(id)
}

onMounted(() => {
  store.fetchList()
})
</script>
