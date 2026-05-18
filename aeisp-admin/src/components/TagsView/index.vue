<template>
  <div class="tags-view-container">
    <ScrollPane class="tags-view-wrapper">
      <router-link
        v-for="tag in tagsViewStore.visitedViews"
        :key="tag.path"
        :to="{ path: tag.path, query: tag.query }"
        :class="['tags-view-item', isActive(tag) ? 'active' : '']"
      >
        {{ tag.title }}
        <span v-if="!tag.meta?.affix" class="el-icon-close" @click.prevent.stop="closeSelectedTag(tag)">
          <el-icon><Close /></el-icon>
        </span>
      </router-link>
    </ScrollPane>
  </div>
</template>

<script setup>
import { watch } from 'vue'
import { useRoute } from 'vue-router'
import { useTagsViewStore } from '@/stores/tagsView'
import ScrollPane from './ScrollPane.vue'

const route = useRoute()
const tagsViewStore = useTagsViewStore()

function isActive(tag) {
  return tag.path === route.path
}

function addTags() {
  const { name } = route
  if (name) {
    tagsViewStore.addView({
      name: route.name,
      title: route.meta.title || 'no-name',
      path: route.path,
      query: route.query,
      meta: { ...route.meta }
    })
  }
}

function closeSelectedTag(view) {
  tagsViewStore.delView(view)
}

watch(() => route.path, addTags, { immediate: true })
</script>

<style scoped>
.tags-view-container {
  height: 34px;
  background: #fff;
  border-bottom: 1px solid #d8dce5;
  display: flex;
  align-items: center;
  padding: 0 10px;
}
.tags-view-item {
  display: inline-flex;
  align-items: center;
  height: 26px;
  line-height: 26px;
  padding: 0 8px;
  font-size: 12px;
  border: 1px solid #d8dce5;
  background: #fff;
  color: #495060;
  text-decoration: none;
  margin-right: 5px;
}
.tags-view-item.active {
  background-color: #409EFF;
  color: #fff;
  border-color: #409EFF;
}
.el-icon-close {
  margin-left: 4px;
  cursor: pointer;
}
</style>
