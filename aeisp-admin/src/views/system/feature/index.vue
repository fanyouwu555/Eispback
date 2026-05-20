<template>
  <div class="app-container">
    <el-alert title="功能开关（预留）" type="info" :closable="false" show-icon>
      <template #default>
        <p>文档要求：平台核心业务开关管控中心</p>
        <p>业务开关：用户注册、账号登录、项目创建、仿真运行、AI对话、文件上传</p>
        <p>计费开关：充值功能、模型购买、扣费、积分支付</p>
        <p>维护模式：一键全站维护模式，前台展示维护提示</p>
        <p>此功能需先创建 sys_feature_switch 表及其后端 API。</p>
      </template>
    </el-alert>
  </div>
</template>
<script setup></script>