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

export const sendSmsCode = (phone) => {
    return request({
        url: '/auth/sms/send',
        method: 'post',
        data: { phone },
    });
};

export const smsLogin = (phone, code) => {
    return request({
        url: '/auth/sms/login',
        method: 'post',
        data: { phone, code },
    });
};

export const setPassword = (password) => {
    return request({
        url: '/me/set-password',
        method: 'post',
        data: { password },
    });
};
