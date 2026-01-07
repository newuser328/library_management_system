<template>
  <div class="page-container">
    <el-card shadow="never" class="hero panel panel-b">
      <div class="hero-inner">
        <div>
          <div class="page-title">个人中心</div>
          <div class="muted">维护你的个人资料与账号安全</div>
        </div>
        <el-tag effect="light" round>{{ authStore.user?.username }} · {{ authStore.user?.role }}</el-tag>
      </div>
    </el-card>

    <el-row :gutter="16">
      <!-- 个人资料 -->
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="never" class="panel panel-c">
          <template #header>
            <div class="card-header">
              <div class="card-title">个人资料</div>
              <el-tag type="success" effect="light" round>Profile</el-tag>
            </div>
          </template>

          <el-form ref="profileRef" :model="profile" :rules="profileRules" label-position="top">
            <el-form-item label="用户名">
              <el-input :model-value="authStore.user?.username" disabled />
            </el-form-item>
            <el-form-item label="姓名" prop="name">
              <el-input v-model="profile.name" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profile.phone" placeholder="可选" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profile.email" placeholder="可选" />
            </el-form-item>

            <div class="actions">
              <el-button type="primary" :loading="profileLoading" @click="saveProfile">保存资料</el-button>
              <el-button plain @click="loadMe">重置</el-button>
            </div>
          </el-form>
        </el-card>
      </el-col>

      <!-- 修改密码 -->
      <el-col :xs="24" :sm="24" :md="12">
        <el-card shadow="never" class="panel panel-d">
          <template #header>
            <div class="card-header">
              <div class="card-title">账号安全</div>
              <el-tag type="warning" effect="light" round>Security</el-tag>
            </div>
          </template>

          <el-alert type="info" show-icon :closable="false" class="tip">
            为保障账号安全，修改密码后需要重新登录。
          </el-alert>

          <el-form ref="pwdRef" :model="pwd" :rules="pwdRules" label-position="top">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="pwd.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwd.newPassword" type="password" show-password placeholder="至少 6 位" />
            </el-form-item>

            <div class="actions">
              <el-button type="primary" :loading="pwdLoading" @click="changePassword">修改密码</el-button>
              <el-button plain @click="clearPwd">清空</el-button>
            </div>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/store/auth';
import request from '@/utils/request';

const authStore = useAuthStore();

const profileRef = ref();
const pwdRef = ref();

const profileLoading = ref(false);
const pwdLoading = ref(false);

const profile = reactive({
  name: '',
  phone: '',
  email: '',
});

const pwd = reactive({
  oldPassword: '',
  newPassword: '',
});

const profileRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
};

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '至少6位', trigger: 'blur' },
  ],
};

const loadMe = async () => {
  const me = await request({ url: '/me', method: 'get' });
  authStore.setUser(me);
  profile.name = me.name || '';
  profile.phone = me.phone || '';
  profile.email = me.email || '';
};

const saveProfile = async () => {
  await profileRef.value.validate();
  profileLoading.value = true;
  try {
    const updated = await request({ url: '/me/profile', method: 'put', data: profile });
    authStore.setUser(updated);
    ElMessage.success('保存成功');
  } finally {
    profileLoading.value = false;
  }
};

const clearPwd = () => {
  pwd.oldPassword = '';
  pwd.newPassword = '';
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

onMounted(() => {
  loadMe();
});
</script>

<style scoped>
.page-container { display:flex; flex-direction:column; gap: 16px; }
.hero { background: linear-gradient(135deg, rgba(79,70,229,0.08), rgba(6,182,212,0.08)); }
.hero-inner { display:flex; justify-content:space-between; align-items:center; gap: 12px; flex-wrap: wrap; }
.card-header { display:flex; align-items:center; justify-content:space-between; }
.card-title { font-weight: 900; }
.actions { display:flex; gap: 10px; }
.tip { margin-bottom: 12px; }
</style>
