import request from '@/utils/request';

export const createBorrow = (data) => {
  return request({
    url: '/borrows',
    method: 'post',
    data,
  });
};

export const cancelBorrow = (id) => {
  return request({
    url: `/borrows/${id}/cancel`,
    method: 'post',
  });
};

export const listMyBorrows = (params) => {
  return request({
    url: '/borrows/my',
    method: 'get',
    params,
  });
};

export const listBorrows = (params) => {
  return request({
    url: '/borrows',
    method: 'get',
    params,
  });
};

export const approveBorrow = (id) => {
  return request({
    url: `/borrows/${id}/approve`,
    method: 'post',
  });
};

export const rejectBorrow = (id, remark) => {
  return request({
    url: `/borrows/${id}/reject`,
    method: 'post',
    params: { remark },
  });
};

export const returnBorrow = (id) => {
  return request({
    url: `/borrows/${id}/return`,
    method: 'post',
  });
};
