export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    hidden: true
  }
]

// 非菜单管理的补充路由（不由后端控制）
export const supplementRoutes = [
  {
    path: '/profile',
    component: () => import('@/views/login/index.vue'),
    name: 'Profile',
    meta: { title: '个人中心' },
    hidden: true
  }
]