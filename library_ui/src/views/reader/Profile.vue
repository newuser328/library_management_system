<template>
  <div class="page-container">
    <div class="backdrop" />

    <el-card shadow="never" class="shell panel">
      <div class="header-card panel panel-b">
        <div class="header-left">
          <el-avatar :size="72" :src="avatarUrl" class="avatar" />
          <div class="meta">
            <div class="username">{{ displayUser.username || '-' }}</div>
            <div class="sub muted">
              <el-tag size="small" effect="light" round>
                {{ displayUser.role === 'ADMIN' ? '管理员' : '读者' }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <div class="list">
        <router-link to="/reader/profile/edit" class="item" aria-label="个人资料">
          <div class="icon i1">
            <el-icon><User /></el-icon>
          </div>
          <div class="content">
            <div class="t">个人资料</div>
            <div class="d">修改姓名、头像、联系方式</div>
          </div>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </router-link>

        <router-link to="/reader/profile/security" class="item" aria-label="账号安全">
          <div class="icon i2">
            <el-icon><Lock /></el-icon>
          </div>
          <div class="content">
            <div class="t">账号安全</div>
            <div class="d">修改登录密码</div>
          </div>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </router-link>
      </div>

      <div class="footer">
        <el-popconfirm title="确定要退出登录吗？" confirm-button-text="确定" cancel-button-text="取消" @confirm="handleLogout">
          <template #reference>
            <el-button type="danger" plain class="logout">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
            </el-button>
          </template>
        </el-popconfirm>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock, ArrowRight, SwitchButton } from '@element-plus/icons-vue';
import { useAuthStore } from '@/store/auth';
import request from '@/utils/request';

const authStore = useAuthStore();
const router = useRouter();

const displayUser = ref({
  username: authStore.user?.username || '',
  role: authStore.user?.role || '',
  avatarUrl: authStore.user?.avatarUrl || '',
});

const toPublicUrl = (path) => {
  if (!path) return '';
  if (/^https?:\/\//i.test(path)) return path;
  const origin = request.defaults.baseURL.replace(/\/?api\/?$/, '');
  if (path.startsWith('/')) return origin + path;
  return origin + '/' + path;
};

const avatarUrl = computed(() => {
  const raw = displayUser.value.avatarUrl;
  return raw ? toPublicUrl(raw) : '/empty.png';
});

const loadUserInfo = async () => {
  try {
    const me = await request({ url: '/me', method: 'get' });
    displayUser.value = {
      username: me.username,
      role: me.role,
      avatarUrl: me.avatarUrl,
    };
    authStore.setUser(me);
  } catch (error) {
    console.error('加载用户信息失败:', error);
    ElMessage.error('加载用户信息失败');
  }
};

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
  ElMessage.success('已退出登录');
};

onMounted(() => {
  loadUserInfo();
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

.header-card {
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(229,231,235,0.9);
}

.header-left { display:flex; align-items:center; gap: 16px; }

.avatar {
  border: 2px solid rgba(79,70,229,0.35);
  background: rgba(255,255,255,0.9);
}

.meta { display:flex; flex-direction:column; gap: 8px; min-width: 0; }
.username {
  font-size: 22px;
  font-weight: 900;
  line-height: 1.2;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.list { margin-top: 16px; display:flex; flex-direction:column; gap: 12px; }

.item {
  display:flex;
  align-items:center;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  border: 1px solid rgba(229,231,235,0.9);
  background: rgba(255,255,255,0.82);
  text-decoration: none;
  color: inherit;
  transition: transform 140ms ease, box-shadow 140ms ease, background 140ms ease;
}
.item:hover {
  background: rgba(79,70,229,0.06);
  box-shadow: 0 10px 26px rgba(15,23,42,0.08);
  transform: translateY(-1px);
}

.icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display:flex;
  align-items:center;
  justify-content:center;
  font-size: 18px;
}
.i1 { background: rgba(79,70,229,0.12); color: #4f46e5; }
.i2 { background: rgba(249,115,22,0.12); color: #f97316; }

.content { flex: 1; min-width: 0; display:flex; flex-direction:column; gap: 4px; }
.t { font-weight: 900; }
.d { font-size: 12px; color: rgba(15,23,42,0.60); overflow:hidden; text-overflow: ellipsis; white-space: nowrap; }

.arrow { color: rgba(15,23,42,0.35); }

.footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(229,231,235,0.9);
}

.logout {
  width: 100%;
  height: 46px;
  display:flex;
  align-items:center;
  justify-content:center;
  gap: 8px;
  border-radius: 14px;
}

@media (max-width: 768px) {
  .page-container { padding: 16px 12px; }
  .shell { width: min(680px, 100%); padding: 14px; }
}
</style>
