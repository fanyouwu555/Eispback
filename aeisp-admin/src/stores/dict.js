import { ref } from 'vue'
import { defineStore } from 'pinia'
import { listDictData, refreshDictCache } from '@/api/system/dict'

export const useDictStore = defineStore('dict', () => {
  const dictMap = ref(new Map()) // dictCode -> DictDataVO[]

  async function loadDict(dictCode, force = false) {
    if (!force && dictMap.value.has(dictCode)) {
      return dictMap.value.get(dictCode)
    }
    const res = await listDictData(dictCode)
    const data = res || []
    dictMap.value.set(dictCode, data)
    return data
  }

  function getDict(dictCode) {
    return dictMap.value.get(dictCode) || []
  }

  function getDictLabel(dictCode, value) {
    const list = getDict(dictCode)
    const item = list.find(i => String(i.itemValue) === String(value))
    return item ? item.itemLabel : value
  }

  function getDictColor(dictCode, value) {
    const list = getDict(dictCode)
    const item = list.find(i => String(i.itemValue) === String(value))
    return item ? item.color : ''
  }

  async function refreshAll() {
    await refreshDictCache()
    dictMap.value.clear()
  }

  return {
    dictMap,
    loadDict,
    getDict,
    getDictLabel,
    getDictColor,
    refreshAll
  }
})
