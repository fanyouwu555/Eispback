<template>
  <div class="app-container">
    <el-alert title="系统监控（预留）" type="info" :closable="false" show-icon>
      <template #default>
        <p>文档要求：全方位监控微服务、接口、数据库、系统资源</p>
        <p>微服务状态：用户权限、资产计费、消息、项目、模板、AI对话等模块运行状态</p>
        <p>服务器资源：CPU使用率、内存占用、磁盘使用率、网络IO</p>
        <p>接口性能：全站QPS、平均响应时间、接口错误率、慢接口排行</p>
        <p>数据库监控：连接数、慢查询、死锁、表空间</p>
        <p>此功能需要集成 Spring Boot Actuator / Prometheus / Grafana。</p>
      </template>
    </el-alert>
  </div>
</template>
<script setup></script>