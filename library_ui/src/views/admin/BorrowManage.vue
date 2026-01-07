<template>
  <div class="page-container">
    <!-- 筛选区 -->
    <el-card class="toolbar-card panel panel-c" shadow="never">
      <div class="toolbar">
        <div class="filters">
          <el-select v-model="status" placeholder="状态" clearable style="width: 160px" @change="handleSearch">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已借出" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已归还" value="RETURNED" />
          </el-select>

          <el-input
            v-model="titleInitial"
            placeholder="书名拼音首字母（如 ST）"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          >
            <template #prepend>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <el-button :icon="Refresh" @click="load">刷新</el-button>
        </div>

        <div class="summary">
          <el-tag effect="light" round>总计：{{ pagination.total }}</el-tag>
          <el-tag type="warning" effect="light" round>待审核：{{ pendingCount }}</el-tag>
        </div>
      </div>
    </el-card>

    <!-- 列表区 -->
    <el-card class="table-card panel panel-d" shadow="never">
      <el-table :data="list" v-loading="loading" style="width: 100%" row-class-name="table-row">
        <el-table-column prop="id" label="#" width="80" />
        <el-table-column prop="bookTitle" label="书名" min-width="220" show-overflow-tooltip />
        <el-table-column prop="username" label="借阅人" width="140" />

        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="light" round>
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="requestTime" label="申请时间" width="180" />
        <el-table-column prop="dueDate" label="应还日期" width="140">
          <template #default="{ row }">
            <span :class="{ danger: isOverdue(row) }">{{ row.dueDate || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />

        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button-group v-if="row.status === 'PENDING'">
              <el-button size="small" type="success" :icon="CircleCheck" @click="onApprove(row)">通过</el-button>
              <el-button size="small" type="danger" :icon="CircleClose" @click="openReject(row)">拒绝</el-button>
            </el-button-group>

            <el-button
              v-else-if="row.status === 'APPROVED'"
              size="small"
              type="primary"
              plain
              :icon="Finished"
              @click="onReturn(row)"
            >登记归还</el-button>

            <el-tag v-else type="info" effect="plain">无可用操作</el-tag>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无借阅记录" />
        </template>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          @current-change="load"
          layout="total, prev, pager, next, jumper"
        />
      </div>
    </el-card>

    <!-- 拒绝弹窗 -->
    <el-dialog v-model="rejectDialog.visible" title="拒绝借阅" width="520px" top="10vh">
      <el-alert type="warning" show-icon :closable="false" class="tip">
        拒绝后将保留记录，读者可在“我的借阅”中查看结果。
      </el-alert>

      <el-form label-position="top">
        <el-form-item label="拒绝原因（可选）">
          <el-input v-model="rejectDialog.remark" type="textarea" :rows="4" placeholder="例如：库存不足 / 信息不完整 / 需到馆办理等" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="rejectDialog.visible=false">取消</el-button>
        <el-button type="danger" :loading="rejectDialog.loading" @click="submitReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Search, Refresh, CircleCheck, CircleClose, Finished } from '@element-plus/icons-vue';
import { listBorrows, approveBorrow, rejectBorrow, returnBorrow } from '@/api/borrows';

const loading = ref(false);
const list = ref([]);

const status = ref('');
const titleInitial = ref('');

const pagination = reactive({ current: 1, size: 10, total: 0 });

const rejectDialog = reactive({ visible: false, loading: false, id: null, remark: '' });

const pendingCount = computed(() => list.value.filter((x) => x.status === 'PENDING').length);

const statusText = (s) => {
  switch (s) {
    case 'PENDING': return '待审核';
    case 'APPROVED': return '已借出';
    case 'REJECTED': return '已拒绝';
    case 'RETURNED': return '已归还';
    default: return s;
  }
};

const statusTagType = (s) => {
  switch (s) {
    case 'PENDING': return 'warning';
    case 'APPROVED': return 'success';
    case 'REJECTED': return 'danger';
    case 'RETURNED': return 'info';
    default: return 'info';
  }
};

const isOverdue = (row) => {
  if (!row.dueDate) return false;
  // 简化：只判断字符串日期是否小于今天（YYYY-MM-DD）
  const today = new Date().toISOString().slice(0, 10);
  return row.status === 'APPROVED' && row.dueDate < today;
};

const load = async () => {
  loading.value = true;
  try {
    const params = { page: pagination.current - 1, size: pagination.size };
    if (status.value) params.status = status.value;
    if (titleInitial.value) params.titleInitial = titleInitial.value;
    const res = await listBorrows(params);
    list.value = res.content;
    pagination.total = res.totalElements;
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  load();
};

const onApprove = async (row) => {
  await approveBorrow(row.id);
  ElMessage.success('已通过');
  load();
};

const openReject = (row) => {
  rejectDialog.id = row.id;
  rejectDialog.remark = '';
  rejectDialog.visible = true;
};

const submitReject = async () => {
  rejectDialog.loading = true;
  try {
    await rejectBorrow(rejectDialog.id, rejectDialog.remark);
    ElMessage.success('已拒绝');
    rejectDialog.visible = false;
    load();
  } finally {
    rejectDialog.loading = false;
  }
};

const onReturn = async (row) => {
  await returnBorrow(row.id);
  ElMessage.success('已登记归还');
  load();
};

onMounted(load);
</script>

<style scoped>
.page-container { display:flex; flex-direction:column; gap: 16px; }
.toolbar { display:flex; justify-content:space-between; align-items:center; gap: 12px; flex-wrap: wrap; }
.filters { display:flex; gap: 12px; align-items:center; flex-wrap: wrap; }
.summary { display:flex; gap: 10px; align-items:center; }
.pagination { margin-top: 16px; display:flex; justify-content:flex-end; }
:deep(.table-row) { cursor: pointer; }
.danger { color: #ef4444; font-weight: 700; }
.tip { margin-bottom: 12px; }
</style>
