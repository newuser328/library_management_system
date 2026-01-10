import axios from 'axios';
import { useAuthStore } from '@/store/auth';

// 上传文件（管理员）
// 说明：上传接口返回的是后端 ApiResponse 包裹结构；这里直接解包并返回 data
export const uploadFile = (file) => {
  const formData = new FormData();
  formData.append('file', file);

  const authStore = useAuthStore();
  const headers = {
    'Content-Type': 'multipart/form-data',
  };

  // 手动补 Authorization，避免出现 401 未登录
  if (authStore.token) {
    headers['Authorization'] = `Bearer ${authStore.token}`;
  }

  return axios({
    url: 'http://localhost:8080/api/files/upload',
    method: 'post',
    data: formData,
    headers,
    timeout: 10000,
  }).then((response) => {
    const res = response.data;
    if (res.code !== 0) {
      return Promise.reject(new Error(res.message || 'Error'));
    }
    // res.data 形如：{ url: "/api/files/images/xxx.jpg", filename: "xxx.jpg" }
    return res.data;
  });
};
