import request from '@/utils/request';

// 上传文件
// 说明：使用项目统一的 request 实例，它会自动处理 baseURL 和错误
export const uploadFile = (file) => {
  const formData = new FormData();
  formData.append('file', file);

  return request({
    url: '/files/upload',
    method: 'post',
    data: formData,
    // `Content-Type` 会由浏览器根据 formData 自动设置，无需手动指定
  });
};
