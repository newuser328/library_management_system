import { defineStore } from 'pinia';

const TOKEN_KEY = 'token';
const USER_KEY = 'user';
const NEED_SET_PASSWORD_KEY = 'needSetPassword';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    // 使用 sessionStorage：同一 tab 刷新不丢；新开 tab 不共享
    token: sessionStorage.getItem(TOKEN_KEY) || '',
    user: (() => {
      const raw = sessionStorage.getItem(USER_KEY);
      try {
        return raw ? JSON.parse(raw) : null;
      } catch {
        return null;
      }
    })(),
    needSetPassword: sessionStorage.getItem(NEED_SET_PASSWORD_KEY) === 'true',
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    role: (state) => state.user?.role,
  },
  actions: {
    setToken(token) {
      this.token = token;
      if (token) {
        sessionStorage.setItem(TOKEN_KEY, token);
      } else {
        sessionStorage.removeItem(TOKEN_KEY);
      }
    },
    setUser(user) {
      this.user = user;
      if (user) {
        sessionStorage.setItem(USER_KEY, JSON.stringify(user));
      } else {
        sessionStorage.removeItem(USER_KEY);
      }
    },
    setNeedSetPassword(needSetPassword) {
      this.needSetPassword = needSetPassword;
      if (needSetPassword) {
        sessionStorage.setItem(NEED_SET_PASSWORD_KEY, 'true');
      } else {
        sessionStorage.removeItem(NEED_SET_PASSWORD_KEY);
      }
    },
    logout() {
      this.token = '';
      this.user = null;
      this.needSetPassword = false;
      sessionStorage.removeItem(TOKEN_KEY);
      sessionStorage.removeItem(USER_KEY);
      sessionStorage.removeItem(NEED_SET_PASSWORD_KEY);
    },
  },
});
