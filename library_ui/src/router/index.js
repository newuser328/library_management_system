import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';

NProgress.configure({ showSpinner: false, trickleSpeed: 120 });

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { public: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    redirect: '/reader/books',
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      { path: 'books', name: 'AdminBooks', component: () => import('@/views/admin/BookManage.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/UserManage.vue') },
      { path: 'borrows', name: 'AdminBorrows', component: () => import('@/views/admin/BorrowManage.vue') },
      { path: 'admin-token', name: 'AdminToken', component: () => import('@/views/admin/AdminToken.vue') },
    ],
  },
  {
    path: '/reader',
    component: () => import('@/layouts/ReaderLayout.vue'),
    meta: { requiresAuth: true, roles: ['READER', 'ADMIN'] },
    children: [
      { path: 'books', name: 'ReaderBooks', component: () => import('@/views/reader/BookList.vue') },
      { path: 'my-borrows', name: 'MyBorrows', component: () => import('@/views/reader/MyBorrows.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/reader/Profile.vue') },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  NProgress.start();

  const authStore = useAuthStore();

  if (to.meta.public) {
    return next();
  }

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return next('/login');
  }

  if (to.meta.roles && authStore.role && !to.meta.roles.includes(authStore.role)) {
    return next('/login');
  }

  next();
});

router.afterEach(() => {
  NProgress.done();
});

router.onError(() => {
  NProgress.done();
});

export default router;
