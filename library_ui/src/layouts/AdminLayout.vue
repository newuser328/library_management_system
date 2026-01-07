<template>
  <el-container class="layout">
    <el-aside width="240px" class="aside">
      <div class="brand">
        <div class="brand-logo">L</div>
        <div class="brand-text">
          <div class="brand-title">图书馆系统</div>
          <div class="brand-sub">管理端</div>
        </div>
      </div>

      <el-menu :default-active="active" router class="menu">
        <el-menu-item index="/admin/books">图书管理</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
        <el-menu-item index="/admin/borrows">借阅管理</el-menu-item>
        <el-menu-item index="/admin/admin-token">管理员口令</el-menu-item>
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
          <div class="muted">欢迎回来，祝你工作顺利</div>
        </div>
        <div class="header-right">
          <el-button type="primary" plain @click="goReader">切到读者端</el-button>
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
  if (route.path.includes('/admin/books')) return '图书管理';
  if (route.path.includes('/admin/users')) return '用户管理';
  if (route.path.includes('/admin/borrows')) return '借阅管理';
  if (route.path.includes('/admin/admin-token')) return '管理员口令';
  return '管理端';
});

const onLogout = () => {
  authStore.logout();
  router.push('/login');
};

const goReader = () => {
  router.push('/reader/books');
};
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: transparent;
}
.aside {
  background: linear-gradient(180deg, rgba(255,255,255,0.92), rgba(255,255,255,0.82));
  border-right: 1px solid rgba(229,231,235,0.9);
  backdrop-filter: blur(10px);
}
.brand {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
}
.brand-logo {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  color: white;
  background: linear-gradient(135deg, #4f46e5, #06b6d4);
  box-shadow: 0 10px 30px rgba(79,70,229,0.25);
}
.brand-title { font-weight: 900; line-height: 1.1; }
.brand-sub { font-size: 12px; color: #6b7280; margin-top: 2px; }

.menu { border-right: none; padding: 8px; background: transparent; }

.header {
  background: rgba(255,255,255,0.75);
  border-bottom: 1px solid rgba(229,231,250,0.9);
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
  background: #22c55e;
  box-shadow: 0 0 0 6px rgba(34,197,94,0.15);
}
.u1 { font-weight: 700; }
.u2 { font-size: 12px; color: #6b7280; }
</style>
