<template>
  <div>
    <!-- 移动端导航栏 -->
    <div v-if="isMobile" class="mobile-nav">
      <div class="mobile-nav-header">
        <div class="logo">
          <img src="../assets/logo.png" alt="logo"/>
          <span>消息推送平台</span>
        </div>
        <el-icon class="menu-toggle-icon" @click="toggleMenu">
          <Menu v-if="!isMenuVisible"/>
          <Close v-else/>
        </el-icon>
      </div>
      <el-menu v-show="isMenuVisible" mode="vertical" :router="true" class="mobile-menu">
        <el-menu-item index="/email">
          <el-icon><Postcard/></el-icon>
          <span>收发配置</span>
        </el-menu-item>
        <el-menu-item index="/sendRecord">
          <el-icon><Memo/></el-icon>
          <span>投递记录</span>
        </el-menu-item>
        <el-menu-item index="/usage-guide">
          <el-icon><Compass/></el-icon>
          <span>食用指南</span>
        </el-menu-item>
        <el-menu-item v-if="!isLoggedIn" index="/login">
          <el-icon><User/></el-icon>
          <span>未登录</span>
        </el-menu-item>
        <el-sub-menu v-else index="/user">
          <template #title>
            <el-icon><User/></el-icon>
            <span>{{ user.nickname }}</span>
          </template>
          <el-menu-item index="/setting">
            <el-icon><Setting/></el-icon>
            <span>设置</span>
          </el-menu-item>
          <el-menu-item @click="confirmLogout">
            <el-icon><SwitchButton/></el-icon>
            <span>登出</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </div>

    <!-- 桌面端菜单 -->
    <el-menu :router="true" :ellipsis="false" mode="horizontal" v-if="!isMobile">
      <el-menu-item index="/">
        <div class="logo">
          <img src="../assets/logo.png" alt="logo"/>
          <span>消息推送平台</span>
        </div>
      </el-menu-item>
<!--      <el-menu-item index="/">使用指南</el-menu-item>-->
      <el-menu-item index="/email">
        <el-icon><Postcard /></el-icon>
        <span>收发配置</span>
      </el-menu-item>
<!--      <el-menu-item index="/app">APP</el-menu-item>-->
<!--      <el-menu-item index="/multiple">组合投递</el-menu-item>-->
      <el-menu-item index="/sendRecord">
        <el-icon><Memo /></el-icon>
        <span>投递记录</span>
      </el-menu-item>
      <el-menu-item index="/usage-guide">
        <el-icon><Compass /></el-icon>
        <span>食用指南</span>
      </el-menu-item>

      <!-- 用户认证菜单 -->
      <el-menu-item v-if="!isLoggedIn" index="/login">
        <el-icon><User /></el-icon>
        <span>未登录</span>
      </el-menu-item>

      <!-- 用户已登录菜单 -->
      <el-sub-menu v-else index="/user" popper-class="user-popper">
        <template #title>
          <el-icon><User /></el-icon>
          <span>{{ user.nickname }}</span>
        </template>
        <el-menu-item index="/setting">
          <el-icon><Setting /></el-icon>
          <span>设置</span>
        </el-menu-item>
        <el-menu-item @click="confirmLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>登出</span>
        </el-menu-item>
      </el-sub-menu>
    </el-menu>
  </div>
</template>

<script setup>
import {useUserStore} from '@/store/userStore';
import {storeToRefs} from 'pinia';
import {ElMessage, ElMessageBox} from "element-plus";
import {useRouter, useRoute} from 'vue-router'; // 导入 useRoute
import cookie from 'js-cookie';
import { logout } from "@/api/authn";
import {ref, watch} from 'vue';
import { Postcard, Memo, User, Setting, SwitchButton, Menu, Close, Compass } from '@element-plus/icons-vue'

const userStore = useUserStore();
const {isLoggedIn, user} = storeToRefs(userStore);
const router = useRouter();
const route = useRoute(); // 使用 useRoute 钩子

let isMenuVisible = ref(false);
let isMobile = ref(window.innerWidth <= 768);

const toggleMenu = () => {
  isMenuVisible.value = !isMenuVisible.value;
};

// 监听路由变化以隐藏菜单
watch(route, () => {
  if (isMobile.value) {
    isMenuVisible.value = false; // 当路由发生变化时，如果是在移动设备上，则隐藏菜单
  }
});

const confirmLogout = () => {
  ElMessageBox.confirm(
      '您确定要登出吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    try {
      await logout();
      ElMessage({
        message: '登出成功',
        type: 'success'
      });
      cookie.remove('JSESSIONID');
      cookie.remove('SESSION');
      router.push('/');
      userStore.logout();
    } catch (error) {
      ElMessage.error(error.message);
    }
  }).catch(() => {
        // ElMessage({
        //   type: 'info',
        //   message: 'Delete canceled',
        // })
      });
};

// 监听窗口大小变化，更新isMobile的值
window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth <= 768;
});
</script>
<style scoped>
.el-menu--horizontal > .el-menu-item:nth-child(1) {
  margin-right: auto;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  text-decoration: none; /* 确保无下划线 */
  color: inherit; /* 继承父级颜色 */
}

.logo img {
  height: 32px;
  margin-right: 10px;
}

.el-menu-item {
  font-size: 16px;
}

/* 移动端菜单样式调整 */
.mobile-nav {
  position: relative;
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
}
.mobile-nav-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}
.menu-toggle-icon {
  font-size: 24px;
  cursor: pointer;
}
.mobile-menu {
  border-right: none;
}
</style>