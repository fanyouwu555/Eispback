import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useTagsViewStore = defineStore('tagsView', () => {
  const visitedViews = ref([])

  function addView(view) {
    if (visitedViews.value.some(v => v.path === view.path)) return
    visitedViews.value.push({ ...view })
  }

  function delView(view) {
    const index = visitedViews.value.findIndex(v => v.path === view.path)
    if (index > -1) visitedViews.value.splice(index, 1)
  }

  function delOthersViews(view) {
    visitedViews.value = visitedViews.value.filter(v => {
      return v.meta?.affix || v.path === view.path
    })
  }

  function delAllViews() {
    visitedViews.value = visitedViews.value.filter(v => v.meta?.affix)
  }

  function updateVisitedView(view) {
    const index = visitedViews.value.findIndex(v => v.path === view.path)
    if (index > -1) {
      visitedViews.value[index] = { ...visitedViews.value[index], ...view }
    }
  }

  return {
    visitedViews,
    addView,
    delView,
    delOthersViews,
    delAllViews,
    updateVisitedView
  }
})
