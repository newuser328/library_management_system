<template>
  <div class="page-container">
    <!-- 筛选区 -->
    <el-card shadow="never" class="toolbar-card panel panel-c">
      <div class="toolbar">
        <div class="left">
          <div class="page-title">我的借阅</div>
          <div class="muted">查看你的借阅历史与当前状态</div>
        </div>

        <div class="right">
          <el-select v-model="status" placeholder="状态筛选" clearable style="width: 180px" @change="handleSearch">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已借出" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已归还" value="RETURNED" />
          </el-select>
          <el-button :icon="Refresh" @click="loadBorrows">刷新</el-button>
        </div>
      </div>
    </el-card>

    <!-- 列表区 -->
    <el-card shadow="never" class="panel panel-d">
      <el-table :data="borrowList" v-loading="loading" style="width:100%" row-class-name="table-row">
        <el-table-column prop="bookTitle" label="书名" min-width="240" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="light" round>{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestTime" label="申请时间" width="180" />
        <el-table-column prop="approveTime" label="审核时间" width="180" />
        <el-table-column prop="dueDate" label="应还日期" width="140">
          <template #default="{ row }">
            <span :class="{ danger: isOverdue(row) }">{{ row.dueDate || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="returnTime" label="归还时间" width="180" />
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />

        <template #empty>
          <el-empty description="你还没有借阅记录">
            <el-button type="primary" @click="$router.push('/reader/books')">去借书</el-button>
          </el-empty>
        </template>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          @current-change="loadBorrows"
          layout="total, prev, pager, next, jumper"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { Refresh } from '@element-plus/icons-vue';
import { listMyBorrows } from '@/api/borrows';

const loading = ref(false);
const borrowList = ref([]);
const status = ref('');

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
});

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
  const today = new Date().toISOString().slice(0, 10);
  return row.status === 'APPROVED' && row.dueDate < today;
};

const loadBorrows = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.current - 1,
      size: pagination.size,
    };
    if (status.value) params.status = status.value;

    const res = await listMyBorrows(params);
    borrowList.value = res.content;
    pagination.total = res.totalElements;
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  loadBorrows();
};

onMounted(() => {
  loadBorrows();
});
</script>

<style scoped>
.page-container { display:flex; flex-direction:column; gap: 16px; }
.toolbar { display:flex; justify-content:space-between; gap: 16px; align-items:center; flex-wrap: wrap; }
.left { display:flex; flex-direction:column; gap: 4px; }
.right { display:flex; gap: 12px; align-items:center; flex-wrap: wrap; }
.pagination { margin-top: 16px; display:flex; justify-content:flex-end; }
.danger { color: #ef4444; font-weight: 700; }
</style>
