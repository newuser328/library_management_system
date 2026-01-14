<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="panel panel-a stat-card">
          <div class="stat-title">用户数量（除管理员）</div>
          <div class="stat-value">{{ stats.userCount }}</div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="panel panel-b stat-card">
          <div class="stat-title">当日借书量</div>
          <div class="stat-value">{{ stats.todayBorrowCount }}</div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="panel panel-a stat-card">
          <div class="stat-title">当月借书量</div>
          <div class="stat-value">{{ stats.monthlyBorrowCount }}</div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="never" class="panel panel-b stat-card">
          <div class="stat-title">书籍种类</div>
          <div class="stat-value">{{ stats.categoryCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="panel panel-a" style="margin-top: 16px">
      <div class="toolbar">
        <div class="muted">数据为实时统计（以借阅申请时间 requestTime 计算）。</div>
        <el-button :loading="loading" @click="load">刷新</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { getStatistics } from '@/api/statistics';

const loading = ref(false);
const stats = reactive({
  userCount: 0,
  todayBorrowCount: 0,
  monthlyBorrowCount: 0,
  categoryCount: 0,
});

const load = async () => {
  loading.value = true;
  try {
    const res = await getStatistics();
    stats.userCount = res.userCount ?? 0;
    stats.todayBorrowCount = res.todayBorrowCount ?? 0;
    stats.monthlyBorrowCount = res.monthlyBorrowCount ?? 0;
    stats.categoryCount = res.categoryCount ?? 0;
  } finally {
    loading.value = false;
  }
};

onMounted(load);
</script>

<style scoped>
.page-container { display:flex; flex-direction:column; }
.stat-card { border-radius: 16px; }
.stat-title { font-size: 13px; color: #6b7280; font-weight: 600; }
.stat-value { margin-top: 10px; font-size: 30px; font-weight: 900; line-height: 1; }
.toolbar { display:flex; justify-content:space-between; align-items:center; }
.muted { color: #6b7280; }
</style>
