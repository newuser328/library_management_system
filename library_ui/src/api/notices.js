import request from '@/utils/request';

export const listPublishedNotices = () => {
  return request({
    url: '/notices/published',
    method: 'get',
  });
};

export const listAdminNotices = () => {
  return request({
    url: '/notices/admin',
    method: 'get',
  });
};

export const createNotice = (data) => {
  return request({
    url: '/notices/admin',
    method: 'post',
    data,
  });
};

export const updateNotice = (id, data) => {
  return request({
    url: `/notices/admin/${id}`,
    method: 'put',
    data,
  });
};

export const deleteNotice = (id) => {
  return request({
    url: `/notices/admin/${id}`,
    method: 'delete',
  });
};
