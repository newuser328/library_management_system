<template>
  <div class="auth-page">
    <div class="auth-shell">
      <!-- 左侧：品牌/插画区 -->
      <div class="brand-panel panel panel-a">
        <div class="brand-top">
          <div class="logo">L</div>
          <div>
            <div class="brand-title">图书馆管理系统</div>
            <div class="brand-sub">Library Management System</div>
          </div>
        </div>

        <div class="brand-center">
          <div class="headline">设置登录密码</div>
          <div class="desc">
            为了您的账户安全，请设置一个登录密码。设置完成后，您可以使用手机号+密码或手机号+验证码两种方式登录。
          </div>
        </div>

        <div class="glow g1" />
        <div class="glow g2" />
      </div>

      <!-- 右侧：设置密码卡片 -->
      <div class="form-panel panel panel-b">
        <el-card class="card" shadow="never">
          <div class="card-header">
            <div class="title">设置密码</div>
            <div class="sub muted">请设置您的登录密码</div>
          </div>

          <el-form
            :model="form"
            :rules="rules"
            ref="formRef"
            label-position="top"
            class="form"
            @submit.prevent="onSubmit"
          >
            <el-form-item label="新密码" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                show-password
                placeholder="请输入密码（6-64位）"
                @keyup.enter="onSubmit"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入密码"
                @keyup.enter="onSubmit"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-button type="primary" class="btn" :loading="loading" @click="onSubmit">设置密码</el-button>
          </el-form>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { setPassword, getMe } from '@/api/auth';
import { useAuthStore } from '@/store/auth';
import { Lock } from '@element-plus/icons-vue';

const router = useRouter();
const authStore = useAuthStore();

const formRef = ref();
const loading = ref(false);

const form = reactive({
  password: '',
  confirmPassword: '',
});

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const rules = {
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 64, message: '密码长度必须在6-64位之间', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
};

const onSubmit = async () => {
  await formRef.value.validate();
  loading.value = true;
  try {
    await setPassword(form.password);
    ElMessage.success('密码设置成功');
    
    // 刷新用户信息
    const me = await getMe();
    authStore.setUser(me);
    
    // 清除需要设置密码的标识（在刷新用户信息后）
    authStore.setNeedSetPassword(false);
    
    // 根据角色跳转（使用replace避免返回时回到设置密码页面）
    if (me.role === 'ADMIN') {
      router.replace('/admin/books');
    } else {
      router.replace('/reader/books');
    }
  } catch (error) {
    // 错误信息已在request拦截器中处理
    console.error('设置密码失败:', error);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18px;
}
.auth-shell {
  width: min(1100px, 100%);
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid rgba(229,231,235,0.9);
  background: rgba(255,255,255,0.65);
  backdrop-filter: blur(12px);
}

.brand-panel {
  position: relative;
  padding: 22px;
  color: #0f172a;
  background: linear-gradient(135deg, rgba(79,70,229,0.14), rgba(6,182,212,0.12));
}
.brand-top {
  display: flex;
  gap: 12px;
  align-items: center;
}
.logo {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  color: white;
  background: linear-gradient(135deg, #4f46e5, #06b6d4);
  box-shadow: 0 14px 35px rgba(79,70,229,0.25);
}
.brand-title { font-size: 20px; font-weight: 900; line-height: 1.1; }
.brand-sub { font-size: 12px; color: rgba(15,23,42,0.65); margin-top: 2px; }

.brand-center { margin-top: 28px; max-width: 460px; }
.headline { font-size: 24px; font-weight: 900; letter-spacing: 0.3px; }
.desc { margin-top: 10px; color: rgba(15,23,42,0.75); line-height: 1.7; }

.glow {
  position: absolute;
  border-radius: 999px;
  filter: blur(40px);
  opacity: 0.7;
}
.g1 { width: 260px; height: 260px; background: rgba(79,70,229,0.35); top: -80px; left: -80px; }
.g2 { width: 240px; height: 240px; background: rgba(6,182,212,0.30); bottom: -90px; right: -90px; }

.form-panel {
  padding: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.65);
}
.card {
  width: min(420px, 100%);
  border-radius: 16px;
}
.card-header { margin-bottom: 10px; }
.title { font-size: 22px; font-weight: 900; }
.sub { margin-top: 4px; }
.form { margin-top: 8px; }
.btn { width: 100%; margin-top: 6px; }

@media (max-width: 960px) {
  .auth-shell { grid-template-columns: 1fr; }
  .brand-panel { display: none; }
  .form-panel { padding: 18px; }
}
</style>
