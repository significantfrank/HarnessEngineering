<template>
  <a-modal
    :open="open"
    :title="order?.id ? '编辑订单' : '新增订单'"
    @cancel="handleCancel"
    @ok="handleOk"
    :confirm-loading="submitting"
    width="600px"
  >
    <a-form :model="formState" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
      <a-form-item label="客户ID" name="customerId" :rules="[{ required: true, message: '请输入客户ID' }]">
        <a-input-number v-model:value="formState.customerId" style="width: 100%" :disabled="!!order?.id" />
      </a-form-item>
      <a-form-item label="机会ID" name="opportunityId" :rules="[{ required: true, message: '请输入机会ID' }]">
        <a-input-number v-model:value="formState.opportunityId" style="width: 100%" :disabled="!!order?.id" />
      </a-form-item>
      <a-form-item label="订单金额" name="totalAmount" :rules="[{ required: true, message: '请输入订单金额' }]">
        <a-input-number v-model:value="formState.totalAmount" style="width: 100%" :min="0" :precision="2" />
      </a-form-item>
      <a-form-item label="状态" name="status">
        <a-select v-model:value="formState.status" placeholder="请选择" allow-clear>
          <a-select-option value="PENDING">待确认</a-select-option>
          <a-select-option value="CONFIRMED">已确认</a-select-option>
          <a-select-option value="PROCESSING">处理中</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="负责人" name="ownerName">
        <a-input v-model:value="formState.ownerName" />
      </a-form-item>
      <a-form-item label="备注" name="remark">
        <a-textarea v-model:value="formState.remark" :rows="3" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { Order } from '@/types/order'

const props = defineProps<{
  open: boolean
  order?: Order
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', data: Order): void
}>()

const submitting = ref(false)

const formState = ref<Order>({
  customerId: undefined as unknown as number,
  opportunityId: undefined as unknown as number,
  totalAmount: undefined,
  status: 'PENDING',
  ownerName: '',
  remark: '',
})

watch(() => props.open, (val) => {
  if (val && props.order) {
    formState.value = { ...props.order }
  } else if (val) {
    formState.value = {
      customerId: undefined as unknown as number,
      opportunityId: undefined as unknown as number,
      totalAmount: undefined,
      status: 'PENDING',
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
