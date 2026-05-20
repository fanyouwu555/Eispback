<template>
  <div class="app-container">
    <!-- 顶部指标卡 -->
    <el-row :gutter="20" class="panel-group">
      <el-col :span="6">
        <el-card>
          <div class="card-panel-description">
            <div class="card-panel-text">总用户数</div>
            <div class="card-panel-num">{{ stats.totalUsers || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="card-panel-description">
            <div class="card-panel-text">今日新增</div>
            <div class="card-panel-num">{{ stats.newUsersToday || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="card-panel-description">
            <div class="card-panel-text">今日活跃</div>
            <div class="card-panel-num">{{ stats.dau || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="card-panel-description">
            <div class="card-panel-text">本周新增</div>
            <div class="card-panel-num">{{ stats.newUsersWeek || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card>
          <div ref="newUserChart" style="height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div ref="activeChart" style="height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getStatistics, getTrend } from '@/api/user'

const stats = ref({})
const newUserChart = ref(null)
const activeChart = ref(null)
const chartInstances = ref([])

async function initCharts() {
  const trendRes = await getTrend({ type: 'daily', days: 30 })
  const dates = trendRes?.dates || []
  const newUsers = trendRes?.newUsers || []
  const activeUsers = trendRes?.activeUsers || []

  const chart1 = echarts.init(newUserChart.value)
  chart1.setOption({
    title: { text: '新增用户趋势（近30天）', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [{ data: newUsers, type: 'line', smooth: true, areaStyle: {} }]
  })
  chartInstances.value.push(chart1)

  const chart2 = echarts.init(activeChart.value)
  chart2.setOption({
    title: { text: '活跃用户趋势（近30天）', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [{ data: activeUsers, type: 'line', smooth: true, areaStyle: { color: '#91cc75' } }]
  })
  chartInstances.value.push(chart2)
}

onMounted(async () => {
  const res = await getStatistics()
  stats.value = res || {}
  initCharts()
})

onUnmounted(() => {
  chartInstances.value.forEach(chart => chart.dispose())
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.panel-group {
  margin-bottom: 20px;
}
.card-panel-description {
  text-align: center;
  padding: 20px;
}
.card-panel-text {
  color: rgba(0, 0, 0, 0.45);
  font-size: 16px;
  margin-bottom: 12px;
}
.card-panel-num {
  font-size: 32px;
  font-weight: bold;
  color: #666;
}
.chart-row {
  margin-top: 20px;
}
</style>
