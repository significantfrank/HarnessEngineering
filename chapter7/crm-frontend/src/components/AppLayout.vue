<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider v-model:collapsed="collapsed" collapsible :trigger="null" theme="dark" width="200">
      <div style="height: 64px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 18px; font-weight: bold">
        <span v-if="!collapsed">CRM系统</span>
        <span v-else>CRM</span>
      </div>
      <a-menu v-model:selectedKeys="selectedKeys" theme="dark" mode="inline" @click="handleMenuClick">
        <a-menu-item key="/leads">
          <template #icon><SearchOutlined /></template>
          <span>线索管理</span>
        </a-menu-item>
        <a-menu-item key="/opportunities">
          <template #icon><BulbOutlined /></template>
          <span>机会管理</span>
        </a-menu-item>
        <a-menu-item key="/orders">
          <template #icon><FileTextOutlined /></template>
          <span>订单管理</span>
        </a-menu-item>
        <a-menu-item key="/customers">
          <template #icon><UserOutlined /></template>
          <span>客户管理</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header style="background: #fff; padding: 0 16px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 4px rgba(0,0,0,0.08)">
        <MenuFoldOutlined v-if="!collapsed" style="font-size: 18px; cursor: pointer" @click="collapsed = true" />
        <MenuUnfoldOutlined v-else style="font-size: 18px; cursor: pointer" @click="collapsed = false" />
      </a-layout-header>
      <a-layout-content style="margin: 0">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { SearchOutlined, BulbOutlined, FileTextOutlined, UserOutlined, MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()

const collapsed = ref(false)
const selectedKeys = computed(() => {
  const path = route.path
  const key = ['/', '/leads', '/opportunities', '/orders', '/customers'].find(k => path.startsWith(k) && k !== '/') || path
  return [key]
})

function handleMenuClick({ key }: { key: string }) {
  router.push(key)
}
</script>
