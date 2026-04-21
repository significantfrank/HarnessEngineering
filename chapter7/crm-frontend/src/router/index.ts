import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/customers',
    },
    {
      path: '/customers',
      name: 'CustomerList',
      component: () => import('@/views/customer/CustomerList.vue'),
    },
  ],
})

export default router
