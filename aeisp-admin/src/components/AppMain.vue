<template>
  <div class="app-main">
    <router-view v-slot="{ Component, route }">
      <transition name="fade-transform" mode="out-in">
        <keep-alive>
          <component :is="Component" :key="route.path" />
        </keep-alive>
      </transition>
    </router-view>
  </div>
</template>

<script setup>
</script>

<style scoped>
.app-main {
  min-height: calc(100vh - 50px - 34px);
  padding: 20px;
  background: #f0f2f5;
}
</style>
