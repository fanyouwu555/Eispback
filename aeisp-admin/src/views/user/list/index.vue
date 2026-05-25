<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable>
          <el-option
            v-for="item in userStatusOptions"
            :key="item.itemValue"
            :label="item.itemLabel"
            :value="Number(item.itemValue)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="比赛用户" prop="isCompetition">
        <el-select v-model="queryParams.isCompetition" clearable placeholder="请选择" style="width:100px">
          <el-option label="是" :value="1" />
          <el-option label="否" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="注册时间" prop="registerTime">
        <el-date-picker
          v-model="registerTimeRange"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="onRegisterTimeChange"
        />
      </el-form-item>
      <el-form-item label="角色" prop="roleIds">
        <el-select v-model="queryParams.roleIds" placeholder="请选择" clearable multiple>
          <el-option label="超级管理员" :value="1" />
          <el-option label="用户管理员" :value="2" />
          <el-option label="模型管理员" :value="3" />
          <el-option label="消息管理员" :value="4" />
          <el-option label="模板管理员" :value="5" />
          <el-option label="财务管理员" :value="6" />
          <el-option label="普通用户" :value="7" />
        </el-select>
      </el-form-item>
      <el-form-item label="剩余时长">
        <el-input-number v-model="queryParams.remainingMinutesMin" :min="0" placeholder="最低" style="width: 100px" />
        <span style="margin: 0 8px">-</span>
        <el-input-number v-model="queryParams.remainingMinutesMax" :min="0" placeholder="最高" style="width: 100px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button icon="Plus" @click="handleCreate">创建</el-button>
        <el-button icon="Upload" @click="handleImport">导入</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="userList" border>
      <el-table-column type="index" width="50" />
      <el-table-column label="用户ID" prop="id" width="80" />
      <el-table-column label="用户名" prop="username" />
      <el-table-column label="昵称" prop="nickname" />
      <el-table-column label="手机号" prop="phone" />
      <el-table-column label="邮箱" prop="email" />
      <el-table-column label="状态" align="center" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="需改密" align="center" width="70">
        <template #default="{ row }">
          <el-icon v-if="row.needChangePassword === 1" color="#f56c6c" size="18"><WarningFilled /></el-icon>
        </template>
      </el-table-column>
      <el-table-column label="比赛用户" align="center" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isCompetition === 1" type="success" size="small">是</el-tag>
          <span v-else>否</span>
        </template>
      </el-table-column>
      <el-table-column label="剩余时长(分)" prop="remainingMinutes" width="110" />
      <el-table-column label="余额(分)" prop="balanceCents" width="100" />
      <el-table-column label="登录次数" prop="loginCount" width="90" />
      <el-table-column label="异地登录" align="center" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.abnormalLogin === 1" type="danger" size="small">是</el-tag>
          <span v-else>否</span>
        </template>
      </el-table-column>
      <el-table-column label="最后登录" prop="lastLoginTime" width="180" />
      <el-table-column label="注册时间" prop="registerTime" width="180" />
      <el-table-column label="操作" align="center" width="280">
        <template #default="{ row }">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(row)">编辑</el-button>
          <el-button link type="primary" icon="View" @click="handleDetail(row)">详情</el-button>
          <el-button link type="warning" icon="Key" @click="handleResetPwd(row)">重置密码</el-button>
          <el-button link type="success" icon="Timer" @click="handleAdjust(row)">调整时长</el-button>
          <el-button link type="primary" icon="Setting" @click="handlePermission(row)">权限</el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination :total="total" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="getList" />

    <!-- 编辑弹窗 -->
    <el-dialog v-model="open" title="编辑用户" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="form.username" disabled /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option
              v-for="item in userStatusOptions"
              :key="item.itemValue"
              :label="item.itemLabel"
              :value="Number(item.itemValue)"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.status !== form.originalStatus" label="管理员密码">
          <el-input v-model="form.adminPassword" type="password" placeholder="输入当前管理员密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitUpdate">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="pwdOpen" title="重置密码" width="400px">
      <p>确认重置用户 "{{ currentUser?.username }}" 的密码？</p>
      <div style="margin: 15px 0;">
        <el-input v-model="pwdAdminPassword" type="password" placeholder="输入当前管理员密码（二次确认）" show-password />
      </div>
      <p v-if="newPassword" style="color: #f56c6c; margin-top: 10px;">
        <el-icon color="#f56c6c"><WarningFilled /></el-icon>
        新密码：{{ newPassword }}
      </p>
      <template #footer>
        <el-button @click="pwdOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitReset">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 调整时长弹窗 -->
    <el-dialog v-model="durationOpen" title="调整时长" width="400px">
      <el-form :model="durationForm" label-width="80px">
        <el-form-item label="调整类型">
          <el-select v-model="durationForm.adjustType">
            <el-option
              v-for="item in balanceOpOptions"
              :key="item.itemValue"
              :label="item.itemLabel"
              :value="Number(item.itemValue)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="调整数值">
          <el-input-number v-model="durationForm.deltaMinutes" :min="0" :max="99999" />
        </el-form-item>
        <el-form-item label="调整原因">
          <el-input v-model="durationForm.reason" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="管理员密码">
          <el-input v-model="durationForm.adminPassword" type="password" placeholder="输入当前管理员密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="durationOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitAdjust">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 导入弹窗 -->
    <el-dialog v-model="importOpen" title="批量导入用户" width="600px">
      <el-upload
        ref="uploadRef"
        drag
        accept=".xlsx,.xls"
        :auto-upload="false"
        :limit="1"
        :on-change="onImportFileChange"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将Excel文件拖到此处，或<em>点击选择</em></div>
        <template #tip>
          <div class="el-upload__tip">仅支持 .xlsx / .xls 格式，列头：用户名、初始密码、角色、比赛用户、备注</div>
        </template>
      </el-upload>
      <div style="margin-top: 15px;" v-if="importResult">
        <el-alert :title="'导入完成：成功 ' + importResult.successCount + ' 条，失败 ' + importResult.failCount + ' 条'" :type="importResult.failCount > 0 ? 'warning' : 'success'" show-icon />
        <el-table v-if="importResult.failList?.length" :data="importResult.failList" style="margin-top: 10px;" size="small">
          <el-table-column label="行号" prop="rowNum" width="60" />
          <el-table-column label="用户名" prop="username" />
          <el-table-column label="失败原因" prop="reason" />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="importOpen = false">关 闭</el-button>
        <el-button type="primary" @click="submitImport" :disabled="!importFile">开始导入</el-button>
      </template>
    </el-dialog>

    <!-- 创建用户结果弹窗 -->
    <el-dialog v-model="createResultOpen" title="创建用户成功" width="400px">
      <el-alert title="请务必保存以下密码，关闭后无法再次查看" type="warning" show-icon :closable="false" style="margin-bottom: 15px;" />
      <p>用户名：<strong>{{ createResult?.username }}</strong></p>
      <p>
        密 码：<strong style="color: #f56c6c;">{{ createResult?.password }}</strong>
        <el-button link type="primary" @click="copyPassword">复制密码</el-button>
      </p>
      <template #footer>
        <el-button type="primary" @click="createResultOpen = false">已保存，关闭</el-button>
      </template>
    </el-dialog>

    <!-- 创建用户弹窗 -->
    <el-dialog v-model="createOpen" title="创建用户" width="500px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="createForm.username" placeholder="4-20位字母/数字/下划线" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="createForm.password" type="password" show-password placeholder="初始密码" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="createForm.phone" placeholder="11位手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="createForm.email" placeholder="邮箱地址" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="createForm.nickname" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="createForm.roleIds" multiple>
            <el-option label="普通用户" :value="7" />
            <el-option label="用户管理员" :value="2" />
            <el-option label="模型管理员" :value="3" />
            <el-option label="消息管理员" :value="4" />
            <el-option label="模板管理员" :value="5" />
            <el-option label="财务管理员" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="初始化时长(分)">
          <el-input-number v-model="createForm.remainingMinutes" :min="0" />
        </el-form-item>
        <el-form-item label="比赛用户">
          <el-select v-model="createForm.isCompetition" style="width:100px">
            <el-option label="否" :value="0" />
            <el-option label="是" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitCreate">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 用户权限弹窗 -->
    <el-dialog v-model="permOpen" title="用户权限设置" width="550px">
      <el-form label-width="140px" v-if="permKeys.length > 0">
        <el-form-item v-for="key in permKeys" :key="key.key" :label="key.label">
          <template v-if="key.type === 'boolean'">
            <el-switch v-model="permValues[key.key]" :active-value="'true'" :inactive-value="'false'" />
          </template>
          <template v-else>
            <el-input-number v-model="permValues[key.key]" :min="0" :max="99999" />
          </template>
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">默认值: {{ key.defaultValue }}</span>
        </el-form-item>
        <el-form-item v-if="currentPermUser" label="过期时间">
          <el-date-picker v-model="permExpireAt" type="datetime" placeholder="不设置则永不过期" clearable value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
      </el-form>
      <el-empty v-else description="暂无权限配置" />
      <template #footer>
        <el-button @click="permOpen = false">取 消</el-button>
        <el-button type="danger" @click="submitResetPermission">重置为默认</el-button>
        <el-button type="primary" :disabled="permKeys.length === 0" @click="submitPermission">保存</el-button>
      </template>
    </el-dialog>

    <!-- 用户详情弹窗 -->
    <el-dialog v-model="detailOpen" title="用户详情" width="900px" @closed="onDetailClose">
      <el-tabs v-model="detailActiveTab" @tab-change="onDetailTabChange">
        <el-tab-pane label="基本信息" name="info">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户ID">{{ detailUser?.id }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ detailUser?.username }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ detailUser?.nickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ detailUser?.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ detailUser?.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(detailUser?.status)">{{ getStatusLabel(detailUser?.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="余额(分)">{{ detailUser?.balanceCents ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="剩余时长(分)">{{ detailUser?.remainingMinutes ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ detailUser?.registerTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="最后登录">{{ detailUser?.lastLoginTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="登录次数">{{ detailUser?.loginCount ?? 0 }}</el-descriptions-item>
            <el-descriptions-item label="异地登录">{{ detailUser?.abnormalLogin === 1 ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="注册IP">{{ detailUser?.registerIp || '-' }}</el-descriptions-item>
            <el-descriptions-item label="比赛用户">{{ detailUser?.isCompetition === 1 ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="角色">{{ detailUser?.roleCodes?.join(', ') || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        <el-tab-pane label="登录记录" name="loginLogs">
          <el-table v-loading="loginLogLoading" :data="loginLogList" border size="small">
            <el-table-column label="登录账号" prop="loginAccount" />
            <el-table-column label="登录时间" prop="createdAt" width="170" />
            <el-table-column label="登录类型" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.loginType === 1" size="small">密码登录</el-tag>
                <el-tag v-else-if="row.loginType === 2" size="small" type="success">验证码登录</el-tag>
                <el-tag v-else-if="row.loginType === 3" size="small" type="info">Token刷新</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="IP地址" prop="ipAddress" width="130" />
            <el-table-column label="设备类型" prop="deviceType" width="120" />
            <el-table-column label="结果" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.loginResult === 1" size="small" type="success">成功</el-tag>
                <el-tag v-else-if="row.loginResult === 2" size="small" type="danger">密码错误</el-tag>
                <el-tag v-else-if="row.loginResult === 3" size="small" type="warning">账号不存在</el-tag>
                <el-tag v-else-if="row.loginResult === 4" size="small" type="warning">账号禁用</el-tag>
                <el-tag v-else-if="row.loginResult === 5" size="small" type="warning">账号冻结</el-tag>
                <el-tag v-else-if="row.loginResult === 6" size="small" type="warning">账号锁定</el-tag>
                <el-tag v-else-if="row.loginResult === 7" size="small" type="danger">验证码错误</el-tag>
                <el-tag v-else-if="row.loginResult === 8" size="small" type="info">Token过期</el-tag>
                <el-tag v-else size="small" type="info">未知</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <Pagination :total="loginLogTotal" :page-num="loginLogQuery.pageNum" :page-size="loginLogQuery.pageSize" @pagination="loadLoginLogs" />
        </el-tab-pane>
        <el-tab-pane label="资产流水" name="assetFlows">
          <h4 style="margin:0 0 10px;">充值订单</h4>
          <el-table v-loading="orderLoading" :data="orderList" border size="small">
            <el-table-column label="订单号" prop="orderNo" width="180" />
            <el-table-column label="金额" width="80">
              <template #default="{ row }">¥{{ ((row.amount || 0) / 100).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="时长(分)" prop="durationMinutes" width="80" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.status === 0" size="small">待支付</el-tag>
                <el-tag v-else-if="row.status === 1" size="small" type="success">已支付</el-tag>
                <el-tag v-else-if="row.status === 2" size="small" type="warning">已退款</el-tag>
                <el-tag v-else size="small" type="info">已取消</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="下单时间" prop="createdAt" width="170" />
          </el-table>
          <h4 style="margin:15px 0 10px;">时长变更</h4>
          <el-table v-loading="durationLogLoading" :data="durationLogList" border size="small">
            <el-table-column label="操作类型" prop="operationTypeLabel" width="100" />
            <el-table-column label="变更时长" width="100">
              <template #default="{ row }">
                <span :style="{ color: row.changeMinutes > 0 ? '#67c23a' : '#f56c6c' }">
                  {{ row.changeMinutes > 0 ? '+' : '' }}{{ row.changeMinutes }} 分
                </span>
              </template>
            </el-table-column>
            <el-table-column label="原因" prop="reason" />
            <el-table-column label="变更时间" prop="createdAt" width="170" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="项目列表" name="projects">
          <el-table v-loading="projectLoading" :data="projectList" border size="small">
            <el-table-column label="项目名称" prop="name" />
            <el-table-column label="创建时间" prop="createdAt" width="170" />
            <el-table-column label="状态" prop="status" width="80" />
          </el-table>
          <Pagination :total="projectTotal" :page-num="projectQuery.pageNum" :page-size="projectQuery.pageSize" @pagination="loadProjects" />
        </el-tab-pane>
        <el-tab-pane label="模板使用" name="templateUsage">
          <el-table v-loading="templateUsageLoading" :data="templateUsageList" border size="small">
            <el-table-column label="模板ID" prop="templateId" width="80" />
            <el-table-column label="模板名称" prop="templateName" />
            <el-table-column label="版本号" prop="versionNo" width="100" />
            <el-table-column label="操作类型" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.actionType === 'download'" size="small">下载</el-tag>
                <el-tag v-else size="small" type="info">{{ row.actionType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="使用时间" prop="createdAt" width="170" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="AI对话" name="aiSessions">
          <el-table v-loading="sessionLoading" :data="sessionList" border size="small">
            <el-table-column label="会话标题" prop="title" />
            <el-table-column label="消息条数" prop="messageCount" width="80" />
            <el-table-column label="创建时间" prop="createdAt" width="170" />
            <el-table-column label="最后对话" prop="lastMessageAt" width="170" />
          </el-table>
          <Pagination :total="sessionTotal" :page-num="sessionQuery.pageNum" :page-size="sessionQuery.pageSize" @pagination="loadSessions" />
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { WarningFilled, UploadFilled, Plus } from '@element-plus/icons-vue'
import { listUsers, getUser, updateUser, updateUserStatus, resetUserPassword, adjustDuration, importUsers, createUser, listLoginLogs, listDurationLogs } from '@/api/user'
import { getUserPermissions, updateUserPermissions, resetUserPermissions, getPermissionKeys } from '@/api/user/permission'
import { listOrders } from '@/api/recharge'
import { listProjects } from '@/api/project'
import { listAiSessions } from '@/api/ai'
import { getUserTemplateUsageLogs } from '@/api/template'
import { useDict } from '@/composables/useDict'
import { useDictStore } from '@/stores/dict'
import Pagination from '@/components/Pagination.vue'

const { options: userStatusOptions } = useDict('user_status')
const { options: balanceOpOptions } = useDict('balance_op_type')
const dictStore = useDictStore()

const loading = ref(false)
const userList = ref([])
const total = ref(0)
const open = ref(false)
const pwdOpen = ref(false)
const durationOpen = ref(false)
const importOpen = ref(false)
const createResultOpen = ref(false)
const queryRef = ref(null)
const uploadRef = ref(null)
const currentUser = ref(null)
const newPassword = ref('')
const pwdAdminPassword = ref('')
const importFile = ref(null)
const importResult = ref(null)
const createOpen = ref(false)
const createResult = ref(null)

// 详情弹窗
const detailOpen = ref(false)
const detailActiveTab = ref('info')
const detailUser = ref(null)
const loginLogList = ref([])
const loginLogTotal = ref(0)
const loginLogLoading = ref(false)
const loginLogQuery = reactive({ pageNum: 1, pageSize: 5 })
const orderList = ref([])
const orderLoading = ref(false)
const durationLogList = ref([])
const durationLogLoading = ref(false)
const projectList = ref([])
const projectTotal = ref(0)
const projectLoading = ref(false)
const projectQuery = reactive({ pageNum: 1, pageSize: 5 })
const sessionList = ref([])
const sessionTotal = ref(0)
const sessionLoading = ref(false)
const sessionQuery = reactive({ pageNum: 1, pageSize: 5 })
const templateUsageList = ref([])
const templateUsageLoading = ref(false)

const createForm = reactive({
  username: '', password: '', phone: '', email: '', nickname: '',
  roleIds: [], remainingMinutes: 0, isCompetition: 0
})
const registerTimeRange = ref(null)

const queryParams = reactive({
  pageNum: 1, pageSize: 10,
  username: undefined, phone: undefined, status: undefined,
  roleIds: undefined,
  registerTimeStart: undefined, registerTimeEnd: undefined,
  remainingMinutesMin: undefined, remainingMinutesMax: undefined,
  isCompetition: undefined
})
const form = reactive({
  id: undefined, username: '', nickname: '', phone: '', email: '',
  status: 1, originalStatus: 1, adminPassword: ''
})
const durationForm = reactive({ adjustType: 1, deltaMinutes: 0, reason: '', adminPassword: '' })

// 权限管理
const permOpen = ref(false)
const currentPermUser = ref(null)
const permKeys = ref([])
const permValues = reactive({})
const permExpireAt = ref(null)

function getStatusLabel(status) {
  return dictStore.getDictLabel('user_status', status) || '未知'
}
function getStatusType(status) {
  return dictStore.getDictColor('user_status', status) || 'info'
}

function onRegisterTimeChange(range) {
  if (range) {
    queryParams.registerTimeStart = range[0]
    queryParams.registerTimeEnd = range[1]
  } else {
    queryParams.registerTimeStart = undefined
    queryParams.registerTimeEnd = undefined
  }
}

async function getList(pagination = null) {
  loading.value = true
  try {
    if (pagination) { queryParams.pageNum = pagination.page; queryParams.pageSize = pagination.limit }
    const res = await listUsers(queryParams)
    userList.value = res.list || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() {
  queryRef.value?.resetFields()
  registerTimeRange.value = null
  queryParams.registerTimeStart = undefined
  queryParams.registerTimeEnd = undefined
  queryParams.roleIds = undefined
  queryParams.remainingMinutesMin = undefined
  queryParams.remainingMinutesMax = undefined
  queryParams.isCompetition = undefined
  handleQuery()
}

function handleUpdate(row) {
  Object.assign(form, {
    id: row.id, username: row.username, nickname: row.nickname,
    phone: row.phone, email: row.email, status: row.status,
    originalStatus: row.status, adminPassword: ''
  })
  open.value = true
}
async function submitUpdate() {
  const baseData = { phone: form.phone, email: form.email, nickname: form.nickname }
  if (form.status !== form.originalStatus) {
    if (!form.adminPassword) {
      ElMessage.warning('状态已变更，请输入管理员密码')
      return
    }
    await updateUserStatus(form.id, {
      status: form.status,
      reason: '管理员手动修改',
      adminPassword: form.adminPassword
    })
    ElMessage.success('状态修改成功')
  }
  await updateUser(form.id, baseData)
  ElMessage.success('修改成功')
  open.value = false
  getList()
}

function handleResetPwd(row) {
  currentUser.value = row
  newPassword.value = ''
  pwdAdminPassword.value = ''
  pwdOpen.value = true
}
async function submitReset() {
  if (!pwdAdminPassword.value) {
    ElMessage.warning('请输入管理员密码')
    return
  }
  const res = await resetUserPassword(currentUser.value.id, { adminPassword: pwdAdminPassword.value })
  newPassword.value = res || '已重置'
  ElMessage.success('密码重置成功')
}

function handleAdjust(row) {
  currentUser.value = row
  durationForm.adjustType = 1
  durationForm.deltaMinutes = 0
  durationForm.reason = ''
  durationForm.adminPassword = ''
  durationOpen.value = true
}
async function submitAdjust() {
  if (!durationForm.reason || durationForm.reason.trim().length < 5) {
    ElMessage.warning('调整原因不能少于5个字符')
    return
  }
  if (!durationForm.adminPassword) {
    ElMessage.warning('请输入管理员密码')
    return
  }
  await adjustDuration(currentUser.value.id, {
    adjustType: durationForm.adjustType,
    deltaMinutes: durationForm.deltaMinutes,
    reason: durationForm.reason,
    adminPassword: durationForm.adminPassword
  })
  ElMessage.success('时长调整成功')
  durationOpen.value = false
  getList()
}

// 权限管理
async function handlePermission(row) {
  currentPermUser.value = row
  permExpireAt.value = null
  permOpen.value = true
  // 加载权限键定义
  try {
    const keys = await getPermissionKeys()
    permKeys.value = keys || []
  } catch {
    permKeys.value = []
  }
  // 加载用户现有权限值
  try {
    const perms = await getUserPermissions(row.id)
    permKeys.value.forEach(k => {
      const existing = (perms || []).find(p => p.permKey === k.key)
      permValues[k.key] = existing ? existing.permValue : k.defaultValue
      if (existing?.expireAt) {
        permExpireAt.value = existing.expireAt
      }
    })
  } catch {
    permKeys.value.forEach(k => {
      permValues[k.key] = k.defaultValue
    })
  }
}

async function submitPermission() {
  const permissions = permKeys.value.map(k => ({
    permKey: k.key,
    permValue: String(permValues[k.key] ?? k.defaultValue),
    expireAt: permExpireAt.value || null
  }))
  await updateUserPermissions(currentPermUser.value.id, { userId: currentPermUser.value.id, permissions })
  ElMessage.success('权限设置成功')
  permOpen.value = false
}

async function submitResetPermission() {
  if (!currentPermUser.value?.id) return
  await resetUserPermissions(currentPermUser.value.id)
  ElMessage.success('权限已重置为默认值')
  permKeys.value.forEach(k => {
    permValues[k.key] = k.defaultValue
  })
  permExpireAt.value = null
}

function handleCreate() {
  createForm.username = ''; createForm.password = ''; createForm.phone = ''
  createForm.email = ''; createForm.nickname = ''
  createForm.roleIds = []; createForm.remainingMinutes = 0; createForm.isCompetition = 0
  createOpen.value = true
}
async function submitCreate() {
  if (!createForm.username || !createForm.password) {
    ElMessage.warning('用户名和密码不能为空')
    return
  }
  try {
    const password = await createUser({
      username: createForm.username,
      password: createForm.password,
      phone: createForm.phone || undefined,
      email: createForm.email || undefined,
      nickname: createForm.nickname || undefined,
      roleIds: createForm.roleIds.length ? createForm.roleIds : undefined,
      remainingMinutes: createForm.remainingMinutes || undefined,
      isCompetition: createForm.isCompetition
    })
    createOpen.value = false
    createResult.value = { username: createForm.username, password }
    createResultOpen.value = true
    getList()
  } catch (e) {
    ElMessage.error('创建失败：' + (e.message || '未知错误'))
  }
}

function handleImport() {
  importOpen.value = true
  importResult.value = null
  importFile.value = null
  nextTick(() => {
    uploadRef.value?.clearFiles()
  })
}
function onImportFileChange(uploadFile) {
  importFile.value = uploadFile.raw
}
async function submitImport() {
  if (!importFile.value) {
    ElMessage.warning('请选择文件')
    return
  }
  try {
    const res = await importUsers(importFile.value)
    importResult.value = res
    ElMessage.success('导入完成')
  } catch (e) {
    ElMessage.error('导入失败：' + (e.message || '未知错误'))
  }
}

function copyPassword() {
  if (createResult.value?.password) {
    navigator.clipboard.writeText(createResult.value.password).then(() => {
      ElMessage.success('密码已复制')
    }).catch(() => {
      ElMessage.warning('复制失败，请手动复制')
    })
  }
}

// ===== 用户详情 =====
async function handleDetail(row) {
  detailUser.value = row
  detailOpen.value = true
  detailActiveTab.value = 'info'
  try {
    const res = await getUser(row.id)
    detailUser.value = { ...row, ...res }
  } catch { /* use row data as fallback */ }
}

function onDetailClose() {
  loginLogList.value = []
  durationLogList.value = []
  orderList.value = []
  projectList.value = []
  sessionList.value = []
  templateUsageList.value = []
}

function onDetailTabChange(tab) {
  if (tab === 'loginLogs') loadLoginLogs()
  else if (tab === 'assetFlows') { loadOrders(); loadDurationLogs() }
  else if (tab === 'projects') loadProjects()
  else if (tab === 'templateUsage') loadTemplateUsage()
  else if (tab === 'aiSessions') loadSessions()
}

async function loadLoginLogs(pagination) {
  if (!detailUser.value?.id) return
  loginLogLoading.value = true
  if (pagination) { loginLogQuery.pageNum = pagination.page; loginLogQuery.pageSize = pagination.limit }
  try {
    const res = await listLoginLogs(detailUser.value.id, loginLogQuery)
    loginLogList.value = res.list || []
    loginLogTotal.value = res.total || 0
  } finally { loginLogLoading.value = false }
}

async function loadOrders() {
  if (!detailUser.value?.id) return
  orderLoading.value = true
  try {
    const res = await listOrders({ userId: detailUser.value.id, pageNum: 1, pageSize: 5 })
    orderList.value = res.list || []
  } finally { orderLoading.value = false }
}

async function loadDurationLogs() {
  if (!detailUser.value?.id) return
  durationLogLoading.value = true
  try {
    const res = await listDurationLogs(detailUser.value.id, { pageNum: 1, pageSize: 5 })
    durationLogList.value = res.list || []
  } finally { durationLogLoading.value = false }
}

async function loadProjects(pagination) {
  if (!detailUser.value?.id) return
  projectLoading.value = true
  if (pagination) { projectQuery.pageNum = pagination.page; projectQuery.pageSize = pagination.limit }
  try {
    const res = await listProjects({ userId: detailUser.value.id, ...projectQuery })
    projectList.value = res.list || []
    projectTotal.value = res.total || 0
  } finally { projectLoading.value = false }
}

async function loadSessions(pagination) {
  if (!detailUser.value?.id) return
  sessionLoading.value = true
  if (pagination) { sessionQuery.pageNum = pagination.page; sessionQuery.pageSize = pagination.limit }
  try {
    const res = await listAiSessions({ userId: detailUser.value.id, ...sessionQuery })
    sessionList.value = res.list || []
    sessionTotal.value = res.total || 0
  } finally { sessionLoading.value = false }
}

async function loadTemplateUsage() {
  if (!detailUser.value?.id) return
  templateUsageLoading.value = true
  try {
    const res = await getUserTemplateUsageLogs(detailUser.value.id)
    templateUsageList.value = res || []
  } finally { templateUsageLoading.value = false }
}

onMounted(getList)
</script>

<style scoped>
.app-container { padding: 20px; }
</style>