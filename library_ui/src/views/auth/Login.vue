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
          <div class="headline">管理更高效 · 借阅更清晰</div>
          <div class="desc">
            管理员可以维护图书与用户，审核借阅记录；读者可浏览图书、发起借阅、查看历史与个人信息。
          </div>

          <div class="chips">
            <div class="chip">Vue3 · Element Plus</div>
            <div class="chip">Spring Boot · JPA</div>
            <div class="chip">JWT · RBAC</div>
          </div>
        </div>

        <div class="brand-bottom">
          <div class="kbd">提示：输入完成后可按 <b>Enter</b> 登录</div>
          <div class="muted small">建议使用 Chrome / Edge 获得最佳体验</div>
        </div>

        <div class="glow g1" />
        <div class="glow g2" />
      </div>

      <!-- 右侧：登录卡片 -->
      <div class="form-panel panel panel-b">
        <el-card class="card" shadow="never">
          <div class="card-header">
            <div class="title">欢迎回来</div>
            <div class="sub muted">{{ loginType === 'password' ? '请使用账号密码登录' : '请使用手机号验证码登录' }}</div>
          </div>

          <!-- 登录方式切换 -->
          <el-tabs v-model="loginType" class="login-tabs">
            <el-tab-pane label="账号密码" name="password" />
            <el-tab-pane label="手机验证码" name="sms" />
          </el-tabs>

          <!-- 账号密码登录 -->
          <el-form
            v-if="loginType === 'password'"
            :model="form"
            :rules="rules"
            ref="formRef"
            label-position="top"
            class="form"
            @submit.prevent="onLogin"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" clearable>
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                show-password
                placeholder="请输入密码"
                @keyup.enter="onLogin"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-button type="primary" class="btn" :loading="loading" @click="onLogin">登录</el-button>

            <div class="actions">
              <el-link type="primary" @click="$router.push('/register')">没有账号？去注册</el-link>
            </div>
          </el-form>

          <!-- 手机号验证码登录 -->
          <el-form
            v-else
            :model="smsForm"
            :rules="smsRules"
            ref="smsFormRef"
            label-position="top"
            class="form"
            @submit.prevent="onSmsLogin"
          >
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="smsForm.phone" placeholder="请输入手机号" clearable maxlength="11">
                <template #prefix>
                  <el-icon><Phone /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="验证码" prop="code">
              <div class="code-input-wrapper">
                <el-input
                  v-model="smsForm.code"
                  placeholder="请输入验证码"
                  maxlength="6"
                  @keyup.enter="onSmsLogin"
                >
                  <template #prefix>
                    <el-icon><Message /></el-icon>
                  </template>
                </el-input>
                <el-button
                  type="primary"
                  :disabled="countdown > 0"
                  :loading="sendingCode"
                  @click="sendCode"
                  class="send-code-btn"
                >
                  {{ countdown > 0 ? `${countdown}秒后重试` : '发送验证码' }}
                </el-button>
              </div>
            </el-form-item>

            <el-button type="primary" class="btn" :loading="loading" @click="onSmsLogin">登录</el-button>

            <div class="actions">
              <el-link type="primary" @click="$router.push('/register')">没有账号？去注册</el-link>
            </div>
          </el-form>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { login, getMe, sendSmsCode, smsLogin } from '@/api/auth';
import { useAuthStore } from '@/store/auth';
import { User, Lock, Phone, Message } from '@element-plus/icons-vue';

const router = useRouter();
const authStore = useAuthStore();

const loginType = ref('password');
const formRef = ref();
const smsFormRef = ref();
const loading = ref(false);
const sendingCode = ref(false);
const countdown = ref(0);
let countdownTimer = null;

const form = reactive({
  username: '',
  password: '',
});

const smsForm = reactive({
  phone: '',
  code: '',
});

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

const smsRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码必须是6位数字', trigger: 'blur' },
  ],
};

const onLogin = async () => {
  await formRef.value.validate();
  loading.value = true;
  try {
    const res = await login(form);
    authStore.setToken(res.token);
    authStore.setNeedSetPassword(res.needSetPassword || false);

    const me = await getMe();
    authStore.setUser(me);

    ElMessage.success('登录成功');
    
    // 如果需要设置密码，跳转到设置密码页面
    if (res.needSetPassword) {
      router.push('/set-password');
    } else if (me.role === 'ADMIN') {
      router.push('/admin/books');
    } else {
      router.push('/reader/books');
    }
  } finally {
    loading.value = false;
  }
};

const sendCode = async () => {
  try {
    await smsFormRef.value.validateField('phone');
    sendingCode.value = true;
    await sendSmsCode(smsForm.phone);
    ElMessage.success('验证码已发送');
    startCountdown();
  } catch (error) {
    if (error.message) {
      ElMessage.error(error.message);
    }
  } finally {
    sendingCode.value = false;
  }
};

const startCountdown = () => {
  countdown.value = 60;
  if (countdownTimer) {
    clearInterval(countdownTimer);
  }
  countdownTimer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(countdownTimer);
      countdownTimer = null;
    }
  }, 1000);
};

const onSmsLogin = async () => {
  await smsFormRef.value.validate();
  loading.value = true;
  try {
    const res = await smsLogin(smsForm.phone, smsForm.code);
    authStore.setToken(res.token);
    authStore.setNeedSetPassword(res.needSetPassword || false);

    const me = await getMe();
    authStore.setUser(me);

    ElMessage.success('登录成功');
    
    // 如果需要设置密码，跳转到设置密码页面
    if (res.needSetPassword) {
      router.push('/set-password');
    } else if (me.role === 'ADMIN') {
      router.push('/admin/books');
    } else {
      router.push('/reader/books');
    }
  } finally {
    loading.value = false;
  }
};

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer);
  }
});
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
.chips { margin-top: 14px; display:flex; gap: 10px; flex-wrap: wrap; }
.chip {
  padding: 8px 10px;
  border-radius: 999px;
  border: 1px solid rgba(255,255,255,0.65);
  background: rgba(255,255,255,0.55);
  font-size: 12px;
}
.brand-bottom { position: absolute; left: 22px; right: 22px; bottom: 18px; }
.kbd {
  display: inline-block;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px dashed rgba(15,23,42,0.25);
  background: rgba(255,255,255,0.55);
}
.small { font-size: 12px; }

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
.login-tabs { margin-bottom: 8px; }
.form { margin-top: 8px; }
.btn { width: 100%; margin-top: 6px; }
.actions { margin-top: 12px; display:flex; justify-content:flex-end; }
.code-input-wrapper {
  display: flex;
  gap: 8px;
}
.code-input-wrapper :deep(.el-input) {
  flex: 1;
}
.send-code-btn {
  white-space: nowrap;
  min-width: 120px;
}

@media (max-width: 960px) {
  .auth-shell { grid-template-columns: 1fr; }
  .brand-panel { display: none; }
  .form-panel { padding: 18px; }
}
</style>
