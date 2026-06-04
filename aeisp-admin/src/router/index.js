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
/** 记录动态添加的路由名称，用于登出时清理 */
const addedRouteNames = []

/** 重置路由状态并移除已注册的动态路由（退出登录时调用） */
export function resetRoutes() {
  isRoutesAdded = false
  for (const name of addedRouteNames) {
    try { router.removeRoute(name) } catch { /* 路由可能已不存在 */ }
  }
  addedRouteNames.length = 0
}

router.beforeEach(async (to, from, next) => {
  const token = getToken()

  if (!token) {
    // 无 token 时重置路由状态，确保下次登录重新加载
    resetRoutes()
    if (to.path === '/login') {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
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
          if (route.name) addedRouteNames.push(route.name)
        }

        // 4. 注册补充路由
        for (const route of supplementRoutes) {
          router.addRoute(route)
          if (route.name) addedRouteNames.push(route.name)
        }

        isRoutesAdded = true

        // 若目标路径为 / 且无 dashboard 权限，重定向到第一个可用菜单
        if (to.path === '/') {
          const firstRoute = menuStore.dynamicRoutes[0]
          if (firstRoute) {
            const redirectPath = firstRoute.redirect || firstRoute.path
            next({ path: redirectPath, replace: true })
            return
          }
        }

        next({ ...to, replace: true })
      } catch {
        const userStore = useUserStore()
        await userStore.logout()
        next(`/login?redirect=${to.path}`)
      }
    } else {
      // 已注册路由后，/ 路径重定向到第一个可用菜单
      if (to.path === '/') {
        const menuStore = useMenuStore()
        const firstRoute = menuStore.dynamicRoutes[0]
        if (firstRoute) {
          const redirectPath = firstRoute.redirect || firstRoute.path
          next({ path: redirectPath, replace: true })
          return
        }
      }
      next()
    }
  }
})

export default router
