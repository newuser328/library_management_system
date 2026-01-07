<template>
  <el-alert type="info" show-icon :closable="false" class="tip">
    管理员注册需要一次性口令：由已登录管理员在“管理端 / 管理员口令”页面生成（24小时有效）。
  </el-alert>

  <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
    <el-form-item label="管理员口令" prop="adminToken">
      <el-input v-model="form.adminToken" placeholder="请输入管理员口令" />
    </el-form-item>

    <el-form-item label="用户名" prop="username">
      <el-input v-model="form.username" placeholder="3-64位" />
    </el-form-item>
    <el-form-item label="密码" prop="password">
      <el-input v-model="form.password" type="password" show-password placeholder="至少6位" />
    </el-form-item>
    <el-form-item label="姓名" prop="name">
      <el-input v-model="form.name" placeholder="请输入姓名" />
    </el-form-item>
    <el-form-item label="手机号" prop="phone">
      <el-input v-model="form.phone" placeholder="可选" />
    </el-form-item>
    <el-form-item label="邮箱" prop="email">
      <el-input v-model="form.email" placeholder="可选" />
    </el-form-item>

    <el-button type="primary" class="btn" :loading="loading" @click="onSubmit">注册管理员</el-button>
  </el-form>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { registerAdmin } from '@/api/auth';

const emit = defineEmits(['success']);

const formRef = ref();
const loading = ref(false);

const form = reactive({
  adminToken: '',
  username: '',
  password: '',
  name: '',
  phone: '',
  email: '',
});

const rules = {
  adminToken: [{ required: true, message: '请输入管理员口令', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
};

const onSubmit = async () => {
  await formRef.value.validate();
  loading.value = true;
  try {
    await registerAdmin(form);
    emit('success');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.tip{ margin-bottom: 12px; }
.btn{ width:100%; }
</style>

