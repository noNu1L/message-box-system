/* eslint-disable no-unused-vars */
import axios from 'axios';
import dialog from '@/utils/dialog' ;
import router from '@/router/index';
import {ElMessage} from "element-plus"; // 确保正确导入了路由实例
import {useUserStore} from '@/store/userStore';



let isShowLoginTip = false;

const axiosExt = axios.create({
    // baseURL: process.env.VUE_APP_BASE_API, // 根据环境变量设置基础 URL
    timeout: 10000, // 请求超时时间，适当加长
    withCredentials: true // 确保跨域请求时携带 Cookie
});

/// 请求拦截器
axiosExt.interceptors.request.use(
    config => {
        // 在发送请求之前做些什么，比如添加 token 到 headers 中
        // 当前使用cookie - session 方式，暂不需要 Token
        // 如需 token，可解开注释
        // const token = localStorage.getItem('userToken');
        // if (token) {
        //     config.headers['Authorization'] = `Bearer ${token}`;
        // }
        return config;
    },
    error => {
        // 对请求错误做些什么
        return Promise.reject(error);
    }
);


/**
 * 数据处理 暂分三类
 * 1、【服务器正常响应 ，数据正常返回】
 * 2、【服务器异常响应：如401 403 500】
 * 3、【服务器正常响应 ，数据异常返回：如自定义异常返回】
 *
 */


/////////////////////////////
let errorResponse = {
    success: false,
    message: '服务器内部错误，请稍后再试。',
    data: null,
    status: 500
}

// 统一错误提示函数
function showError(msg) {
    ElMessage({
        message: msg || '请求失败',
        type: 'error',
        showClose: true,
        duration: 3000
    });
}

/// 响应拦截器
axiosExt.interceptors.response.use(
    response => {
        // 兼容后端自定义返回结构
        console.log("response",response)
        console.log("response-data",response.data)
        const res = response.data;


        // 1. 处理后端自定义异常（如 code/msg）
        if (res && typeof res.code !== 'undefined' && res.code !== 0) {
            showError(res.msg || '请求失败');
            return Promise.reject(res);
        }

        // 2. 兼容旧的 status 1005 逻辑
        if (res && res.status === 1005) {
            showError(res.message || '请求失败');
            return Promise.reject(res);
        }
        return res;
    },
    error => {
        const {response} = error;
        const userStore = useUserStore();
        if (response) {
            switch (response.status) {
                case 401: {
                    userStore.logout();
                    if (isShowLoginTip){
                        return Promise.reject(response.data) ;
                    }
                    isShowLoginTip = true ;
                    dialog.alter("提示", "您还未登录或登录已过期，请重新登录。", () => {
                        router.push({path: '/login'})
                            .then(() => {
                                isShowLoginTip = false ;
                            });
                    }, () => {
                        router.push({path: '/login'})
                            .then(() => {
                                isShowLoginTip = false ;
                            });
                    })
                    return Promise.reject(response.data) ;
                }
                case 403: {
                    showError('无权访问');
                    router.push('/index');
                    return Promise.reject(response.data)
                }

                case 500: {
                    showError((response.data && response.data.message) || '服务器内部错误，请稍后再试。');
                    return Promise.reject(response.data || { message: '服务器内部错误，请稍后再试。' });
                }

                default: {
                    showError((response.data && response.data.message) || `请求错误(${response.status})`);
                    return Promise.reject(response.data);
                }
            }
        } else {
            // 网络异常、超时等
            showError(error.message || '网络异常');
            return Promise.reject(error);
        }
    }
);

export default axiosExt;