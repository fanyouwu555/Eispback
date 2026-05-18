<template>
  <el-scrollbar ref="scrollContainer" class="scroll-container">
    <slot />
  </el-scrollbar>
</template>

<script setup>
import { ref } from 'vue'
const scrollContainer = ref(null)
</script>

<style scoped>
.scroll-container {
  white-space: nowrap;
  width: 100%;
}
</style>
