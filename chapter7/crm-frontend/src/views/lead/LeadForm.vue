<template>
  <a-modal
    :open="open"
    :title="lead?.id ? '编辑线索' : '新增线索'"
    @cancel="handleCancel"
    @ok="handleOk"
    :confirm-loading="submitting"
    width="700px"
  >
    <a-form :model="formState" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="线索名称" name="name" :rules="[{ required: true, message: '请输入线索名称' }]">
            <a-input v-model:value="formState.name" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="电话" name="phone">
            <a-input v-model:value="formState.phone" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="邮箱" name="email">
            <a-input v-model:value="formState.email" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="公司" name="company">
            <a-input v-model:value="formState.company" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="来源" name="source">
            <a-select v-model:value="formState.source" placeholder="请选择" allow-clear>
              <a-select-option value="REFERRAL">推荐</a-select-option>
              <a-select-option value="WEBSITE">网站</a-select-option>
              <a-select-option value="AD">广告</a-select-option>
              <a-select-option value="COLD_CALL">电销</a-select-option>
              <a-select-option value="OTHER">其他</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="状态" name="status">
            <a-select v-model:value="formState.status" placeholder="请选择" allow-clear>
              <a-select-option value="NEW">新线索</a-select-option>
              <a-select-option value="CONTACTED">已联系</a-select-option>
              <a-select-option value="QUALIFIED">已合格</a-select-option>
              <a-select-option value="UNQUALIFIED">不合格</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="负责人" name="ownerName">
            <a-input v-model:value="formState.ownerName" />
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
import type { Lead } from '@/types/lead'

const props = defineProps<{
  open: boolean
  lead?: Lead
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', data: Lead): void
}>()

const submitting = ref(false)

const formState = ref<Lead>({
  name: '',
  phone: '',
  email: '',
  company: '',
  source: undefined,
  status: 'NEW',
  ownerName: '',
  remark: '',
})

watch(() => props.open, (val) => {
  if (val && props.lead) {
    formState.value = { ...props.lead }
  } else if (val) {
    formState.value = {
      name: '',
      phone: '',
      email: '',
      company: '',
      source: undefined,
      status: 'NEW',
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
