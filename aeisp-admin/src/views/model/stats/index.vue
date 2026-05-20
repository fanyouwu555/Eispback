<template>
  <div class="app-container">
    <el-alert title="AI统计（预留）" type="info" :closable="false" show-icon>
      <template #default>
        <p>文档要求：全平台AI对话运营数据统计</p>
        <p>核心指标：累计会话数、总Token消耗、日活跃AI用户、AI调用成功率、平均响应时间</p>
        <p>此功能需基于 model_call_log 表实现后端聚合 API。</p>
      </template>
    </el-alert>
  </div>
</template>
<script setup></script>