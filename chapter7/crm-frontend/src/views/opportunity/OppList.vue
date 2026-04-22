<template>
  <div style="padding: 24px">
    <a-card title="机会列表" :bordered="false">
      <div style="margin-bottom: 16px; display: flex; gap: 12px; align-items: center; flex-wrap: wrap">
        <a-input v-model:value="searchTitle" placeholder="机会标题" style="width: 200px" allow-clear @pressEnter="handleSearch" />
        <a-select v-model:value="searchStage" placeholder="阶段" style="width: 120px" allow-clear>
          <a-select-option value="PROSPECTING">寻访</a-select-option>
          <a-select-option value="QUALIFYING">资格审查</a-select-option>
          <a-select-option value="PROPOSAL">方案</a-select-option>
          <a-select-option value="NEGOTIATION">谈判</a-select-option>
          <a-select-option value="WON">赢单</a-select-option>
          <a-select-option value="LOST">丢单</a-select-option>
        </a-select>
        <a-button type="primary" @click="handleSearch">查询</a-button>
        <a-button @click="handleReset">重置</a-button>
        <a-button type="primary" @click="showForm()">新增机会</a-button>
      </div>

      <a-table
        :columns="columns"
        :data-source="store.opportunities"
        :loading="store.loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'stage'">
            <a-tag :color="stageColorMap[record.stage]">{{ stageMap[record.stage] || record.stage }}</a-tag>
          </template>
          <template v-if="column.key === 'amount'">
            {{ record.amount ? '¥' + Number(record.amount).toLocaleString() : '-' }}
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" @click="showForm(record)">编辑</a-button>
            <a-button type="link" @click="showDetail(record)">详情</a-button>
            <a-popconfirm title="确定删除该机会？" @confirm="handleDelete(record.id)">
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>

    <OppForm
      v-model:open="formVisible"
      :opportunity="formOpp"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useOpportunityStore } from '@/stores/opportunity'
import OppForm from './OppForm.vue'
import type { Opportunity, OppStage } from '@/types/opportunity'

const store = useOpportunityStore()
const router = useRouter()

const searchTitle = ref<string | undefined>()
const searchStage = ref<OppStage | undefined>()

const formVisible = ref(false)
const formOpp = ref<Opportunity | undefined>()

const stageMap: Record<string, string> = { PROSPECTING: '寻访', QUALIFYING: '资格审查', PROPOSAL: '方案', NEGOTIATION: '谈判', WON: '赢单', LOST: '丢单' }
const stageColorMap: Record<string, string> = { PROSPECTING: 'blue', QUALIFYING: 'cyan', PROPOSAL: 'orange', NEGOTIATION: 'purple', WON: 'green', LOST: 'red' }

const columns = [
  { title: '机会标题', dataIndex: 'title', key: 'title', width: 180 },
  { title: '预计金额', dataIndex: 'amount', key: 'amount', width: 120 },
  { title: '阶段', dataIndex: 'stage', key: 'stage', width: 100 },
  { title: '赢单概率', dataIndex: 'probability', key: 'probability', width: 80 },
  { title: '负责人', dataIndex: 'ownerName', key: 'ownerName', width: 100 },
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
  store.updateQuery({ title: searchTitle.value, stage: searchStage.value, page: 0 })
  store.fetchList()
}

function handleReset() {
  searchTitle.value = undefined
  searchStage.value = undefined
  store.resetQuery()
  store.fetchList()
}

function handleTableChange(pag: any) {
  store.updateQuery({ page: pag.current - 1, size: pag.pageSize })
  store.fetchList()
}

function showForm(opp?: Opportunity) {
  formOpp.value = opp ? { ...opp } : undefined
  formVisible.value = true
}

function showDetail(opp: Opportunity) {
  router.push(`/opportunities/${opp.id}`)
}

async function handleSubmit(data: Opportunity) {
  if (data.id) {
    await store.editOpportunity(data.id, data)
  } else {
    await store.addOpportunity(data)
  }
  formVisible.value = false
}

async function handleDelete(id: number) {
  await store.removeOpportunity(id)
}

onMounted(() => {
  store.fetchList()
})
</script>
