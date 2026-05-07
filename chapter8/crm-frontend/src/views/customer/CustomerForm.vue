<template>
  <a-modal
    :open="open"
    :title="customer?.id ? '编辑客户' : '新增客户'"
    @cancel="handleCancel"
    @ok="handleOk"
    :confirm-loading="submitting"
    width="700px"
  >
    <a-form :model="formState" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="客户名称" name="name" :rules="[{ required: true, message: '请输入客户名称' }]">
            <a-input v-model:value="formState.name" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="联系人" name="contactPerson">
            <a-input v-model:value="formState.contactPerson" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="电话" name="phone">
            <a-input v-model:value="formState.phone" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="邮箱" name="email">
            <a-input v-model:value="formState.email" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="证件类型" name="idType">
            <a-select v-model:value="formState.idType" placeholder="请选择证件类型" allow-clear>
              <a-select-option value="ID_CARD">身份证</a-select-option>
              <a-select-option value="PASSPORT">护照</a-select-option>
              <a-select-option value="DRIVER_LICENSE">驾驶证</a-select-option>
              <a-select-option value="OTHER">其他</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="证件号码" name="idNumber">
            <a-input v-model:value="formState.idNumber" :disabled="!!customer?.id" placeholder="证件号码创建后不可修改" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="公司" name="company">
            <a-input v-model:value="formState.company" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="行业" name="industry">
            <a-input v-model:value="formState.industry" />
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
          <a-form-item label="等级" name="level">
            <a-select v-model:value="formState.level" placeholder="请选择" allow-clear>
              <a-select-option value="VIP">VIP</a-select-option>
              <a-select-option value="IMPORTANT">重要</a-select-option>
              <a-select-option value="NORMAL">普通</a-select-option>
              <a-select-option value="POTENTIAL">潜在</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="状态" name="status">
            <a-select v-model:value="formState.status" placeholder="请选择" allow-clear>
              <a-select-option value="ACTIVE">活跃</a-select-option>
              <a-select-option value="INACTIVE">停用</a-select-option>
              <a-select-option value="LOST">流失</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="网站" name="website">
            <a-input v-model:value="formState.website" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="标签" name="tagIds">
            <TagSelect v-model="formState.tagIds!" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="最后跟进" name="lastFollowUp">
            <a-date-picker v-model:value="formState.lastFollowUp" style="width: 100%" show-time />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
      </a-row>
      <a-form-item label="地址" name="address" :label-col="{ span: 3 }" :wrapper-col="{ span: 20 }">
        <a-input v-model:value="formState.address" />
      </a-form-item>
      <a-form-item label="备注" name="remark" :label-col="{ span: 3 }" :wrapper-col="{ span: 20 }">
        <a-textarea v-model:value="formState.remark" :rows="3" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { Customer } from '@/types/customer'
import TagSelect from '@/components/TagSelect.vue'

const props = defineProps<{
  open: boolean
  customer?: Customer
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', data: Customer): void
}>()

const submitting = ref(false)

const formState = ref<Customer>({
  name: '',
  phone: '',
  email: '',
  company: '',
  address: '',
  source: undefined,
  level: undefined,
  industry: '',
  website: '',
  contactPerson: '',
  lastFollowUp: undefined,
  status: 'ACTIVE',
  remark: '',
  idType: undefined,
  idNumber: '',
  tagIds: [],
})

watch([() => props.open, () => props.customer], ([val, customer]) => {
  if (val && customer) {
    formState.value = { ...customer, tagIds: customer.tagIds ?? customer.tags?.map(t => t.id!).filter(Boolean) ?? [] }
  } else if (val) {
    formState.value = {
      name: '',
      phone: '',
      email: '',
      company: '',
      address: '',
      source: undefined,
      level: undefined,
      industry: '',
      website: '',
      contactPerson: '',
      lastFollowUp: undefined,
      status: 'ACTIVE',
      remark: '',
      idType: undefined,
      idNumber: '',
      tagIds: [],
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
