<template>
  <div class="tray-menu">
    <div class="menu-item">
      <input type="checkbox" v-model="connected" id="connected" />
      <label for="connected">{{ connected ? '已连接' : '未连接' }}</label>
      <span class="server-info">服务器: 100.86.235.37</span>
      <span class="code">通道代码: 123456</span>
    </div>
    <div class="menu-item">
      <input type="checkbox" v-model="notified" id="notified" />
      <label for="notified">{{ notified ? '通知' : '未通知' }}</label>
    </div>
    <div class="divider"></div>
    <div class="menu-item clickable">历史消息</div>
    <div class="menu-item clickable">推送</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const connected = ref(true)
const notified = ref(true)
</script>

<style scoped>
.tray-menu {
  width: 280px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  padding: 16px 0;
  font-size: 15px;
}
.menu-item {
  display: flex;
  align-items: center;
  padding: 8px 24px;
}
.menu-item label {
  margin-left: 6px;
  margin-right: 12px;
}
.menu-item .server-info {
  color: #888;
  font-size: 13px;
  margin-right: 10px;
}
.menu-item .code {
  color: #aaa;
  font-size: 13px;
}
.menu-item.clickable {
  cursor: pointer;
  color: #409eff;
  transition: background 0.2s;
}
.menu-item.clickable:hover {
  background: #f5f7fa;
}
.divider {
  height: 1px;
  background: #eee;
  margin: 8px 0;
}
</style> 