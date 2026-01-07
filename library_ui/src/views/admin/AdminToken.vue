<template>
  <el-card shadow="never" class="panel panel-c">
    <template #header>
      <div class="header">
        <div class="title">管理员注册口令</div>
        <el-button type="primary" @click="generate" :loading="loading">生成口令</el-button>
      </div>
    </template>

    <el-alert type="info" show-icon :closable="false" class="tip">
      口令规则：仅管理员可生成；字母数字随机；有效期 24 小时；使用一次后失效。
    </el-alert>

    <el-descriptions v-if="token" :column="1" border>
      <el-descriptions-item label="口令">
        <el-input :model-value="token" readonly>
          <template #append>
            <el-button @click="copy">复制</el-button>
          </template>
        </el-input>
      </el-descriptions-item>
      <el-descriptions-item label="过期时间">{{ expiresAt }}</el-descriptions-item>
    </el-descriptions>

    <el-empty v-else description="请点击右上角生成口令" />
  </el-card>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { generateAdminRegisterToken } from '@/api/adminRegisterToken';

const loading = ref(false);
const token = ref('');
const expiresAt = ref('');

const generate = async () => {
  loading.value = true;
  try {
    const res = await generateAdminRegisterToken();
    token.value = res.token;
    expiresAt.value = res.expiresAt;
    ElMessage.success('生成成功');
  } finally {
    loading.value = false;
  }
};

const copy = async () => {
  await navigator.clipboard.writeText(token.value);
  ElMessage.success('已复制');
};
</script>

<style scoped>
.header{ display:flex; align-items:center; justify-content:space-between; }
.title{ font-weight: 800; }
.tip{ margin-bottom: 12px; }
</style>

