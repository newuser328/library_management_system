<template>
  <div class="page-container">
    <div class="backdrop" />

    <el-card shadow="never" class="shell panel">
      <div class="form-title">修改密码</div>

      <el-card shadow="never" class="panel panel-d body">

        <el-form ref="pwdRef" :model="pwd" :rules="pwdRules" label-position="top" class="form">
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input v-model="pwd.oldPassword" type="password" show-password placeholder="请输入旧密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwd.newPassword" type="password" show-password placeholder="至少 6 位" />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="pwd.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>

          <div class="actions">
            <el-button type="primary" :loading="pwdLoading" @click="changePassword">修改</el-button>
            <el-button plain @click="clearPwd">清空</el-button>
            <el-button plain @click="$router.back()">返回</el-button>
          </div>
        </el-form>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/store/auth';
import request from '@/utils/request';

const authStore = useAuthStore();

const pwdRef = ref();
const pwdLoading = ref(false);

const pwd = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== pwd.newPassword) {
          callback(new Error('两次输入的新密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur',
    },
  ],
};



const clearPwd = () => {
  pwd.oldPassword = '';
  pwd.newPassword = '';
  pwd.confirmPassword = '';
  pwdRef.value?.clearValidate();
};

const changePassword = async () => {
  await pwdRef.value.validate();
  pwdLoading.value = true;
  try {
    await request({ url: '/me/password', method: 'put', data: pwd });
    ElMessage.success('密码修改成功，请重新登录');
    authStore.logout();
    location.href = '/login';
  } finally {
    pwdLoading.value = false;
  }
};
</script>

<style scoped>
.page-container {
  position: relative;
  display: flex;
  justify-content: center;
  padding: 28px 24px;
}

.backdrop {
  position: absolute;
  inset: 0;
  border-radius: 18px;
  background:
    radial-gradient(900px circle at 10% 12%, rgba(79,70,229,0.20) 0%, rgba(255,255,255,0) 55%),
    radial-gradient(900px circle at 92% 18%, rgba(6,182,212,0.16) 0%, rgba(255,255,255,0) 58%),
    radial-gradient(900px circle at 14% 86%, rgba(236,72,153,0.10) 0%, rgba(255,255,255,0) 60%),
    radial-gradient(900px circle at 86% 88%, rgba(249,115,22,0.10) 0%, rgba(255,255,255,0) 60%);
  pointer-events: none;
}

.shell {
  position: relative;
  width: min(860px, 100%);
  border-radius: 18px;
  border: 1px solid var(--border);
  background: rgba(255,255,255,0.68);
  backdrop-filter: blur(12px);
  padding: 16px;
}

.page-header {
  padding: 14px 14px 10px 14px;
  border-radius: 14px;
  border: 1px solid rgba(229,231,235,0.9);
  background: rgba(255,255,255,0.78);
  backdrop-filter: blur(10px);
}

.header-content { display: flex; flex-direction: column; gap: 4px; }
.title { font-weight: 900; }

.body { margin-top: 12px; padding: 16px; border-radius: 14px; }
.form { max-width: 520px; }
.tip { margin-bottom: 12px; }
.actions { display:flex; gap: 10px; }

@media (max-width: 768px) {
  .page-container { padding: 16px 12px; }
  .shell { width: min(680px, 100%); padding: 14px; }
}
</style>
