<template>
  <a-modal
    :open="open"
    title="转化线索"
    @cancel="handleCancel"
    @ok="handleOk"
    :confirm-loading="submitting"
    width="500px"
  >
    <a-alert message="转化将创建客户和机会，此操作不可逆" type="warning" show-icon style="margin-bottom: 16px" />
    <a-form :model="formState" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
      <a-form-item label="机会标题" name="opportunityTitle" :rules="[{ required: true, message: '请输入机会标题' }]">
        <a-input v-model:value="formState.opportunityTitle" />
      </a-form-item>
      <a-form-item label="预计金额" name="amount">
        <a-input-number v-model:value="formState.amount" style="width: 100%" :min="0" :precision="2" />
      </a-form-item>
      <a-form-item label="预计结单日期" name="expectedCloseDate">
        <a-date-picker v-model:value="formState.expectedCloseDate" style="width: 100%" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { Lead } from '@/types/lead'

const props = defineProps<{
  open: boolean
  lead?: Lead
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', data: any): void
}>()

const submitting = ref(false)

const formState = ref({
  opportunityTitle: '',
  amount: undefined as number | undefined,
  expectedCloseDate: undefined as string | undefined,
})

watch(() => props.open, (val) => {
  if (val && props.lead) {
    formState.value = {
      opportunityTitle: props.lead.company ? `${props.lead.company}-商机` : `${props.lead.name}-商机`,
      amount: undefined,
      expectedCloseDate: undefined,
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
