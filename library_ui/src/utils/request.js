import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/store/auth';

const service = axios.create({
    baseURL: 'http://localhost:8080/api',
    timeout: 10000,
});

// 请求拦截器
service.interceptors.request.use(
    config => {
        const authStore = useAuthStore();
        if (authStore.token) {
            config.headers['Authorization'] = `Bearer ${authStore.token}`;
        }
        return config;
    },
    error => {
        console.error('Request error:', error);
        return Promise.reject(error);
    }
);

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data;
        if (res.code !== 0) {
            ElMessage.error(res.message || 'Error');
            return Promise.reject(new Error(res.message || 'Error'));
        }
        return res.data;
    },
    error => {
        console.error('Response error:', error);
        ElMessage.error(error.response?.data?.message || error.message || 'Network Error');
        return Promise.reject(error);
    }
);

export default service;

