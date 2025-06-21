import axios from "@/utils/axios-ext";

// 登录
export function login(data) {
    return axios.post('web/user/login', data);
}

// 登出
export function logout() {
    return axios.post('web/logout');
}