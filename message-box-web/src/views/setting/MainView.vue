<template>
  <el-card class="unified-settings-card">
    <template #header>
      <div class="card-header">
        <span><el-icon><Setting /></el-icon>设置</span>
      </div>
    </template>
    <el-container>
      <!-- 左侧菜单 -->
      <el-aside width="200px" class="settings-aside">
        <el-menu
            :default-active="$route.path"
            class="settings-menu"
            router
        >
          <el-menu-item index="/setting/user">
            <el-icon><User /></el-icon>
            <span>基础设置</span>
          </el-menu-item>
          <el-menu-item index="/setting/management">
            <el-icon><UserFilled /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧内容区 -->
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-card>
</template>

<script setup>
import { Setting, UserFilled, User } from '@element-plus/icons-vue';
// 移除了所有业务逻辑，该组件现在是纯布局容器
</script>

<style scoped>
.unified-settings-card {
  margin: 20px;
  min-height: calc(100vh - 120px);
}

.settings-aside {
  border-right: 1px solid #e6e6e6;
}

.settings-menu {
  border-right: none;
}

.card-header {
  display: flex;
  align-items: center;
  font-weight: bold;
  font-size: 1.2em;
}
.card-header .el-icon {
  margin-right: 8px;
}
</style> 