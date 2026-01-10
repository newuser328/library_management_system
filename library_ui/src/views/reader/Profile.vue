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
            <el-form-item label="头像">
              <div class="avatar-row">
                <el-avatar :size="64" :src="avatarSrc" />
                <div class="avatar-actions">
                  <input ref="avatarInputRef" type="file" accept="image/*" class="file-input" @change="onAvatarSelected" />
                  <el-button :loading="avatarLoading" @click="triggerAvatarPick">上传头像</el-button>
                  <div class="muted small">支持 png/jpg/jpeg/webp，最大 10MB</div>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="用户名" prop="username">
              <el-input v-model="profile.username" :disabled="authStore.user?.role !== 'READER'" placeholder="仅支持字母/数字/下划线，长度3-64" />
              <div v-if="authStore.user?.role === 'READER'" class="muted small mt8">修改用户名后需要重新登录</div>
              <div v-else class="muted small mt8">管理员不允许在此修改用户名</div>
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
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/store/auth';
import request from '@/utils/request';
import { uploadFile } from '@/api/files';

const authStore = useAuthStore();

const profileRef = ref();
const pwdRef = ref();

const profileLoading = ref(false);
const pwdLoading = ref(false);

const profile = reactive({
  username: '',
  name: '',
  phone: '',
  email: '',
  avatarUrl: '',
});

const pwd = reactive({
  oldPassword: '',
  newPassword: '',
});

const profileRules = {
  username: [
    {
      validator: (_rule, value, callback) => {
        if (authStore.user?.role !== 'READER') return callback();
        if (!value || !String(value).trim()) return callback(new Error('请输入用户名'));
        if (String(value).trim().length < 3) return callback(new Error('至少3位'));
        return callback();
      },
      trigger: 'blur',
    },
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
};

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '至少6位', trigger: 'blur' },
  ],
};

const toPublicUrl = (path) => {
  if (!path) return '';
  if (/^https?:\/\//i.test(path)) return path;
  const origin = request.defaults.baseURL.replace(/\/?api\/?$/, '');
  if (path.startsWith('/')) return origin + path;
  return origin + '/' + path;
};

const avatarSrc = computed(() => {
  const raw = profile.avatarUrl || authStore.user?.avatarUrl;
  return raw ? toPublicUrl(raw) : '/empty.png';
});

const avatarInputRef = ref();
const avatarLoading = ref(false);

const triggerAvatarPick = () => {
  avatarInputRef.value?.click();
};

const onAvatarSelected = async (e) => {
  const file = e?.target?.files?.[0];
  if (!file) return;

  avatarLoading.value = true;
  try {
    const uploaded = await uploadFile(file);
    profile.avatarUrl = uploaded.url;

    const payload = {
      name: profile.name,
      phone: profile.phone,
      email: profile.email,
      avatarUrl: profile.avatarUrl,
    };
    if (authStore.user?.role === 'READER') {
      payload.username = profile.username;
    }

    const updated = await request({ url: '/me/profile', method: 'put', data: payload });
    authStore.setUser(updated);
    ElMessage.success('头像已更新');
  } finally {
    avatarLoading.value = false;
    // 允许同一文件重复选择触发 change
    if (e && e.target) e.target.value = '';
  }
};

const loadMe = async () => {
  const me = await request({ url: '/me', method: 'get' });
  authStore.setUser(me);
  profile.username = me.username || '';
  profile.name = me.name || '';
  profile.phone = me.phone || '';
  profile.email = me.email || '';
  profile.avatarUrl = me.avatarUrl || '';
};

const saveProfile = async () => {
  await profileRef.value.validate();
  profileLoading.value = true;
  try {
    const oldUsername = authStore.user?.username;

    // 管理员不允许修改 username：提交时直接不带 username
    const payload = {
      name: profile.name,
      phone: profile.phone,
      email: profile.email,
      avatarUrl: profile.avatarUrl,
    };
    if (authStore.user?.role === 'READER') {
      payload.username = profile.username;
    }

    const updated = await request({ url: '/me/profile', method: 'put', data: payload });
    authStore.setUser(updated);

    if (authStore.user?.role === 'READER' && oldUsername && updated?.username && oldUsername !== updated.username) {
      ElMessage.success('用户名已修改，请重新登录');
      authStore.logout();
      location.href = '/login';
      return;
    }

    ElMessage.success('保存成功');
  } catch (e) {
    // 兼容后端拒绝管理员改用户名时的提示
    const msg = e?.message || e?.data?.message;
    if (msg && msg.includes('管理员不允许修改用户名')) {
      ElMessage.warning(msg);
      return;
    }
    throw e;
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
.small { font-size: 12px; }
.mt8 { margin-top: 8px; }
.avatar-row { display:flex; align-items:center; gap: 12px; }
.avatar-actions { display:flex; flex-direction:column; gap: 6px; }
.file-input { display:none; }
</style>
