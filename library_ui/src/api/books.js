import request from '@/utils/request';

export const listBooks = (params) => {
  return request({
    url: '/books',
    method: 'get',
    params,
  });
};

export const getBook = (id) => {
  return request({
    url: `/books/${id}`,
    method: 'get',
  });
};

export const createBook = (data) => {
  return request({
    url: '/books',
    method: 'post',
    data,
  });
};

export const updateBook = (id, data) => {
  return request({
    url: `/books/${id}`,
    method: 'put',
    data,
  });
};

export const deleteBook = (id) => {
  return request({
    url: `/books/${id}`,
    method: 'delete',
  });
};

