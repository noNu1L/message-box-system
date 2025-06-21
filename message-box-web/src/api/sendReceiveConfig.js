import axios from "@/utils/axios-ext";

// 获取发送配置
export function getSendConfig() {
    return axios.post('/web/sendReceiveConfig/getSendConfig');
}

// 获取接收配置
export function getReceiveConfig() {
    return axios.post('/web/sendReceiveConfig/getReceiveConfig');
}

// 获取邮件服务器
export function getSmtpServer() {
    return axios.post('/web/sendReceiveConfig/getSmtpServer');
}

// 更新发件配置
export function updateSendConfig(formData) {
    return axios.post('/web/sendReceiveConfig/updateSendConfig', formData);
}

// 测试发件配置
export function testSendConfig(formData) {
    return axios.post('/web/sendReceiveConfig/testSendConfig', formData);
}

// 测试接收配置
export function testReceiveConfig(formData) {
    return axios.post('/send/ms', formData);
}

// 更新接收配置
export function updateReceiveConfig(formData) {
    return axios.post('/web/sendReceiveConfig/updateReceiveConfig', formData);
}

// 删除接收配置
export function deleteReceiveConfig(formData) {
    return axios.post('/web/sendReceiveConfig/deleteReceiveConfig', formData);
} 