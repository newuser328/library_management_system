import request from '@/utils/request';

export const login = (data) => {
    return request({
        url: '/auth/login',
        method: 'post',
        data,
    });
};

export const register = (data) => {
    return request({
        url: '/auth/register',
        method: 'post',
        data,
    });
};

export const registerAdmin = (data) => {
    return request({
        url: '/auth/register-admin',
        method: 'post',
        data,
    });
};

export const getMe = () => {
    return request({
        url: '/me',
        method: 'get',
    });
};

