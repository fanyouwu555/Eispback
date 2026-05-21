import { createRouter, createWebHashHistory } from 'vue-router'
import { constantRoutes, supplementRoutes } from './routes'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu'
import { getToken } from '@/utils/auth'

const router = createRouter({
  history: createWebHashHistory(),
  routes: constantRoutes
})

let isRoutesAdded = false

router.beforeEach(async (to, from, next) => {
  const token = getToken()

  if (token) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      if (!isRoutesAdded) {
        try {
          const userStore = useUserStore()
          const menuStore = useMenuStore()

          // 1. 获取用户信息
          await userStore.getInfo()

          // 2. 获取后端菜单树并生成动态路由
          await menuStore.fetchRoutes()

          // 3. 注册动态路由
          for (const route of menuStore.dynamicRoutes) {
            router.addRoute(route)
          }

          // 4. 注册补充路由
          for (const route of supplementRoutes) {
            router.addRoute(route)
          }

          isRoutesAdded = true
          next({ ...to, replace: true })
        } catch {
          const userStore = useUserStore()
          await userStore.logout()
          next(`/login?redirect=${to.path}`)
        }
      } else {
        next()
      }
    }
  } else {
    if (to.path === '/login') {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router