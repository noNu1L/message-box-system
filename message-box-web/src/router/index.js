import {createRouter, createWebHistory} from 'vue-router'
import {useUserStore} from '@/store/userStore';
import dialog from '@/utils/dialog';

import EmailConfigView from '@/views/sendReceiveConfig/MainView.vue'
import SendRecordView from '../views/sendRecord/MainView.vue'
import LoginView from '../views/LoginView.vue'
import Index from '../views/IndexView.vue'
import SettingMainView from '../views/setting/MainView.vue'
import UserSettingView from '../views/setting/UserSetting.vue'
import UserManagementView from '../views/setting/UserManagement.vue'
import UsageGuideView from '../views/usageGuide/MainView.vue'


const routes = [
    {
        path: '/',
        name: 'Index',
        component: Index,
        meta: { title: '首页' }
    },
    {
        path: '/login',
        name: 'LoginView',
        component: LoginView,
        meta: { title: '登录' }
    },
    {
        path: '/sendRecord',
        name: 'SendRecordView',
        component: SendRecordView,
        meta: { requiresAuth: true, title: '投递记录' }
    },
    {
        path: '/email',
        name: 'EmailConfigView',
        component: EmailConfigView,
        meta: { requiresAuth: true, title: '收发配置' }
    },
    {
        path: '/usage-guide',
        name: 'UsageGuideView',
        component: UsageGuideView,
        meta: { requiresAuth: true, title: '食用指南' }
    },
    {
        path: '/setting',
        name: 'Setting',
        component: SettingMainView,
        meta: { requiresAuth: true, title: '设置' },
        redirect: '/setting/user',
        children: [
            {
                path: 'user',
                name: 'UserSetting',
                component: UserSettingView,
                meta: { title: '基础设置' }
            },
            {
                path: 'management',
                name: 'UserManagement',
                component: UserManagementView,
                meta: { title: '用户管理' }
            }
        ]
    },
]

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes
});

let isLoginTipShowing = false;

router.beforeEach((to, from, next) => {
    const userStore = useUserStore();
    const isLoggedIn = userStore.isLoggedIn;

    if (to.meta.requiresAuth && !isLoggedIn) {
        sessionStorage.setItem('lastPath', to.fullPath);

        if (!isLoginTipShowing) {
            isLoginTipShowing = true;
            dialog.alter("提示", "您还未登录或登录已过期，请重新登录。", () => {
                next({ path: '/login' });
                isLoginTipShowing = false;
            }, () => {
                next({ path: '/login' });
                isLoginTipShowing = false;
            });
        }
    } else {
        next();
    }
});

router.afterEach((to) => {
    const baseTitle = "消息推送平台";
    if (to.meta.title) {
        document.title = `${to.meta.title} | ${baseTitle}`;
    } else {
        document.title = baseTitle;
    }
});

export default router;