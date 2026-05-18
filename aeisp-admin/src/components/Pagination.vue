<template>
  <div class="pagination-container">
    <el-pagination
      :current-page="pageNum"
      :page-size="pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
const props = defineProps({
  total: { type: Number, default: 0 },
  pageNum: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 }
})
const emit = defineEmits(['pagination'])

function handleSizeChange(val) {
  emit('pagination', { page: props.pageNum, limit: val })
}
function handleCurrentChange(val) {
  emit('pagination', { page: val, limit: props.pageSize })
}
</script>

<style scoped>
.pagination-container {
  padding: 20px 0;
  display: flex;
  justify-content: flex-end;
}
</style>
