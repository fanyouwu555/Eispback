import { createRouter, createWebHashHistory } from 'vue-router'
import { constantRoutes, asyncRoutes } from './routes'
import { useUserStore } from '@/stores/user'
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
          await userStore.getInfo()
          asyncRoutes.forEach(route => {
            if (!router.hasRoute(route.name)) {
              router.addRoute(route)
            }
          })
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
