<template>
  <div class="app-wrapper">
    <Sidebar class="sidebar-container" />
    <div class="main-container" :class="{ 'sidebar-hide': !appStore.sidebar.opened }">
      <Navbar />
      <AppMain />
    </div>
  </div>
</template>

<script setup>
import { useAppStore } from '@/stores/app'
import Sidebar from '@/components/Sidebar/index.vue'
import Navbar from '@/components/Navbar.vue'
import AppMain from '@/components/AppMain.vue'

const appStore = useAppStore()
</script>

<style scoped>
.app-wrapper {
  display: flex;
}
.main-container {
  flex: 1;
  margin-left: 210px;
  transition: margin-left 0.28s;
}
.main-container.sidebar-hide {
  margin-left: 64px;
}
.sidebar-container {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 1001;
}
</style>
