<template>
  <a-modal
    :open="open"
    :title="opportunity?.id ? '编辑机会' : '新增机会'"
    @cancel="handleCancel"
    @ok="handleOk"
    :confirm-loading="submitting"
    width="700px"
  >
    <a-form :model="formState" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="机会标题" name="title" :rules="[{ required: true, message: '请输入机会标题' }]">
            <a-input v-model:value="formState.title" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="客户ID" name="customerId" :rules="[{ required: true, message: '请输入客户ID' }]">
            <a-input-number v-model:value="formState.customerId" style="width: 100%" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="预计金额" name="amount">
            <a-input-number v-model:value="formState.amount" style="width: 100%" :min="0" :precision="2" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="赢单概率" name="probability">
            <a-input-number v-model:value="formState.probability" style="width: 100%" :min="0" :max="100" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="阶段" name="stage">
            <a-select v-model:value="formState.stage" placeholder="请选择" allow-clear>
              <a-select-option value="PROSPECTING">寻访</a-select-option>
              <a-select-option value="QUALIFYING">资格审查</a-select-option>
              <a-select-option value="PROPOSAL">方案</a-select-option>
              <a-select-option value="NEGOTIATION">谈判</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="预计结单日期" name="expectedCloseDate">
            <a-date-picker v-model:value="formState.expectedCloseDate" style="width: 100%" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="负责人" name="ownerName">
            <a-input v-model:value="formState.ownerName" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="来源线索ID" name="leadId">
            <a-input-number v-model:value="formState.leadId" style="width: 100%" :disabled="!!opportunity?.id" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-form-item label="备注" name="remark" :label-col="{ span: 3 }" :wrapper-col="{ span: 20 }">
        <a-textarea v-model:value="formState.remark" :rows="3" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { Opportunity } from '@/types/opportunity'

const props = defineProps<{
  open: boolean
  opportunity?: Opportunity
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', data: Opportunity): void
}>()

const submitting = ref(false)

const formState = ref<Opportunity>({
  title: '',
  customerId: undefined as unknown as number,
  amount: undefined,
  stage: 'PROSPECTING',
  probability: undefined,
  expectedCloseDate: undefined,
  leadId: undefined,
  ownerName: '',
  remark: '',
})

watch(() => props.open, (val) => {
  if (val && props.opportunity) {
    formState.value = { ...props.opportunity }
  } else if (val) {
    formState.value = {
      title: '',
      customerId: undefined as unknown as number,
      amount: undefined,
      stage: 'PROSPECTING',
      probability: undefined,
      expectedCloseDate: undefined,
      leadId: undefined,
      ownerName: '',
      remark: '',
    }
  }
})

function handleCancel() {
  emit('update:open', false)
}

function handleOk() {
  submitting.value = true
  try {
    emit('submit', { ...formState.value })
  } finally {
    submitting.value = false
  }
}
</script>
