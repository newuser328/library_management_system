<template>
  <div class="page-container">
    <div class="backdrop" />

    <el-card shadow="never" class="shell panel">
      <div class="rows">
        <!-- 头像行 -->
        <div class="row">
          <div class="label">个人头像</div>
          <div class="right avatar-right">
            <input ref="avatarInputRef" type="file" accept="image/*" class="file-input" @change="onAvatarSelected" />
            <el-button :loading="avatarLoading" @click="triggerAvatarPick" size="small" plain>修改头像</el-button>
            <el-image :src="avatarSrc" :preview-src-list="[avatarSrc]" :initial-index="0" fit="cover" class="avatar-preview" style="width: 48px; height: 48px; border-radius: 50%;" />
          </div>
        </div>

        <!-- 用户名 -->
        <el-form ref="profileRef" :model="profile" :rules="profileRules" label-position="top" class="form">
          <el-form-item prop="username" class="row form-row">
            <div class="label">用户名</div>
            <div class="right">
              <template v-if="editing">
                <el-input
                  v-model="profile.username"
                  :disabled="authStore.user?.role !== 'READER'"
                  placeholder="3-64位，仅读者可修改"
                  input-style="text-align: right;"
                />
              </template>
              <template v-else>
                <span class="value">{{ profile.username || '-' }}</span>
              </template>
            </div>
          </el-form-item>

          <!-- 姓名 -->
          <el-form-item prop="name" class="row form-row">
            <div class="label">姓名</div>
            <div class="right">
              <template v-if="editing">
                <el-input v-model="profile.name" placeholder="请输入姓名" input-style="text-align: right;" />
              </template>
              <template v-else>
                <span class="value">{{ profile.name || '-' }}</span>
              </template>
            </div>
          </el-form-item>

          <!-- 电话号码 -->
          <el-form-item prop="phone" class="row form-row">
            <div class="label">电话号码</div>
            <div class="right">
              <template v-if="editing">
                <el-input v-model="profile.phone" placeholder="可选" input-style="text-align: right;" />
              </template>
              <template v-else>
                <span class="value">{{ profile.phone || '-' }}</span>
              </template>
            </div>
          </el-form-item>

          <!-- 邮箱 -->
          <el-form-item prop="email" class="row form-row">
            <div class="label">邮箱</div>
            <div class="right">
              <template v-if="editing">
                <el-input v-model="profile.email" placeholder="可选" input-style="text-align: right;" />
              </template>
              <template v-else>
                <span class="value">{{ profile.email || '-' }}</span>
              </template>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <!-- 按钮区 -->
      <div class="actions">
        <el-button
          v-if="!editing"
          type="primary"
          class="action-btn"
          @click="startEdit"
        >修改个人资料</el-button>

        <el-button
          v-else
          type="primary"
          class="action-btn"
          :loading="profileLoading"
          @click="saveProfile"
        >保存</el-button>

        <el-button plain class="action-btn" @click="onBack">返回</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/store/auth';
import request from '@/utils/request';
import { uploadFile } from '@/api/files';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const editing = ref(false);
const original = ref(null);

const profileRef = ref();
const profileLoading = ref(false);

const profile = reactive({
  username: '',
  name: '',
  phone: '',
  email: '',
  avatarUrl: '',
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
    ElMessage.success('头像已选择，请点击保存生效');
    // 上传头像后自动进入编辑态，避免用户误以为已生效
    if (!editing.value) startEdit();
  } finally {
    avatarLoading.value = false;
    if (e && e.target) e.target.value = '';
  }
};

const snapshot = () => {
  original.value = {
    username: profile.username,
    name: profile.name,
    phone: profile.phone,
    email: profile.email,
    avatarUrl: profile.avatarUrl,
  };
};

const restoreSnapshot = () => {
  if (!original.value) return;
  Object.assign(profile, original.value);
};

const startEdit = () => {
  snapshot();
  editing.value = true;
  profileRef.value?.clearValidate();
};

const onBack = () => {
  if (editing.value) {
    restoreSnapshot();
    editing.value = false;
    profileRef.value?.clearValidate();
    return;
  }
  router.back();
};

const loadMe = async () => {
  const me = await request({ url: '/me', method: 'get' });
  authStore.setUser(me);
  profile.username = me.username || '';
  profile.name = me.name || '';
  profile.phone = me.phone || '';
  profile.email = me.email || '';
  profile.avatarUrl = me.avatarUrl || '';
  snapshot();
};

const saveProfile = async () => {
  await profileRef.value.validate();
  profileLoading.value = true;
  try {
    const oldUsername = authStore.user?.username;

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
    editing.value = false;
    await loadMe();
  } catch (e) {
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

onMounted(() => {
  loadMe();
});
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
    radial-gradient(900px circle at 10% 12%, rgba(34,197,94,0.15) 0%, rgba(255,255,255,0) 55%),
    radial-gradient(900px circle at 92% 18%, rgba(79,70,229,0.12) 0%, rgba(255,255,255,0) 58%);
  pointer-events: none;
}

.shell {
  position: relative;
  width: min(860px, 100%);
  border-radius: 18px;
  border: 1px solid var(--border);
  background: rgba(255,255,255,0.72);
  backdrop-filter: blur(12px);
  padding: 0;
  overflow: hidden;
}

.rows {
  border-bottom: 1px solid var(--border);
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 20px;
  border-bottom: 1px solid var(--border);
}

.row:last-child {
  border-bottom: none;
}

.form { display: block; }

.form-row :deep(.el-form-item__content) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.form-row :deep(.el-form-item__error) {
  left: auto;
  right: 18px;
  text-align: right;
}

.label {
  font-weight: 700;
  color: var(--el-text-color-primary);
  width: 120px;
  font-size: 16px;
  line-height: 1.25;
}

.right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  flex: 1;
  font-size: 16px;
  line-height: 1.25;
}

.value {
  color: rgba(15,23,42,0.78);
  font-weight: 700;
  font-size: 16px;
  line-height: 1.25;
}

.avatar-right { gap: 10px; }
.avatar-preview { border: 1px solid var(--border); cursor: pointer; }
.file-input { display: none; }

:deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: rgba(255,255,255,0.65);
  border: 1px solid rgba(229,231,235,0.9);
  border-radius: 10px;
  padding: 6px 12px;
  font-size: 16px;
}

:deep(.el-input__inner) {
  font-size: 16px;
}

:deep(.el-button) {
  font-size: 16px;
}

.actions {
  display: flex;
  gap: 12px;
  padding: 18px;
  background: rgba(255,255,255,0.60);
}

.action-btn {
  flex: 1;
  height: 44px;
  border-radius: 12px;
}

@media (max-width: 768px) {
  .page-container { padding: 16px 12px; }
  .label { width: 88px; }
  .row { padding: 16px 14px; }
  .actions { padding: 14px; }
}
</style>
