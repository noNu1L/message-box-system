import axios from "@/utils/axios-ext";

// 拉取发送记录
export function pullSendRecord() {
    return axios.post('/web/sendRecord/pull');
}

export default {
    pullSendRecord,
};