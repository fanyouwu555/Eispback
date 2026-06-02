<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item label="模板名称" prop="templateName">
        <el-input v-model="queryParams.templateName" placeholder="请输入模板名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="模板编码" prop="templateCode">
        <el-input v-model="queryParams.templateCode" placeholder="编码精确搜索" clearable @keyup.enter="handleQuery" style="width: 180px" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="模板状态" clearable style="width: 120px">
          <el-option v-for="item in templateStatusOptions" :key="item.itemValue" :label="item.itemLabel" :value="Number(item.itemValue)" />
        </el-select>
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <el-cascader
          v-model="categoryPath"
          :options="categoryTreeOptions"
          :props="{ value: 'id', label: 'name', children: 'children', emitPath: false }"
          placeholder="选择分类"
          clearable
          style="width: 200px"
          @change="handleCategoryChange"
        />
      </el-form-item>
      <el-form-item label="付费类型" prop="isPaid">
        <el-select v-model="queryParams.isPaid" placeholder="是否付费" clearable style="width: 120px">
          <el-option label="免费" :value="0" />
          <el-option label="付费" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button type="success" icon="Plus" @click="handleAdd" v-permission="'template:create'">新增模板</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table :data="templateList" v-loading="loading" stripe border style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="templateCode" label="编码" width="180" align="center" />
      <el-table-column prop="templateName" label="模板名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="categoryPath" label="所属分类" min-width="160" show-overflow-tooltip align="center" />
      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="templateStatusColor(row.status) || 'info'" size="small">
            {{ templateStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="currentVersionNo" label="当前版本" width="100" align="center" />
      <el-table-column prop="creator" label="创作者" width="100" align="center" />
      <el-table-column prop="price" label="价格" width="100" align="center">
        <template #default="{ row }">
          <span v-if="row.isPaid === 1">¥{{ row.price }}</span>
          <el-tag v-else type="success" size="small">免费</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="feeType" label="费用类型" width="100" align="center">
        <template #default="{ row }">
          {{ feeTypeLabel(row.feeType) || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="downloadCount" label="下载" width="70" align="center" />
      <el-table-column prop="favoriteCount" label="收藏" width="70" align="center" />
      <el-table-column prop="visitCount" label="访问" width="70" align="center" />
      <el-table-column prop="sortWeight" label="权重" width="70" align="center" />
      <el-table-column prop="usageCount" label="使用次数" width="90" align="center" />
      <el-table-column prop="createdAt" label="创建时间" width="170" align="center" />
      <el-table-column label="操作" width="280" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" icon="View" @click="handleView(row)">详情</el-button>
          <el-button type="primary" link size="small" icon="Edit" @click="handleEdit(row)" v-permission="'template:update'">编辑</el-button>
          <el-button type="success" link size="small" icon="Upload" @click="handleUploadVersion(row)" v-permission="'template:version:manage'">版本</el-button>
          <el-button
            :type="row.status === 1 ? 'warning' : 'success'"
            link
            size="small"
            :icon="row.status === 1 ? 'Hide' : 'View'"
            @click="handleToggleStatus(row)"
            v-permission="'template:update'"
          >{{ row.status === 1 ? '下架' : '上架' }}</el-button>
          <el-button
            v-if="row.status !== 3"
            type="danger" link size="small" icon="Warning"
            @click="handleMarkViolation(row)"
            v-permission="'template:update'"
          >违规</el-button>
          <el-button
            v-if="row.isPaid === 1"
            type="warning" link size="small" icon="Money"
            @click="handlePurchase(row)"
          >购买</el-button>
          <el-button type="danger" link size="small" icon="Delete" @click="handleDelete(row)" v-permission="'template:delete'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination v-show="total > 0" :total="total" :page-num="queryParams.pageNum" :page-size="queryParams.pageSize" @pagination="handlePagination" />

    <!-- 新增模板弹窗 -->
    <el-dialog title="新增模板" v-model="createVisible" width="560px">
      <el-form :model="createForm" ref="createFormRef" label-width="110px" :rules="templateRules">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="createForm.templateName" placeholder="请输入模板名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="权重" prop="sortWeight">
          <el-input-number v-model="createForm.sortWeight" :min="0" :step="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="封面图" prop="coverImage">
          <el-upload ref="coverUploadRef" :auto-upload="false" :limit="1" accept="image/*" @change="handleCoverImageChange">
            <el-button type="primary" icon="Upload">选择封面图</el-button>
            <template #tip><span class="el-upload__tip">支持 JPG/PNG/GIF 等图片格式</span></template>
          </el-upload>
        </el-form-item>
        <el-form-item label="缩略图" prop="thumbnail">
          <el-upload ref="thumbnailUploadRef" :auto-upload="false" :limit="1" accept="image/*" @change="handleThumbnailChange">
            <el-button type="primary" icon="Upload">选择缩略图</el-button>
            <template #tip><span class="el-upload__tip">客户端列表展示用缩略图</span></template>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="2" placeholder="模板描述（可选）" maxlength="500" />
        </el-form-item>
        <el-form-item label="详细描述" prop="detailDesc">
          <el-input v-model="createForm.detailDesc" type="textarea" :rows="3" placeholder="详细介绍详情（可选）" maxlength="2000" />
        </el-form-item>
        <el-form-item label="难度等级" prop="difficulty">
          <el-select v-model="createForm.difficulty" placeholder="选择难度等级" clearable style="width: 200px">
            <el-option label="入门" :value="1" />
            <el-option label="初级" :value="2" />
            <el-option label="中级" :value="3" />
            <el-option label="高级" :value="4" />
            <el-option label="专家" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类" prop="categoryPath">
          <el-cascader
            v-model="createCategoryPath"
            :options="categoryTreeOptions"
            :props="{ value: 'id', label: 'name', children: 'children', emitPath: true }"
            placeholder="选择分类"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="是否付费" prop="isPaid">
          <el-radio-group v-model="createForm.isPaid">
            <el-radio :value="0">免费</el-radio>
            <el-radio :value="1">付费</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="createForm.isPaid === 1" label="费用类型" prop="feeType">
          <el-select v-model="createForm.feeType" placeholder="选择费用类型" style="width: 200px">
            <el-option v-for="item in feeTypeOptions" :key="item.itemValue" :label="item.itemLabel" :value="item.itemValue" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="createForm.isPaid === 1" label="价格" prop="price">
          <el-input-number v-model="createForm.price" :min="0" :precision="2" :step="1" style="width: 200px" />
          <span class="ml-2">元</span>
        </el-form-item>
        <el-form-item label="上线时间" prop="onlineTime">
          <el-date-picker v-model="createForm.onlineTime" type="datetime" placeholder="选择上线时间" style="width: 200px" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="有效截止" prop="validTime">
          <el-date-picker v-model="createForm.validTime" type="datetime" placeholder="选择有效截止时间" style="width: 200px" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="创作者" prop="creator">
          <el-input v-model="createForm.creator" placeholder="创作者名称" maxlength="64" style="width: 200px" />
        </el-form-item>
        <el-form-item label="创作时间" prop="produceDate">
          <el-date-picker v-model="createForm.produceDate" type="date" placeholder="选择创作时间" style="width: 200px" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="关联库资源" prop="libraryIds">
          <el-select v-model="createLibraryIds" multiple clearable placeholder="选择关联的库资源" style="width: 100%">
            <el-option
              v-for="item in onlineLibraryOptions"
              :key="item.id"
              :label="item.resourceName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-divider>初始版本</el-divider>
        <el-form-item label="版本号" prop="versionNo">
          <el-input v-model="createForm.versionNo" placeholder="如 1.0.0" style="width: 200px" />
        </el-form-item>
        <el-form-item label="更新日志" prop="changelog">
          <el-input v-model="createForm.changelog" type="textarea" :rows="2" placeholder="更新说明（可选）" maxlength="500" />
        </el-form-item>
        <el-form-item label="模板文件(ZIP)" prop="zipFile">
          <el-upload ref="zipUploadRef" :auto-upload="false" :limit="1" accept=".zip,application/zip,application/x-zip-compressed" @change="handleZipChange">
            <el-button type="primary" icon="Upload">选择ZIP文件</el-button>
            <template #tip><span class="el-upload__tip">仅支持 .zip 格式</span></template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSubmit" :loading="createLoading">创建</el-button>
      </template>
    </el-dialog>

    <!-- 编辑模板弹窗 -->
    <el-dialog title="编辑模板" v-model="editVisible" width="520px">
      <el-form :model="editForm" ref="editFormRef" label-width="110px" :rules="templateRules">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="editForm.templateName" maxlength="100" />
        </el-form-item>
        <el-form-item label="权重" prop="sortWeight">
          <el-input-number v-model="editForm.sortWeight" :min="0" :step="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="预览图URL" prop="previewImage">
          <el-input v-model="editForm.previewImage" maxlength="500" />
        </el-form-item>
        <el-form-item label="缩略图" prop="thumbnail">
          <div class="flex items-center gap-2">
            <el-input v-model="editForm.thumbnail" maxlength="500" placeholder="缩略图 URL" />
            <el-upload ref="editThumbnailUploadRef" :auto-upload="false" :limit="1" accept="image/*" :show-file-list="false" @change="handleEditThumbnailChange">
              <el-button type="primary" size="small" icon="Upload">上传</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="3" maxlength="500" />
        </el-form-item>
        <el-form-item label="详细描述" prop="detailDesc">
          <el-input v-model="editForm.detailDesc" type="textarea" :rows="3" placeholder="详细介绍详情（可选）" maxlength="2000" />
        </el-form-item>
        <el-form-item label="难度等级" prop="difficulty">
          <el-select v-model="editForm.difficulty" placeholder="选择难度等级" clearable style="width: 200px">
            <el-option label="入门" :value="1" />
            <el-option label="初级" :value="2" />
            <el-option label="中级" :value="3" />
            <el-option label="高级" :value="4" />
            <el-option label="专家" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类" prop="categoryPath">
          <el-cascader
            v-model="editCategoryPath"
            :options="categoryTreeOptions"
            :props="{ value: 'id', label: 'name', children: 'children', emitPath: true }"
            placeholder="选择分类"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="是否付费" prop="isPaid">
          <el-radio-group v-model="editForm.isPaid">
            <el-radio :value="0">免费</el-radio>
            <el-radio :value="1">付费</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="editForm.isPaid === 1" label="费用类型" prop="feeType">
          <el-select v-model="editForm.feeType" placeholder="选择费用类型" style="width: 200px">
            <el-option v-for="item in feeTypeOptions" :key="item.itemValue" :label="item.itemLabel" :value="item.itemValue" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="editForm.isPaid === 1" label="价格" prop="price">
          <el-input-number v-model="editForm.price" :min="0" :precision="2" :step="1" style="width: 200px" />
          <span class="ml-2">元</span>
        </el-form-item>
        <el-form-item label="上线时间" prop="onlineTime">
          <el-date-picker v-model="editForm.onlineTime" type="datetime" placeholder="选择上线时间" style="width: 200px" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="有效截止" prop="validTime">
          <el-date-picker v-model="editForm.validTime" type="datetime" placeholder="选择有效截止时间" style="width: 200px" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="创作者" prop="creator">
          <el-input v-model="editForm.creator" placeholder="创作者名称" maxlength="64" style="width: 200px" />
        </el-form-item>
        <el-form-item label="创作时间" prop="produceDate">
          <el-date-picker v-model="editForm.produceDate" type="date" placeholder="选择创作时间" style="width: 200px" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="关联库资源" prop="libraryIds">
          <el-select v-model="editLibraryIds" multiple clearable placeholder="选择关联的库资源" style="width: 100%">
            <el-option
              v-for="item in onlineLibraryOptions"
              :key="item.id"
              :label="item.resourceName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="editLoading">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog title="模板详情" v-model="detailVisible" width="720px">
      <template v-if="currentDetail">
        <el-descriptions :column="2" border class="mb-4">
          <el-descriptions-item label="ID" :span="1">{{ currentDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="模板名称" :span="1">{{ currentDetail.templateName }}</el-descriptions-item>
          <el-descriptions-item label="状态" :span="1">
            <el-tag v-if="currentDetail.status === 1" type="success" size="small">上架</el-tag>
            <el-tag v-else type="danger" size="small">下架</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="权重" :span="1">{{ currentDetail.sortWeight }}</el-descriptions-item>
          <el-descriptions-item label="难度等级" :span="1">
            {{ difficultyLabel(currentDetail.difficulty) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="使用次数" :span="1">{{ currentDetail.usageCount }}</el-descriptions-item>
          <el-descriptions-item label="创作者" :span="1">{{ currentDetail.creator || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创作时间" :span="1">{{ currentDetail.produceDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="价格" :span="1">
            <span v-if="currentDetail.isPaid === 1">¥{{ currentDetail.price }}（{{ feeTypeLabel(currentDetail.feeType) }}）</span>
            <el-tag v-else type="success" size="small">免费</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下载/收藏/访问" :span="1">
            {{ currentDetail.downloadCount }} / {{ currentDetail.favoriteCount }} / {{ currentDetail.visitCount }}
          </el-descriptions-item>
          <el-descriptions-item label="上线时间" :span="1">{{ currentDetail.onlineTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="有效截止" :span="1">{{ currentDetail.validTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="当前版本" :span="2">
            {{ currentDetail.currentVersion?.versionNo || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="关联库资源" :span="2">
            <template v-if="detailLibraryNames.length > 0">
              <el-tag v-for="name in detailLibraryNames" :key="name" size="small" class="mr-1">{{ name }}</el-tag>
            </template>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentDetail.status === 3" label="违规原因" :span="2">
            <el-tag type="danger">{{ currentDetail.violationReason }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentDetail.description" label="描述" :span="2">{{ currentDetail.description }}</el-descriptions-item>
        </el-descriptions>

        <!-- 版本历史 -->
        <el-divider>版本历史</el-divider>
        <el-table :data="versionList" stripe border size="small">
          <el-table-column prop="versionNo" label="版本号" width="100" />
          <el-table-column prop="changelog" label="更新日志" min-width="200" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="创建时间" width="170" />
          <el-table-column label="ZIP 下载" width="100" align="center">
            <template #default="{ row }">
              <el-link v-if="row.storageUrl" :href="row.storageUrl" target="_blank" type="primary" :underline="false" icon="Download">下载</el-link>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <el-button
                v-if="row.id !== currentDetail.currentVersion?.id"
                type="warning"
                link
                size="small"
                icon="Refresh"
                @click="handleRollback(row)"
                v-permission="'template:version:manage'"
              >回滚</el-button>
              <el-tag v-else size="small" type="success">当前</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 文件树 -->
        <el-divider>文件结构</el-divider>
        <el-tree :data="fileTree" :props="fileTreeProps" default-expand-all highlight-current>
          <template #default="{ node, data }">
            <span>
              <el-icon v-if="data.type === 'dir'"><Folder /></el-icon>
              <el-icon v-else><Document /></el-icon>
              {{ data.name }}
            </span>
          </template>
        </el-tree>
      </template>
    </el-dialog>

    <!-- 上传版本弹窗 -->
    <el-dialog title="上传新版本" v-model="versionVisible" width="480px">
      <el-form :model="versionForm" ref="versionFormRef" label-width="100px" :rules="versionRules">
        <el-form-item label="模板名称">
          <el-input :model-value="versionTemplateName" disabled />
        </el-form-item>
        <el-form-item label="版本号" prop="versionNo">
          <el-input v-model="versionForm.versionNo" placeholder="如 1.1.0" style="width: 200px" />
        </el-form-item>
        <el-form-item label="更新日志" prop="changelog">
          <el-input v-model="versionForm.changelog" type="textarea" :rows="2" placeholder="更新说明（可选）" maxlength="500" />
        </el-form-item>
        <el-divider>同步更新模板信息（可选，留空则不修改）</el-divider>
        <el-form-item label="上线时间" prop="onlineTime">
          <el-date-picker v-model="versionForm.onlineTime" type="datetime" placeholder="留空不更新" style="width: 200px" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="有效截止" prop="validTime">
          <el-date-picker v-model="versionForm.validTime" type="datetime" placeholder="留空不更新" style="width: 200px" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="难度等级" prop="difficulty">
          <el-select v-model="versionForm.difficulty" placeholder="留空不更新" clearable style="width: 200px">
            <el-option label="入门" :value="1" />
            <el-option label="初级" :value="2" />
            <el-option label="中级" :value="3" />
            <el-option label="高级" :value="4" />
            <el-option label="专家" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否付费" prop="isPaid">
          <el-select v-model="versionForm.isPaid" placeholder="留空不更新" clearable style="width: 200px">
            <el-option label="免费" :value="0" />
            <el-option label="付费" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="versionForm.isPaid === 1" label="费用类型" prop="feeType">
          <el-select v-model="versionForm.feeType" placeholder="留空不更新" clearable style="width: 200px">
            <el-option v-for="item in feeTypeOptions" :key="item.itemValue" :label="item.itemLabel" :value="item.itemValue" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="versionForm.isPaid === 1" label="价格" prop="price">
          <el-input-number v-model="versionForm.price" :min="0" :precision="2" :step="1" style="width: 200px" />
          <span class="ml-2">元</span>
        </el-form-item>
        <el-form-item label="ZIP文件" prop="zipFile">
          <el-upload ref="versionUploadRef" :auto-upload="false" :limit="1" accept=".zip,application/zip,application/x-zip-compressed" @change="handleVersionZipChange">
            <el-button type="primary" icon="Upload">选择ZIP文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="versionVisible = false">取消</el-button>
        <el-button type="primary" @click="handleVersionSubmit" :loading="versionLoading">上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onActivated, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Folder, Document } from '@element-plus/icons-vue'
import { useDict } from '@/composables/useDict'
import {
  listTemplates, getTemplate, createTemplate, updateTemplate,
  deleteTemplate, toggleTemplateStatus,
  uploadTemplateVersion, rollbackTemplateVersion,
  uploadTemplateCover, uploadTemplateThumbnail, markTemplateViolation,
  purchaseTemplate
} from '@/api/template'
import { getCategoryTree } from '@/api/template/category'
import { listOnlineLibraries, setTemplateLibraries, getTemplateLibraries, getLibraryDetail } from '@/api/library'
import Pagination from '@/components/Pagination.vue'

const { options: templateStatusOptions, label: templateStatusLabel, color: templateStatusColor } = useDict('template_status')
const { options: feeTypeOptions, label: feeTypeLabel } = useDict('template_fee_type')

const loading = ref(false)
const templateList = ref([])
const total = ref(0)
const createVisible = ref(false)
const createLoading = ref(false)
const editVisible = ref(false)
const editLoading = ref(false)
const detailVisible = ref(false)
const versionVisible = ref(false)
const versionLoading = ref(false)
const currentDetail = ref(null)
const versionList = ref([])
const fileTree = ref([])
const versionTemplateName = ref('')
const versionTemplateId = ref(null)
const zipFile = ref(null)
const versionZipFile = ref(null)
const coverImageFile = ref(null)
const thumbnailFile = ref(null)
const thumbnailUploadRef = ref(null)
const editThumbnailUploadRef = ref(null)
const editThumbnailFile = ref(null)
const coverUploadRef = ref(null)
const zipUploadRef = ref(null)
const versionUploadRef = ref(null)

const categoryPath = ref(null)
const categoryTreeOptions = ref([])
const onlineLibraryOptions = ref([])
const createLibraryIds = ref([])
const editLibraryIds = ref([])
const detailLibraryNames = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  templateName: undefined,
  templateCode: undefined,
  status: undefined,
  topCategoryId: undefined,
  firstCategoryId: undefined,
  secondCategoryId: undefined,
  isPaid: undefined
})

const createForm = reactive({
  templateName: '',
  sortWeight: 0,
  description: '',
  detailDesc: '',
  difficulty: undefined,
  versionNo: '1.0.0',
  changelog: '',
  topCategoryId: undefined,
  firstCategoryId: undefined,
  secondCategoryId: undefined,
  isPaid: 0,
  feeType: undefined,
  price: undefined,
  onlineTime: undefined,
  validTime: undefined,
  creator: '',
  produceDate: undefined
})

const editForm = reactive({
  templateName: '',
  sortWeight: 0,
  previewImage: '',
  thumbnail: '',
  description: '',
  detailDesc: '',
  difficulty: undefined,
  topCategoryId: undefined,
  firstCategoryId: undefined,
  secondCategoryId: undefined,
  isPaid: 0,
  feeType: undefined,
  price: undefined,
  onlineTime: undefined,
  validTime: undefined,
  creator: '',
  produceDate: undefined
})
const editId = ref(null)

const versionForm = reactive({
  versionNo: '',
  changelog: '',
  onlineTime: undefined,
  validTime: undefined,
  difficulty: undefined,
  isPaid: undefined,
  feeType: undefined,
  price: undefined
})

const createCategoryPath = ref([])
const editCategoryPath = ref([])

watch(createCategoryPath, (val) => {
  if (val && val.length > 0) {
    createForm.topCategoryId = val[0]
    createForm.firstCategoryId = val.length > 1 ? val[1] : undefined
    createForm.secondCategoryId = val.length > 2 ? val[2] : undefined
  } else {
    createForm.topCategoryId = undefined
    createForm.firstCategoryId = undefined
    createForm.secondCategoryId = undefined
  }
})

watch(editCategoryPath, (val) => {
  if (val && val.length > 0) {
    editForm.topCategoryId = val[0]
    editForm.firstCategoryId = val.length > 1 ? val[1] : undefined
    editForm.secondCategoryId = val.length > 2 ? val[2] : undefined
  } else {
    editForm.topCategoryId = undefined
    editForm.firstCategoryId = undefined
    editForm.secondCategoryId = undefined
  }
})

const templateRules = {
  templateName: [{ required: true, message: '模板名称不能为空', trigger: 'blur' }],
  sortWeight: [{ required: true, message: '请输入权重', trigger: 'blur' }],
  versionNo: [{ required: true, message: '请输入版本号', trigger: 'blur' }]
}

const versionRules = {
  versionNo: [{ required: true, message: '请输入版本号', trigger: 'blur' }]
}

const fileTreeProps = { children: 'children', label: 'name' }

function difficultyLabel(val) {
  const map = { 1: '入门', 2: '初级', 3: '中级', 4: '高级', 5: '专家' }
  return map[val] || val
}

function getList(page) {
  loading.value = true
  if (page) {
    queryParams.pageNum = page.pageNum
    queryParams.pageSize = page.pageSize
  }
  listTemplates(queryParams).then(res => {
    templateList.value = res.list
    total.value = res.total
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.templateName = undefined
  queryParams.status = undefined
  queryParams.topCategoryId = undefined
  queryParams.firstCategoryId = undefined
  queryParams.secondCategoryId = undefined
  queryParams.isPaid = undefined
  categoryPath.value = null
  handleQuery()
}

function handlePagination(pageInfo) {
  getList(pageInfo)
}

function handleZipChange(uploadFile) {
  zipFile.value = uploadFile.raw
}

function handleCoverImageChange(uploadFile) {
  coverImageFile.value = uploadFile.raw
}

function handleThumbnailChange(uploadFile) {
  thumbnailFile.value = uploadFile.raw
}

function handleEditThumbnailChange(uploadFile) {
  editThumbnailFile.value = uploadFile.raw
}

function handleVersionZipChange(uploadFile) {
  versionZipFile.value = uploadFile.raw
}

function handleAdd() {
  createForm.templateName = ''
  createForm.sortWeight = 0
  createForm.description = ''
  createForm.detailDesc = ''
  createForm.versionNo = '1.0.0'
  createForm.changelog = ''
  createForm.topCategoryId = undefined
  createForm.firstCategoryId = undefined
  createForm.secondCategoryId = undefined
  createForm.isPaid = 0
  createForm.feeType = undefined
  createForm.price = undefined
  createForm.onlineTime = undefined
  createForm.validTime = undefined
  createForm.creator = ''
  createForm.produceDate = undefined
  createCategoryPath.value = []
  createLibraryIds.value = []
  zipFile.value = null
  coverImageFile.value = null
  thumbnailFile.value = null
  createVisible.value = true
  // 清除上传组件的内部文件列表，避免显示上一次的文件
  nextTick(() => {
    zipUploadRef.value?.clearFiles()
    coverUploadRef.value?.clearFiles()
    thumbnailUploadRef.value?.clearFiles()
  })
}

function handleCreateSubmit() {
  if (!zipFile.value) {
    ElMessage.warning('请选择ZIP文件')
    return
  }
  createLoading.value = true
  const fd = new FormData()
  fd.append('templateName', createForm.templateName)
  fd.append('sortWeight', createForm.sortWeight)
  fd.append('description', createForm.description || '')
  fd.append('versionNo', createForm.versionNo)
  fd.append('changelog', createForm.changelog || '')
  fd.append('topCategoryId', createForm.topCategoryId || '')
  fd.append('firstCategoryId', createForm.firstCategoryId || '')
  fd.append('secondCategoryId', createForm.secondCategoryId || '')
  fd.append('isPaid', createForm.isPaid)
  fd.append('feeType', createForm.feeType || '')
  fd.append('price', createForm.price || '')
  fd.append('onlineTime', createForm.onlineTime || '')
  fd.append('validTime', createForm.validTime || '')
  fd.append('creator', createForm.creator || '')
  fd.append('produceDate', createForm.produceDate || '')
  fd.append('detailDesc', createForm.detailDesc || '')
  fd.append('difficulty', createForm.difficulty !== undefined && createForm.difficulty !== null ? createForm.difficulty : '')
  fd.append('zipFile', zipFile.value)
  if (coverImageFile.value) fd.append('coverImage', coverImageFile.value)
  if (thumbnailFile.value) fd.append('thumbnail', thumbnailFile.value)
  createTemplate(fd).then(templateId => {
    if (templateId) {
      return setTemplateLibraries(templateId, { libraryIds: createLibraryIds.value })
    }
  }).then(() => {
    ElMessage.success('创建成功')
    createVisible.value = false
    getList()
  }).finally(() => {
    createLoading.value = false
  })
}

function handleEdit(row) {
  editId.value = row.id
  editForm.templateName = row.templateName
  editForm.sortWeight = row.sortWeight
  editForm.previewImage = row.previewImage || ''
  editForm.thumbnail = row.thumbnail || ''
  editThumbnailFile.value = null
  editForm.description = row.description || ''
  editForm.detailDesc = row.detailDesc || ''
  editForm.difficulty = row.difficulty
  editForm.isPaid = row.isPaid || 0
  editForm.feeType = row.feeType
  editForm.price = row.price
  editForm.onlineTime = row.onlineTime
  editForm.validTime = row.validTime
  editForm.creator = row.creator || ''
  editForm.produceDate = row.produceDate
  editForm.topCategoryId = row.topCategoryId
  editForm.firstCategoryId = row.firstCategoryId
  editForm.secondCategoryId = row.secondCategoryId
  // Build cascader path from category IDs
  const leafId = row.secondCategoryId || row.firstCategoryId || row.topCategoryId
  editCategoryPath.value = leafId ? (findPathToNode(categoryTreeOptions.value, leafId) || []) : []
  // Load associated libraries (backend returns List<Long>)
  editLibraryIds.value = []
  getTemplateLibraries(row.id).then(res => {
    editLibraryIds.value = Array.isArray(res) ? res : []
  }).catch(() => {})
  editVisible.value = true
}

function handleEditSubmit() {
  editLoading.value = true
  updateTemplate(editId.value, {
    templateName: editForm.templateName,
    sortWeight: editForm.sortWeight,
    previewImage: editForm.previewImage,
    thumbnail: editForm.thumbnail,
    description: editForm.description,
    detailDesc: editForm.detailDesc,
    difficulty: editForm.difficulty,
    topCategoryId: editForm.topCategoryId,
    firstCategoryId: editForm.firstCategoryId,
    secondCategoryId: editForm.secondCategoryId,
    isPaid: editForm.isPaid,
    feeType: editForm.feeType,
    price: editForm.price,
    onlineTime: editForm.onlineTime,
    validTime: editForm.validTime,
    creator: editForm.creator,
    produceDate: editForm.produceDate
  }).then(() => {
    // 如果有选中缩略图文件，上传到资源服务器
    if (editThumbnailFile.value) {
      const fd = new FormData()
      fd.append('file', editThumbnailFile.value)
      return uploadTemplateThumbnail(editId.value, fd).then(res => {
        // 上传成功后更新缩略图 URL
        editForm.thumbnail = res
      })
    }
  }).then(() => {
    return setTemplateLibraries(editId.value, { libraryIds: editLibraryIds.value })
  }).then(() => {
    ElMessage.success('保存成功')
    editVisible.value = false
    getList()
  }).catch(() => {}).finally(() => {
    editLoading.value = false
    editThumbnailFile.value = null
  })
}

function handleView(row) {
  getTemplate(row.id).then(res => {
    currentDetail.value = res
    versionList.value = [res.currentVersion, ...(res.historyVersions || [])].filter(Boolean)
    fileTree.value = res.fileTree || []
    detailLibraryNames.value = []
    getTemplateLibraries(row.id).then(libRes => {
      const libIds = Array.isArray(libRes) ? libRes : []
      if (libIds.length > 0) {
        Promise.all(libIds.map(id => getLibraryDetail(id).catch(() => null)))
          .then(details => {
            detailLibraryNames.value = details
              .filter(d => d && d.resourceName)
              .map(d => d.resourceName)
          })
      }
    }).catch(() => {})
    detailVisible.value = true
  })
}

function handleUploadVersion(row) {
  versionTemplateId.value = row.id
  versionTemplateName.value = row.templateName
  versionForm.versionNo = ''
  versionForm.changelog = ''
  versionForm.onlineTime = undefined
  versionForm.validTime = undefined
  versionForm.difficulty = undefined
  versionForm.isPaid = undefined
  versionForm.feeType = undefined
  versionForm.price = undefined
  versionZipFile.value = null
  versionVisible.value = true
  nextTick(() => {
    versionUploadRef.value?.clearFiles()
  })
}

function handleVersionSubmit() {
  if (!versionZipFile.value) {
    ElMessage.warning('请选择ZIP文件')
    return
  }
  versionLoading.value = true
  const fd = new FormData()
  fd.append('versionNo', versionForm.versionNo)
  fd.append('changelog', versionForm.changelog || '')
  if (versionForm.onlineTime) fd.append('onlineTime', versionForm.onlineTime)
  if (versionForm.validTime) fd.append('validTime', versionForm.validTime)
  if (versionForm.difficulty !== undefined && versionForm.difficulty !== null) fd.append('difficulty', versionForm.difficulty)
  if (versionForm.isPaid !== undefined && versionForm.isPaid !== null) fd.append('isPaid', versionForm.isPaid)
  if (versionForm.feeType) fd.append('feeType', versionForm.feeType)
  if (versionForm.price !== undefined && versionForm.price !== null) fd.append('price', versionForm.price)
  fd.append('zipFile', versionZipFile.value)
  uploadTemplateVersion(versionTemplateId.value, fd).then(() => {
    ElMessage.success('版本上传成功')
    versionVisible.value = false
    getList()
  }).finally(() => {
    versionLoading.value = false
  })
}

function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 2 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  ElMessageBox.confirm(`确认${action}模板「${row.templateName}」？`, '状态确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    toggleTemplateStatus(row.id, newStatus).then(() => {
      ElMessage.success(`${action}成功`)
      getList()
    })
  }).catch(() => {})
}

function handleRollback(version) {
  ElMessageBox.confirm(
    `确认回滚到版本 ${version.versionNo}？回滚仅改变当前版本指向，不会删除其他版本。`,
    '回滚确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    rollbackTemplateVersion(currentDetail.value.id, version.id).then(() => {
      ElMessage.success('回滚成功')
      detailVisible.value = false
      getList()
    })
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除模板「${row.templateName}」及其所有版本？`, '删除确认', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'danger'
  }).then(() => {
    deleteTemplate(row.id).then(() => {
      ElMessage.success('删除成功')
      getList()
    })
  }).catch(() => {})
}

function loadCategories() {
  getCategoryTree().then(res => {
    categoryTreeOptions.value = res || []
  })
}

function loadOnlineLibraries() {
  listOnlineLibraries().then(res => {
    onlineLibraryOptions.value = res || []
  }).catch(() => {
    onlineLibraryOptions.value = []
  })
}

function handleCategoryChange(val) {
  queryParams.topCategoryId = undefined
  queryParams.firstCategoryId = undefined
  queryParams.secondCategoryId = undefined
  if (!val) return
  const node = findNode(categoryTreeOptions.value, val)
  if (node) {
    const lvl = node.level !== undefined ? node.level : 0
    if (lvl === 0) queryParams.topCategoryId = val
    else if (lvl === 1) queryParams.firstCategoryId = val
    else if (lvl === 2) queryParams.secondCategoryId = val
  }
}

function findNode(nodes, id) {
  for (const n of nodes || []) {
    if (n.id === id) return n
    if (n.children) {
      const found = findNode(n.children, id)
      if (found) return found
    }
  }
  return null
}

function findPathToNode(nodes, id, path = []) {
  for (const n of nodes || []) {
    if (n.id === id) {
      return [...path, n.id]
    }
    if (n.children) {
      const found = findPathToNode(n.children, id, [...path, n.id])
      if (found) return found
    }
  }
  return null
}

function handleMarkViolation(row) {
  ElMessageBox.prompt('请输入违规原因', '标记违规', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '违规原因不能为空'
  }).then(({ value }) => {
    markTemplateViolation(row.id, value).then(() => {
      ElMessage.success('已标记为违规')
      getList()
    })
  }).catch(() => {})
}

function handlePurchase(row) {
  ElMessageBox.confirm(`确认购买模板「${row.templateName}」？价格：¥${row.price}`, '购买确认', {
    confirmButtonText: '确认购买',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    purchaseTemplate(row.id).then(() => {
      ElMessage.success('购买成功')
    }).catch(err => {
      const msg = err?.response?.data?.message || '购买失败'
      ElMessage.error(msg)
    })
  }).catch(() => {})
}

onMounted(() => {
  getList()
  loadCategories()
  loadOnlineLibraries()
})

onActivated(() => {
  loadCategories()
  loadOnlineLibraries()
})
</script>