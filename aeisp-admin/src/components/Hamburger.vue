<template>
  <div class="hamburger-container" @click="toggleClick">
    <el-icon :size="20">
      <Fold v-if="isActive" />
      <Expand v-else />
    </el-icon>
  </div>
</template>

<script setup>
defineProps({ isActive: Boolean })
const emit = defineEmits(['toggle'])
function toggleClick() {
  emit('toggle')
}
</script>

<style scoped>
.hamburger-container {
  padding: 0 15px;
  cursor: pointer;
}
</style>
