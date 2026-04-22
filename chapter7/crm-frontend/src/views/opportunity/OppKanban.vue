<template>
  <div style="padding: 24px">
    <a-card title="机会看板" :bordered="false">
      <template #extra>
        <a-button type="primary" @click="showForm()">新增机会</a-button>
      </template>
      <div v-if="store.loading" style="text-align: center; padding: 40px">
        <a-spin size="large" />
      </div>
      <div v-else class="kanban-board">
        <div v-for="stage in stages" :key="stage.value" class="kanban-column">
          <div class="kanban-column-header" :style="{ borderTopColor: stage.color }">
            <span>{{ stage.label }}</span>
            <a-badge :count="getStageList(stage.value).length" :number-style="{ backgroundColor: stage.color }" />
          </div>
          <div class="kanban-column-body">
            <VueDraggable
              :model-value="getStageList(stage.value)"
              group="opportunity"
              :animation="150"
              @update:model-value="(list: any[]) => onDragEnd(stage.value, list)"
            >
              <div v-for="opp in getStageList(stage.value)" :key="opp.id" class="kanban-card" @click="showDetail(opp)">
                <div class="kanban-card-title">{{ opp.title }}</div>
                <div class="kanban-card-info">
                  <span v-if="opp.amount">¥{{ opp.amount.toLocaleString() }}</span>
                  <span v-if="opp.ownerName">{{ opp.ownerName }}</span>
                </div>
                <div class="kanban-card-footer">
                  <a-tag v-if="opp.probability" :color="opp.probability >= 70 ? 'green' : opp.probability >= 40 ? 'orange' : 'red'">
                    {{ opp.probability }}%
                  </a-tag>
                </div>
              </div>
            </VueDraggable>
          </div>
        </div>
      </div>
    </a-card>

    <OppForm
      v-model:open="formVisible"
      :opportunity="formOpp"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useOpportunityStore } from '@/stores/opportunity'
import { updateOppStage } from '@/api/opportunity'
import { VueDraggable } from 'vue-draggable-plus'
import OppForm from './OppForm.vue'
import type { Opportunity, OppStage } from '@/types/opportunity'
import { message } from 'ant-design-vue'

const store = useOpportunityStore()
const router = useRouter()

const formVisible = ref(false)
const formOpp = ref<Opportunity | undefined>()

const stages = [
  { value: 'PROSPECTING' as OppStage, label: '寻访', color: '#1890ff' },
  { value: 'QUALIFYING' as OppStage, label: '资格审查', color: '#13c2c2' },
  { value: 'PROPOSAL' as OppStage, label: '方案', color: '#faad14' },
  { value: 'NEGOTIATION' as OppStage, label: '谈判', color: '#722ed1' },
  { value: 'WON' as OppStage, label: '赢单', color: '#52c41a' },
  { value: 'LOST' as OppStage, label: '丢单', color: '#ff4d4f' },
]

function getStageList(stage: OppStage): Opportunity[] {
  return store.kanbanData?.[stage] ?? []
}

async function onDragEnd(targetStage: OppStage, list: Opportunity[]) {
  const oldData = store.kanbanData
  if (!oldData) return
  const movedItems = list.filter(item => {
    const oldList = oldData[item.stage! as OppStage] ?? []
    return !oldList.find(o => o.id === item.id)
  })
  for (const item of movedItems) {
    try {
      await updateOppStage(item.id!, targetStage)
    } catch {
      message.error('更新阶段失败')
    }
  }
  store.fetchKanban()
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
  store.fetchKanban()
}

onMounted(() => {
  store.fetchKanban()
})
</script>

<style scoped>
.kanban-board {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 8px;
}
.kanban-column {
  min-width: 260px;
  flex: 1;
  background: #fafafa;
  border-radius: 6px;
  border-top: 3px solid #d9d9d9;
  display: flex;
  flex-direction: column;
}
.kanban-column-header {
  padding: 12px 16px;
  font-weight: 600;
  font-size: 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 3px solid inherit;
}
.kanban-column-body {
  padding: 8px;
  flex: 1;
  min-height: 200px;
}
.kanban-card {
  background: #fff;
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 8px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.06);
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.kanban-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
}
.kanban-card-title {
  font-weight: 500;
  margin-bottom: 6px;
}
.kanban-card-info {
  font-size: 12px;
  color: #8c8c8c;
  display: flex;
  justify-content: space-between;
}
.kanban-card-footer {
  margin-top: 6px;
}
</style>
