<template>
  <div id="app">
    <menu-component class="fixed-header"></menu-component>
    <el-scrollbar class="main-content">
      <el-row justify="center">
        <el-col :xs="24" :sm="22" :md="20" :lg="18" :xl="16" class="content-col">
          <router-view/> <!-- 动态渲染路由组件 -->
        </el-col>
      </el-row>
    </el-scrollbar>
  </div>
</template>

<script>

import MenuComponent from "@/components/MenuComponent.vue";

export default {
  name: 'App',
  /* eslint-disable vue/multi-word-component-names */
  components: {
    MenuComponent
  }
}
</script>

<style>
/* 你可以在这里添加全局样式 */
body {
  margin: 0;
  font-family: "Helvetica Neue",Helvetica,"PingFang SC","Hiragino Sans GB","Microsoft YaHei","微软雅黑",Arial,sans-serif;
  background: linear-gradient(to bottom right, #f7f8fa, #eef1f5);
  min-height: 100vh;
}

#app {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  height: 100vh;
  overflow-y: auto;
  margin-top: 60px;
}

.content-col {
  padding: 0 20px 20px 20px;
}


/* 导航栏固定在顶部 */
.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000; /* 确保它位于其他内容之上 */
  background-color: rgba(255, 255, 255, 0.85); /* 设置背景颜色以覆盖下面的内容 */
  backdrop-filter: saturate(50%) blur(8px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, .12), 0 0 6px rgba(0, 0, 0, .04); /* 添加阴影效果 */
  height: 60px; /* 根据你的设计设置高度 */
}


/* 在全局样式文件中 */
/* 覆盖 el-select 默认样式 */
el-select :deep(.el-select__wrapper) {
  /* 自定义样式 */
  border-radius: 0;
  border: 0;
}

/* 覆盖下拉框选项样式 */
.el-select-dropdown__item {
  /* 自定义样式 */
}
</style>