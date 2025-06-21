import axios from "@/utils/axios-ext";

// 重新获取发送密钥
export function reGetEmailChannelCode() {
    return axios.post('/web/channelCode/getNewChannelCode');
}

export default {
    reGetEmailChannelCode,
};