export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    hidden: true
  }
]

export const asyncRoutes = [
  {
    path: '/',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/system/user',
    name: 'Home',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/login/index.vue'),
        name: 'Profile',
        meta: { title: '个人中心' },
        hidden: true
      }
    ]
  },
  {
    path: '/system',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/system/user',
    name: 'System',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'user',
        component: () => import('@/views/system/user/index.vue'),
        name: 'SysUser',
        meta: { title: '用户管理', permissions: ['system:user:list'] }
      },
      {
        path: 'role',
        component: () => import('@/views/system/role/index.vue'),
        name: 'SysRole',
        meta: { title: '角色管理', permissions: ['system:role:list'] }
      },
      {
        path: 'permission',
        component: () => import('@/views/system/permission/index.vue'),
        name: 'SysPermission',
        meta: { title: '权限管理', permissions: ['system:permission:list'] }
      },
      {
        path: 'log',
        component: () => import('@/views/system/log/index.vue'),
        name: 'SysLog',
        meta: { title: '操作日志', permissions: ['system:log:list'] }
      },
      {
        path: 'config',
        component: () => import('@/views/system/config/index.vue'),
        name: 'SysConfig',
        meta: { title: '系统配置', permissions: ['system:config:list'] }
      }
    ]
  },
  {
    path: '/user',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/user/list',
    name: 'User',
    meta: { title: '用户模块', icon: 'UserFilled' },
    children: [
      {
        path: 'list',
        component: () => import('@/views/user/list/index.vue'),
        name: 'UserList',
        meta: { title: '用户列表', permissions: ['user:list'] }
      },
      {
        path: 'login-log',
        component: () => import('@/views/user/login-log/index.vue'),
        name: 'LoginLog',
        meta: { title: '登录日志', permissions: ['user:loginLog:list'] }
      },
      {
        path: 'duration-log',
        component: () => import('@/views/user/duration-log/index.vue'),
        name: 'DurationLog',
        meta: { title: '时长日志', permissions: ['user:durationLog:list'] }
      },
      {
        path: 'statistics',
        component: () => import('@/views/user/statistics/index.vue'),
        name: 'UserStatistics',
        meta: { title: '数据统计', permissions: ['user:statistics'] }
      }
    ]
  }
]
