<template>
  <div class="app-container">
    <!-- 查询用户 -->
    <el-form :inline="true">
      <el-form-item label="用户ID">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable @keyup.enter="handleQuery" style="width: 160px" />
      </el-form-item>
      <el-form-item label="消耗类型" prop="consumeType">
        <el-select v-model="queryParams.consumeType" placeholder="消耗类型" clearable style="width: 140px">
          <el-option
            v-for="item in deductionTypeOptions"
            :key="item.itemValue"
            :label="item.itemLabel"
            :value="item.itemValue"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          @change="handleDateRange"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 时长统计卡片 -->
    <el-card v-if="statsVisible" class="mb-4" shadow="hover">
      <template #header>
        <span><strong>时长统计</strong> — 用户ID: {{ queryParams.userId }}</span>
      </template>
      <el-row :gutter="16">
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-label">剩余时长</div>
            <div class="stat-value text-primary">{{ statsInfo.remainingDuration || 0 }} 分钟</div>
          </div>
        </el-col>
        <el-col :span="5">
          <div class="stat-item">
            <div class="stat-label">总充值</div>
            <div class="stat-value text-success">{{ statsInfo.totalRecharged || 0 }} 分钟</div>
          </div>
        </el-col>
        <el-col :span="5">
          <div class="stat-item">
            <div class="stat-label">总消耗</div>
            <div class="stat-value text-warning">{{ statsInfo.totalConsumed || 0 }} 分钟</div>
          </div>
        </el-col>
        <el-col :span="5">
          <div class="stat-item">
            <div class="stat-label">本月消耗</div>
            <div class="stat-value">{{ statsInfo.currentMonthConsumed || 0 }} 分钟</div>
          </div>
        </el-col>
        <el-col :span="5">
          <div class="stat-item">
            <div class="stat-label">今日消耗</div>
            <div class="stat-value">{{ statsInfo.todayConsumed || 0 }} 分钟</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 数据表格 -->
    <el-table :data="consumeList" v-loading="loading" stripe border style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="userId" label="用户ID" width="80" align="center" />
      <el-table-column prop="consumeTime" label="消耗时间" width="170" align="center" />
      <el-table-column prop="consumeType" label="消耗类型" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="deductionTypeColor(row.consumeType) || 'primary'" size="small">
            {{ deductionTypeLabel(row.consumeType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="consumeDuration" label="消耗时长" width="100" align="center">
        <template #default="{ row }">{{ row.consumeDuration }} 分钟</template>
      </el-table-column>
      <el-table-column prop="projectId" label="项目ID" width="80" align="center">
        <template #default="{ row }">{{ row.projectId || '-' }}</template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">{{ row.description || '-' }}</template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination v-show="total > 0" :total="total" :page-num="pageNum" :page-size="pageSize" @pagination="handlePagination" />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { getDurationStats, listDurationConsumes } from '@/api/recharge'
import { useDict } from '@/composables/useDict'
import Pagination from '@/components/Pagination.vue'

const { options: deductionTypeOptions, label: deductionTypeLabel, color: deductionTypeColor } = useDict('deduction_type')

const loading = ref(false)
const consumeList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dateRange = ref([])
const statsVisible = ref(false)
const statsInfo = ref({})

const queryParams = reactive({
  userId: undefined,
  consumeType: undefined,
  startTime: undefined,
  endTime: undefined
})

function handleDateRange(value) {
  if (value) {
    queryParams.startTime = value[0]
    queryParams.endTime = value[1]
  } else {
    queryParams.startTime = undefined
    queryParams.endTime = undefined
  }
}

function getList(page) {
  if (!queryParams.userId) return
  loading.value = true
  const p = page || {}
  listDurationConsumes(queryParams.userId, {
    pageNum: p.pageNum || pageNum.value,
    pageSize: p.pageSize || pageSize.value,
    consumeType: queryParams.consumeType,
    startTime: queryParams.startTime,
    endTime: queryParams.endTime
  }).then(res => {
    consumeList.value = res.list
    total.value = res.total
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  if (!queryParams.userId) return
  pageNum.value = 1
  getList()
  getDurationStats(queryParams.userId).then(res => {
    statsInfo.value = res
    statsVisible.value = true
  }).catch(() => {})
}

function resetQuery() {
  queryParams.userId = undefined
  queryParams.consumeType = undefined
  queryParams.startTime = undefined
  queryParams.endTime = undefined
  dateRange.value = []
  consumeList.value = []
  total.value = 0
  statsVisible.value = false
}

function handlePagination(pageInfo) {
  pageNum.value = pageInfo.pageNum
  pageSize.value = pageInfo.pageSize
  getList(pageInfo)
}
</script>

<style scoped>
.stat-item { text-align: center; padding: 8px 0; }
.stat-label { font-size: 13px; color: #909399; margin-bottom: 6px; }
.stat-value { font-size: 20px; font-weight: bold; }
.text-primary { color: #409eff; }
.text-success { color: #67c23a; }
.text-warning { color: #e6a23c; }
.mb-4 { margin-bottom: 16px; }
</style>