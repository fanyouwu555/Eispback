import { computed, onMounted } from 'vue'
import { useDictStore } from '@/stores/dict'

export function useDict(dictCode) {
  const store = useDictStore()

  const options = computed(() => store.getDict(dictCode))

  const label = (value) => store.getDictLabel(dictCode, value)
  const color = (value) => store.getDictColor(dictCode, value)

  async function refresh(force = true) {
    return store.loadDict(dictCode, force)
  }

  onMounted(() => {
    store.loadDict(dictCode)
  })

  return { options, label, color, refresh }
}
