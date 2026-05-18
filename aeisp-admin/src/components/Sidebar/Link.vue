<template>
  <component :is="type" v-bind="linkProps">
    <slot />
  </component>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ to: { type: String, required: true } })

const isExternal = computed(() => /^(https?:|mailto:|tel:)/.test(props.to))
const type = computed(() => (isExternal.value ? 'a' : 'router-link'))

const linkProps = computed(() => {
  if (isExternal.value) {
    return { href: props.to, target: '_blank', rel: 'noopener' }
  }
  return { to: props.to }
})
</script>
