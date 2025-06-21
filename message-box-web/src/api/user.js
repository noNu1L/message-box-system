import axios from "@/utils/axios-ext";

/**
 * 更新用户信息（昵称或密码）
 * @param {object} userData - 包含要更新的用户信息
 * @returns {Promise}
 */
export function updateUser(userData) {
    return axios.post('web/user/update', userData);
} 