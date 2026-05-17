import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../views/Dashboard.vue')
  },
  {
    path: '/contracts',
    component: () => import('../views/ContractList.vue')
  },
  {
    path: '/payments',
    component: () => import('../views/PaymentList.vue')
  },
  {
    path: '/reminders',
    component: () => import('../views/ReminderList.vue')
  },
  {
    path: '/renewals',
    component: () => import('../views/RenewalList.vue')
  },
  {
    path: '/suppliers',
    component: () => import('../views/SupplierList.vue')
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
