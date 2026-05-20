<template>
  <div class="app-container">
    <el-alert title="运营数据大盘" type="info" :closable="false" show-icon>
      <template #default>
        <p>文档要求：平台可视化运营数据总览</p>
        <p>顶部指标卡片：总用户数、总项目数、月活跃用户、累计营收、日活跃数</p>
        <p>可视化图表：用户增长趋势、项目创建趋势、充值/消费趋势、AI对话统计</p>
        <p>排行数据：模板使用排行、活跃用户排行、高频项目排行</p>
        <p>此功能需后端聚合多个模块数据。</p>
      </template>
    </el-alert>
  </div>
</template>
<script setup></script>