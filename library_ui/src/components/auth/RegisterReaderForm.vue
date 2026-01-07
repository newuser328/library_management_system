<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
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

    <el-button type="primary" class="btn" :loading="loading" @click="onSubmit">注册读者</el-button>
  </el-form>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { register } from '@/api/auth';

const emit = defineEmits(['success']);

const formRef = ref();
const loading = ref(false);
const form = reactive({
  username: '',
  password: '',
  name: '',
  phone: '',
  email: '',
});

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
};

const onSubmit = async () => {
  await formRef.value.validate();
  loading.value = true;
  try {
    await register(form);
    emit('success');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.btn{ width:100%; }
</style>

