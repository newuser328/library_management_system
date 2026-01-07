<template>
  <el-container class="layout">
    <el-aside width="240px" class="aside">
      <div class="brand">
        <div class="brand-logo">L</div>
        <div class="brand-text">
          <div class="brand-title">图书馆系统</div>
          <div class="brand-sub">读者端</div>
        </div>
      </div>

      <el-menu :default-active="active" router class="menu">
        <el-menu-item index="/reader/books">图书浏览</el-menu-item>
        <el-menu-item index="/reader/my-borrows">我的借阅</el-menu-item>
        <el-menu-item index="/reader/profile">个人中心</el-menu-item>
      </el-menu>

      <div class="aside-footer">
        <div class="user-chip">
          <div class="dot" />
          <div class="user-meta">
            <div class="u1">{{ authStore.user?.username }}</div>
            <div class="u2">{{ authStore.user?.role }}</div>
          </div>
        </div>
      </div>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <div class="page-title gradient-text">{{ title }}</div>
          <div class="muted">发现好书，从这里开始</div>
        </div>
        <div class="header-right">
          <el-button v-if="authStore.user?.role === 'ADMIN'" type="primary" plain @click="goAdmin">切到管理端</el-button>
          <el-button type="danger" plain @click="onLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const active = computed(() => route.path);

const title = computed(() => {
  if (route.path.includes('/reader/books')) return '图书浏览';
  if (route.path.includes('/reader/my-borrows')) return '我的借阅';
  if (route.path.includes('/reader/profile')) return '个人中心';
  return '读者端';
});

const onLogout = () => {
  authStore.logout();
  router.push('/login');
};

const goAdmin = () => {
  router.push('/admin/books');
};
</script>

<style scoped>
.layout { min-height: 100vh; background: transparent; }
.aside {
  background: linear-gradient(180deg, rgba(255,255,255,0.92), rgba(255,255,255,0.82));
  border-right: 1px solid rgba(229,231,235,0.9);
  backdrop-filter: blur(10px);
  position: relative;
}
.brand { height: 64px; display:flex; align-items:center; gap: 12px; padding: 0 16px; }
.brand-logo {
  width: 34px; height: 34px; border-radius: 10px;
  display:flex; align-items:center; justify-content:center;
  font-weight: 900; color: white;
  background: linear-gradient(135deg, #4f46e5, #22c55e);
  box-shadow: 0 10px 30px rgba(79,70,229,0.2);
}
.brand-title { font-weight: 900; line-height: 1.1; }
.brand-sub { font-size: 12px; color: #6b7280; margin-top: 2px; }

.menu { border-right: none; padding: 8px; background: transparent; }

.header {
  background: rgba(255,255,255,0.75);
  border-bottom: 1px solid rgba(229,231,235,0.9);
  display: flex;
  align-items: center;
  justify-content: space-between;
  backdrop-filter: blur(10px);
}
.header-left { display:flex; flex-direction:column; gap: 2px; }
.header-right { display:flex; gap: 10px; align-items:center; }

.main { padding: 16px; }

.aside-footer {
  position: absolute;
  bottom: 0;
  width: 240px;
  padding: 12px;
  box-sizing: border-box;
}
.user-chip {
  display:flex;
  align-items:center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid rgba(229,231,235,0.9);
  background: rgba(255,255,255,0.8);
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: #06b6d4;
  box-shadow: 0 0 0 6px rgba(6,182,212,0.12);
}
.u1 { font-weight: 700; }
.u2 { font-size: 12px; color: #6b7280; }
</style>
