<template>
  <div class="dashboard-container">
    <!-- ===== 1. 用户类指标 ===== -->
    <div class="section-label"><el-icon><UserFilled /></el-icon> 用户数据</div>
    <el-row :gutter="16" class="kpi-row">
      <el-col :span="6" v-for="card in userCards" :key="card.label">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-inner">
            <div class="kpi-icon" :style="{ background: card.bg, color: card.color }">
              <el-icon :size="22"><component :is="card.icon" /></el-icon>
            </div>
            <div class="kpi-body">
              <div class="kpi-label">{{ card.label }}</div>
              <div class="kpi-value">{{ card.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== 2. 资产类指标 ===== -->
    <div class="section-label"><el-icon><Coin /></el-icon> 资产数据</div>
    <el-row :gutter="16" class="kpi-row">
      <el-col :span="6" v-for="card in assetCards" :key="card.label">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-inner">
            <div class="kpi-icon" :style="{ background: card.bg, color: card.color }">
              <el-icon :size="22"><component :is="card.icon" /></el-icon>
            </div>
            <div class="kpi-body">
              <div class="kpi-label">{{ card.label }}</div>
              <div class="kpi-value">{{ card.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== 3. 项目类指标 ===== -->
    <div class="section-label"><el-icon><Management /></el-icon> 项目数据</div>
    <el-row :gutter="16" class="kpi-row">
      <el-col :span="6" v-for="card in projectCards" :key="card.label">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-inner">
            <div class="kpi-icon" :style="{ background: card.bg, color: card.color }">
              <el-icon :size="22"><component :is="card.icon" /></el-icon>
            </div>
            <div class="kpi-body">
              <div class="kpi-label">{{ card.label }}</div>
              <div class="kpi-value">{{ card.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== 4. 模板类指标 ===== -->
    <div class="section-label"><el-icon><Files /></el-icon> 模板数据</div>
    <el-row :gutter="16" class="kpi-row">
      <el-col :span="6" v-for="card in templateCards" :key="card.label">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-inner">
            <div class="kpi-icon" :style="{ background: card.bg, color: card.color }">
              <el-icon :size="22"><component :is="card.icon" /></el-icon>
            </div>
            <div class="kpi-body">
              <div class="kpi-label">{{ card.label }}</div>
              <div class="kpi-value">{{ card.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== 5. AI 类指标 ===== -->
    <div class="section-label"><el-icon><Cpu /></el-icon> AI 数据</div>
    <el-row :gutter="16" class="kpi-row">
      <el-col :span="8" v-for="card in aiCards" :key="card.label">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-inner">
            <div class="kpi-icon" :style="{ background: card.bg, color: card.color }">
              <el-icon :size="22"><component :is="card.icon" /></el-icon>
            </div>
            <div class="kpi-body">
              <div class="kpi-label">{{ card.label }}</div>
              <div class="kpi-value">{{ card.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== 6. 趋势图 ===== -->
    <div class="section-label"><el-icon><TrendCharts /></el-icon> 业务趋势</div>
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12" v-for="chart in charts" :key="chart.key">
        <el-card shadow="hover">
          <template #header>
            <div class="chart-header">
              <span>{{ chart.title }}</span>
              <el-radio-group
                :model-value="chart.days"
                size="small"
                @update:model-value="val => switchChartDays(chart, val)"
              >
                <el-radio-button :value="7">7日</el-radio-button>
                <el-radio-button :value="30">30日</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div :ref="el => setChartRef(chart, el)" style="height: 280px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== 7. 告警 + 快捷入口 ===== -->
    <el-row :gutter="16" class="bottom-row">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header><span>业务告警</span></template>
          <el-empty description="暂无告警" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span>快捷入口</span></template>
          <div class="quick-links">
            <el-button v-for="link in quickLinks" :key="link.path" class="quick-btn" @click="goto(link.path)">
              <el-icon><component :is="link.icon" /></el-icon>
              {{ link.label }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getDashboardSummary, getDashboardTrends } from '@/api/dashboard'

const router = useRouter()
const summary = ref({})

// --- Chart configs ---
const charts = reactive([
  { key: 'user', title: '用户增长趋势', days: 30 },
  { key: 'recharge', title: '充值消费趋势', days: 30 },
  { key: 'project', title: '项目创建趋势', days: 30 },
  { key: 'ai', title: 'AI对话趋势', days: 30 },
])
const chartRefs = {}
const chartInstances = {}

function setChartRef(chart, el) {
  if (el) {
    chartRefs[chart.key] = el
  }
}

const COLORS = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']

// --- Computed KPI Cards ---
const userCards = computed(() => {
  const u = summary.value.user || {}
  return [
    { label: '总注册用户数', value: u.totalUsers ?? '-', icon: 'UserFilled', color: COLORS[0], bg: COLORS[0] + '18' },
    { label: '日新增用户', value: u.newUsersToday ?? '-', icon: 'Plus', color: COLORS[1], bg: COLORS[1] + '18' },
    { label: '月活跃用户', value: u.mau ?? '-', icon: 'TrendCharts', color: COLORS[2], bg: COLORS[2] + '18' },
    { label: '总活跃用户', value: u.totalActiveUsers ?? '-', icon: 'Calendar', color: COLORS[3], bg: COLORS[3] + '18' }
  ]
})

const assetCards = computed(() => {
  const a = summary.value.asset || {}
  const fmt = v => v != null ? '¥' + (v / 100).toFixed(2) : '-'
  return [
    { label: '总充值余额', value: fmt(a.totalRechargeBalance), icon: 'Coin', color: COLORS[2], bg: COLORS[2] + '18' },
    { label: '今日充值', value: fmt(a.dailyRechargeAmount), icon: 'TopUp', color: COLORS[1], bg: COLORS[1] + '18' },
    { label: '总消费', value: fmt(a.totalConsumedAmount), icon: 'Sell', color: COLORS[3], bg: COLORS[3] + '18' },
    { label: '平均余额', value: fmt(a.avgBalance), icon: 'DataLine', color: COLORS[0], bg: COLORS[0] + '18' }
  ]
})

const projectCards = computed(() => {
  const p = summary.value.project || {}
  return [
    { label: '全部项目', value: p.totalProjects ?? '-', icon: 'FolderOpened', color: COLORS[0], bg: COLORS[0] + '18' },
    { label: '今日新增', value: p.dailyNewProjects ?? '-', icon: 'FolderAdd', color: COLORS[1], bg: COLORS[1] + '18' },
    { label: '本月新增', value: p.monthlyNewProjects ?? '-', icon: 'Calendar', color: COLORS[2], bg: COLORS[2] + '18' },
    { label: '标杆项目', value: p.benchmarkProjects ?? '-', icon: 'Star', color: COLORS[4], bg: COLORS[4] + '18' }
  ]
})

const templateCards = computed(() => {
  const t = summary.value.template || {}
  return [
    { label: '总模板', value: t.totalTemplates ?? '-', icon: 'Files', color: COLORS[0], bg: COLORS[0] + '18' },
    { label: '今日新增', value: t.todayNewTemplates ?? '-', icon: 'Plus', color: COLORS[1], bg: COLORS[1] + '18' },
    { label: '上线数量', value: t.onlineCount ?? '-', icon: 'CircleCheck', color: COLORS[2], bg: COLORS[2] + '18' },
    { label: '热门模板', value: (t.hotTemplates && t.hotTemplates.length) || '-', icon: 'TrendCharts', color: COLORS[3], bg: COLORS[3] + '18' }
  ]
})

const aiCards = computed(() => {
  const a = summary.value.ai || {}
  const fmtCalls = v => v != null ? (v >= 10000 ? (v / 10000).toFixed(1) + 'w' : v) : '-'
  return [
    { label: '累计调用', value: fmtCalls(a.totalCalls), icon: 'Cpu', color: COLORS[0], bg: COLORS[0] + '18' },
    { label: '累计会话', value: fmtCalls(a.totalSessions), icon: 'ChatDotSquare', color: COLORS[1], bg: COLORS[1] + '18' },
    { label: '调用成功率', value: a.successRate ?? '-', icon: 'CircleCheckFilled', color: COLORS[2], bg: COLORS[2] + '18' }
  ]
})

const quickLinks = [
  { label: '用户列表', path: '/user/list', icon: 'UserFilled' },
  { label: '发布通告', path: '/message/notification', icon: 'ChatDotSquare' },
  { label: '系统配置', path: '/sysconfig/base', icon: 'Setting' },
  { label: '管理员管理', path: '/system/user', icon: 'Operation' },
  { label: '操作日志', path: '/system/log', icon: 'Document' },
  { label: '数据字典', path: '/system/dict', icon: 'Reading' }
]

function goto(path) {
  router.push(path)
}

// --- Load data ---
async function loadSummary() {
  try {
    const res = await getDashboardSummary()
    summary.value = res || {}
  } catch (e) {
    console.error('Failed to load dashboard summary', e)
  }
}

async function loadTrends(chart) {
  try {
    const res = await getDashboardTrends(chart.key, chart.days)
    renderChart(chart.key, res || {})
  } catch (e) {
    console.error('Failed to load trend', chart.key, e)
  }
}

function renderChart(key, data) {
  const el = chartRefs[key]
  if (!el) return
  if (chartInstances[key]) chartInstances[key].dispose()
  const instance = echarts.init(el)
  chartInstances[key] = instance

  let option = {}
  if (key === 'user') {
    option = {
      tooltip: { trigger: 'axis' },
      legend: { data: ['新增用户', '活跃用户'], bottom: 0 },
      grid: { left: 50, right: 20, bottom: 40, top: 20 },
      xAxis: { type: 'category', data: data.dates || [], boundaryGap: false },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        { name: '新增用户', type: 'line', smooth: true, data: data.newUsers || [], areaStyle: {}, itemStyle: { color: COLORS[0] } },
        { name: '活跃用户', type: 'line', smooth: true, data: data.activeUsers || [], areaStyle: { color: COLORS[1] + '44' }, itemStyle: { color: COLORS[1] } }
      ]
    }
  } else if (key === 'recharge') {
    option = {
      tooltip: { trigger: 'axis' },
      legend: { data: ['充值金额', '消费金额'], bottom: 0 },
      grid: { left: 60, right: 20, bottom: 40, top: 20 },
      xAxis: { type: 'category', data: data.dates || [], boundaryGap: false },
      yAxis: { type: 'value' },
      series: [
        { name: '充值金额', type: 'line', smooth: true, data: data.rechargeAmounts || [], areaStyle: {}, itemStyle: { color: COLORS[1] } },
        { name: '消费金额', type: 'line', smooth: true, data: data.consumeAmounts || [], areaStyle: { color: COLORS[3] + '44' }, itemStyle: { color: COLORS[3] } }
      ]
    }
  } else if (key === 'project') {
    option = {
      tooltip: { trigger: 'axis' },
      legend: { data: ['新建项目'], bottom: 0 },
      grid: { left: 50, right: 20, bottom: 40, top: 20 },
      xAxis: { type: 'category', data: data.dates || [], boundaryGap: false },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        { name: '新建项目', type: 'bar', data: data.created || [], itemStyle: { color: COLORS[0] } }
      ]
    }
  } else if (key === 'ai') {
    option = {
      tooltip: { trigger: 'axis' },
      legend: { data: ['调用次数', 'Token消耗'], bottom: 0 },
      grid: { left: 50, right: 20, bottom: 40, top: 20 },
      xAxis: { type: 'category', data: data.dates || [], boundaryGap: false },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        { name: '调用次数', type: 'line', smooth: true, data: data.callCounts || [], areaStyle: {}, itemStyle: { color: COLORS[0] } },
        { name: 'Token消耗', type: 'line', smooth: true, data: data.tokenCounts || [], areaStyle: { color: COLORS[2] + '44' }, itemStyle: { color: COLORS[2] } }
      ]
    }
  }
  instance.setOption(option)
}

function switchChartDays(chart, days) {
  chart.days = days
  loadTrends(chart)
}

function handleResize() {
  Object.values(chartInstances).forEach(i => i?.resize())
}

onMounted(async () => {
  await loadSummary()
  for (const chart of charts) {
    await loadTrends(chart)
  }
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  Object.values(chartInstances).forEach(i => i?.dispose())
})
</script>

<style scoped>
.dashboard-container {
  padding: 16px;
}

.section-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 20px 0 12px 0;
  display: flex;
  align-items: center;
  gap: 6px;
}
.section-label:first-child {
  margin-top: 0;
}

.kpi-row {
  margin-bottom: 4px;
}

.kpi-card {
  --el-card-padding: 16px;
}

.kpi-inner {
  display: flex;
  align-items: center;
  gap: 14px;
}

.kpi-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.kpi-body {
  flex: 1;
  min-width: 0;
}

.kpi-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.kpi-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.chart-row {
  margin-bottom: 16px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 14px;
}

.bottom-row {
  margin-top: 4px;
}

.quick-links {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-btn {
  width: calc(50% - 4px);
  justify-content: flex-start;
}
</style>