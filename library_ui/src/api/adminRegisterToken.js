import request from '@/utils/request';

export const generateAdminRegisterToken = () => {
    return request({
        url: '/admin-register-tokens',
        method: 'post',
    });
};

