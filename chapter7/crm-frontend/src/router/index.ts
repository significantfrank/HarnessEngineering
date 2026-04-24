import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/leads',
    },
    {
      path: '/leads',
      name: 'LeadList',
      component: () => import('@/views/lead/LeadList.vue'),
    },
    {
      path: '/opportunities',
      name: 'OppKanban',
      component: () => import('@/views/opportunity/OppKanban.vue'),
    },
    {
      path: '/opportunities/list',
      name: 'OppList',
      component: () => import('@/views/opportunity/OppList.vue'),
    },
    {
      path: '/opportunities/:id',
      name: 'OppDetail',
      component: () => import('@/views/opportunity/OppDetail.vue'),
    },
    {
      path: '/orders',
      name: 'OrderList',
      component: () => import('@/views/order/OrderList.vue'),
    },
    {
      path: '/orders/:id',
      name: 'OrderDetail',
      component: () => import('@/views/order/OrderDetail.vue'),
    },
    {
      path: '/customers',
      name: 'CustomerList',
      component: () => import('@/views/customer/CustomerList.vue'),
    },
    {
      path: '/customers/:id',
      name: 'Customer360',
      component: () => import('@/views/customer/Customer360.vue'),
    },
    {
      path: '/tags',
      name: 'TagList',
      component: () => import('@/views/tag/TagList.vue'),
    },
  ],
})

export default router
