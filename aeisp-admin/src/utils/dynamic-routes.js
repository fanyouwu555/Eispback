import Layout from '@/views/layout/index.vue'

// Vite 编译期扫描 @/views/ 下所有 .vue 文件作为组件映射
const pageComponents = import.meta.glob('/src/views/**/*.vue')

/**
 * 将后端菜单树转换为 Vue Router 路由配置数组。
 * @param {Array} menuTree - 后端返回的菜单树
 * @returns {Array} Vue Router 路由配置
 */
export function buildRoutes(menuTree) {
  return menuTree.map(menu => convertMenu(menu)).filter(Boolean)
}

function convertMenu(menu) {
  const { id, menuType, permissionName, permissionCode, routePath, component, icon, isCache, children } = menu

  // 按钮 → 不生成路由
  if (menuType === 2) return null

  // 移除系统配置子页签
  if (permissionName === '系统配置') return null

  const route = {
    path: routePath || `/${permissionCode?.replace(/:/g, '-') || id}`,
    meta: {
      title: permissionName || '',
      icon: icon || undefined,
      permissions: permissionCode ? [permissionCode] : []
    }
  }

  if (menuType === 0) {
    // 目录 → 用 Layout 包裹，递归子菜单
    route.component = Layout
    route.name = `MenuDir_${id}`
    route.meta.alwaysShow = true
    const subItems = (children || []).map(c => convertMenu(c)).filter(Boolean)
    if (subItems.length === 0) return null
    route.children = subItems
    // 重定向到第一个子菜单，避免 Vue Router 4 处理 'noRedirect' 时产生无匹配错误
    route.redirect = subItems[0].path
  } else if (menuType === 1) {
    // 菜单 → 从 import.meta.glob 查找组件
    route.component = resolveComponent(component)
    if (!route.component) return null
    route.name = `Menu_${id}`
    if (isCache !== undefined) {
      route.meta.keepAlive = isCache === 1
    }
  } else if (menuType === 3) {
    // 外链 → 渲染空组件，由 Link.vue 处理跳转
    route.component = { render: () => null }
    route.meta.externalLink = true
  }

  return route
}

function resolveComponent(componentPath) {
  if (!componentPath) return null
  const normalizedPath = componentPath.startsWith('/')
    ? componentPath
    : '/src/views/' + componentPath

  if (pageComponents[normalizedPath]) return pageComponents[normalizedPath]

  // 容错匹配
  const key = Object.keys(pageComponents).find(k => k.endsWith(componentPath))
  return key ? pageComponents[key] : null
}