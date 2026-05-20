export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    hidden: true
  }
]

export const asyncRoutes = [
  // ===== 1. 仪表盘 =====
  {
    path: '/',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/dashboard',
    name: 'Home',
    meta: { title: '仪表盘', icon: 'DataAnalysis', alwaysShow: true },
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        name: 'Dashboard',
        meta: { title: '数据大盘', icon: 'DataAnalysis' }
      },
      {
        path: 'profile',
        component: () => import('@/views/login/index.vue'),
        name: 'Profile',
        meta: { title: '个人中心' },
        hidden: true
      }
    ]
  },
  // ===== 2. 系统管理 =====
  {
    path: '/system',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/system/menu',
    name: 'System',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'menu',
        component: () => import('@/views/system/menu/index.vue'),
        name: 'SysMenu',
        meta: { title: '菜单管理', permissions: ['system:menu:list'] }
      },
      {
        path: 'role',
        component: () => import('@/views/system/role/index.vue'),
        name: 'SysRole',
        meta: { title: '角色管理', permissions: ['system:role:list'] }
      },
      {
        path: 'user',
        component: () => import('@/views/system/user/index.vue'),
        name: 'SysAdmin',
        meta: { title: '管理员管理', permissions: ['system:user:list'] }
      },
      {
        path: 'log',
        component: () => import('@/views/system/log/index.vue'),
        name: 'SysLog',
        meta: { title: '操作日志', permissions: ['system:log:list'] }
      },
      {
        path: 'login-log',
        component: () => import('@/views/system/login-log/index.vue'),
        name: 'SysLoginLog',
        meta: { title: '登录日志', permissions: ['admin:log:read'] }
      },
      {
        path: 'dict',
        component: () => import('@/views/system/dict/index.vue'),
        name: 'SysDict',
        meta: { title: '数据字典', permissions: ['system:dict:list'] }
      }
    ]
  },
  // ===== 3. 用户与权限 =====
  {
    path: '/user',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/user/list',
    name: 'User',
    meta: { title: '用户与权限', icon: 'UserFilled' },
    children: [
      {
        path: 'list',
        component: () => import('@/views/user/list/index.vue'),
        name: 'UserList',
        meta: { title: '用户管理', permissions: ['user:list'] }
      },
      {
        path: 'permission',
        component: () => import('@/views/user/permission/index.vue'),
        name: 'UserPermission',
        meta: { title: '权限分配', permissions: ['user:permission:assign'] }
      }
    ]
  },
  // ===== 4. 资产与计费 =====
  {
    path: '/finance',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/finance/balance',
    name: 'Finance',
    meta: { title: '资产与计费', icon: 'Coin' },
    children: [
      {
        path: 'balance',
        component: () => import('@/views/recharge/balance/index.vue'),
        name: 'FinanceBalance',
        meta: { title: '用户余额' }
      },
      {
        path: 'order',
        component: () => import('@/views/recharge/order/index.vue'),
        name: 'FinanceOrder',
        meta: { title: '充值记录', permissions: ['order:manage'] }
      },
      {
        path: 'deduction',
        component: () => import('@/views/recharge/deduction/index.vue'),
        name: 'FinanceDeduction',
        meta: { title: '扣费记录' }
      },
      {
        path: 'purchase',
        component: () => import('@/views/recharge/purchase/index.vue'),
        name: 'FinancePurchase',
        meta: { title: '模型购买' }
      },
      {
        path: 'package',
        component: () => import('@/views/recharge/package/index.vue'),
        name: 'FinancePackage',
        meta: { title: '套餐管理', permissions: ['finance:package:manage'] }
      }
    ]
  },
  // ===== 5. 消息管理 =====
  {
    path: '/message',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/message/notification',
    name: 'Message',
    meta: { title: '消息管理', icon: 'Bell', alwaysShow: true },
    children: [
      {
        path: 'notification',
        component: () => import('@/views/message/notification/index.vue'),
        name: 'SysNotification',
        meta: { title: '系统通告', permissions: ['notification:read'] }
      }
    ]
  },
  // ===== 6. 项目管理 =====
  {
    path: '/project',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/project/list',
    name: 'Project',
    meta: { title: '项目管理', icon: 'Management', alwaysShow: true },
    children: [
      {
        path: 'list',
        component: () => import('@/views/project/index.vue'),
        name: 'ProjectList',
        meta: { title: '项目列表', permissions: ['project:list'] }
      }
    ]
  },
  // ===== 7. 模板资源 =====
  {
    path: '/template',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/template/list',
    name: 'Template',
    meta: { title: '模板资源', icon: 'Files' },
    children: [
      {
        path: 'category',
        component: () => import('@/views/template/category/index.vue'),
        name: 'TemplateCategory',
        meta: { title: '模板分类' }
      },
      {
        path: 'list',
        component: () => import('@/views/template/list/index.vue'),
        name: 'TemplateList',
        meta: { title: '模板管理', permissions: ['template:read'] }
      }
    ]
  },
  // ===== 8-9 (文档红色标记，跳过) =====
  // ===== 10. AI对话模块 =====
  {
    path: '/ai',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/ai/session',
    name: 'AiDialog',
    meta: { title: 'AI对话模块', icon: 'Cpu' },
    children: [
      {
        path: 'session',
        component: () => import('@/views/ai/session/index.vue'),
        name: 'AiSession',
        meta: { title: '会话管理', permissions: ['ai:session:read'] }
      },
      {
        path: 'config',
        component: () => import('@/views/model/index.vue'),
        name: 'AiConfig',
        meta: { title: 'AI配置', permissions: ['model:read'] }
      },
      {
        path: 'stats',
        component: () => import('@/views/model/stats/index.vue'),
        name: 'AiStats',
        meta: { title: 'AI统计' }
      }
    ]
  },
  // ===== 11. 系统配置模块 =====
  {
    path: '/sysconfig',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/sysconfig/base',
    name: 'SysConfig',
    meta: { title: '系统配置模块', icon: 'Operation' },
    children: [
      {
        path: 'base',
        component: () => import('@/views/system/config/index.vue'),
        name: 'SysConfigBase',
        meta: { title: '基础配置', permissions: ['system:config:list'] }
      },
      {
        path: 'feature',
        component: () => import('@/views/system/feature/index.vue'),
        name: 'SysConfigFeature',
        meta: { title: '功能开关' }
      }
    ]
  },
  // ===== 12. 运营统计模块 =====
  {
    path: '/operations',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/operations/dashboard',
    name: 'Operations',
    meta: { title: '运营统计模块', icon: 'DataBoard' },
    children: [
      {
        path: 'dashboard',
        component: () => import('@/views/operations/dashboard/index.vue'),
        name: 'OpsDashboard',
        meta: { title: '数据大盘' }
      },
      {
        path: 'monitor',
        component: () => import('@/views/operations/monitor/index.vue'),
        name: 'OpsMonitor',
        meta: { title: '系统监控' }
      }
    ]
  }
]