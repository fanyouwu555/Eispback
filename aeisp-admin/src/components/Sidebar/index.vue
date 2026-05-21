<template>
  <div class="sidebar-container">
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="!appStore.sidebar.opened"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        mode="vertical"
      >
        <SidebarItem
          v-for="route in routes"
          :key="route.path"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useMenuStore } from '@/stores/menu'
import SidebarItem from './SidebarItem.vue'

const route = useRoute()
const appStore = useAppStore()
const menuStore = useMenuStore()

const routes = computed(() => menuStore.dynamicRoutes)

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) return meta.activeMenu
  return path
})
</script>

<style scoped>
.sidebar-container {
  width: 210px;
  height: 100vh;
  background-color: #304156;
  transition: width 0.28s;
}
.el-menu {
  border-right: none;
}
</style>
