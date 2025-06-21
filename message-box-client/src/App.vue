<script setup>
import {ref, onMounted, watch, onUnmounted} from 'vue';
import { invoke } from '@tauri-apps/api/core';
import {Cog, BotMessageSquare, Plug, Unplug} from 'lucide-vue-next';
import {isPermissionGranted, requestPermission, sendNotification} from '@tauri-apps/plugin-notification';
import {loadSettings, saveSettings, saveData, loadTodayData, clearAllData} from './services/storage';
import { log } from './services/logger';
import SettingsPanel from './components/SettingsPanel.vue';
import LogPanel from "./components/LogPanel.vue";

const serverAddress = ref('');
const channelCodes = ref('');
const enableNotifications = ref(true);
const enableSound = ref(true);
const autoConnect = ref(false);
const isConnected = ref(false);
const isSettingsOpen = ref(false);
const isLogPanelOpen = ref(false);
const history = ref([]);
const selectedMessage = ref(null);
const machineId = ref('');

let ws = null;
let heartbeatInterval = null;
let reconnectTimeout = null;

const connect = () => {
  if (ws && ws.readyState === WebSocket.OPEN) {
    const msg = 'WebSocket is already connected.';
    console.log(msg);
    log(msg);
    return;
  }

  const codes = channelCodes.value.split('\n').map(c => c.trim()).filter(Boolean).join(',');
  if (!codes) {
    const msg = '请在设置中提供至少一个通道代码';
    alert(msg);
    log(`连接失败: ${msg}`);
    return;
  }

  let baseUrl;
  try {
    const url = new URL(serverAddress.value);
    baseUrl = `${url.protocol}//${url.host}`.replace(/^http/, 'ws');
  } catch (error) {
    const msg = '无效的服务器地址格式，请在设置中更正';
    alert(msg);
    log(`连接失败: ${msg}. 错误: ${error.message}`);
    return;
  }

  const fullWsUrl = `${baseUrl}/ws/client`;
  const connectMsg = `正在连接到 ${fullWsUrl}`;
  console.log(connectMsg);
  log(connectMsg);

  ws = new WebSocket(fullWsUrl);

  ws.onopen = () => {
    const openMsg = 'WebSocket 已连接.';
    console.log(openMsg);
    log(openMsg);

    // 发送注册消息，包含通道代码和机器ID
    const registrationMessage = {
      type: 'register',
      machineId: machineId.value,
      channelCodes: channelCodes.value.split('\n').map(c => c.trim()).filter(Boolean)
    };
    ws.send(JSON.stringify(registrationMessage));
    const regMsg = `发送注册消息: ${JSON.stringify(registrationMessage)}`;
    console.log('Sent registration message:', registrationMessage);
    log(regMsg);

    isConnected.value = true;
    startHeartbeat();
    clearTimeout(reconnectTimeout);
  };

  async function handleMessage(data) {
    const newMessage = {
      id: data.id,
      code: data.channelCode,
      content: data.body,
      title: data.title,
      time: new Date().toLocaleString()
    };
    history.value.unshift(newMessage);
    if (!selectedMessage.value) {
      selectedMessage.value = newMessage;
    }

    if (enableNotifications.value) {
      let permissionGranted = await isPermissionGranted();
      if (!permissionGranted) {
        const permission = await requestPermission();
        permissionGranted = permission === 'granted';
      }
      if (permissionGranted) {
        // 系统通知
        sendNotification({title: newMessage.title, body: newMessage.content});
      }
    }

    // 播放声音
    if (enableSound.value) {
      const audio = new Audio('/notification.mp3');
      audio.play().catch(e => console.error("Error playing sound:", e));
    }

    // 保存数据
    await saveData(newMessage);
  }


  ws.onmessage = async (event) => {
    log(`接收到原始数据: ${event.data}`);
    try {
      const data = JSON.parse(event.data);
      console.log('Received data:', data);

      if (data.type === 'pong') {
          const pongMsg = '接收到心跳 Pong';
          console.log('心跳检测', Date.now());
          log(pongMsg);
          return;
      }

      if (data.title && data.body && data.channelCode && data.id) {
          const bizMsg = `已处理业务消息: ${data.title}`;
          console.log('接收到业务消息', data.title);
          log(bizMsg);
          await handleMessage(data);
      } else if (data.type === "message") {
          const oldMsg = `已处理旧格式消息: ${data.message.title}`;
          console.log('接收到旧格式消息', data.message);
          log(oldMsg);
          await handleMessage(data.message);
      } else {
          const unknownMsg = `无法识别的消息类型或格式: ${JSON.stringify(data)}`;
          console.log('无法识别的消息类型或格式:', data);
          log(unknownMsg);
      }

    } catch (error) {
      const errorMsg = `解析消息或显示通知时出错: ${error.message}`;
      console.error('Error parsing message or showing notification:', error);
      log(errorMsg);
    }
  };

  ws.onclose = () => {
    const closeMsg = 'WebSocket 连接已断开.';
    console.log(closeMsg);
    log(closeMsg);
    isConnected.value = false;
    stopHeartbeat();
    reconnectTimeout = setTimeout(connect, 5000);
  };

  ws.onerror = (error) => {
    const errorMsg = `WebSocket 错误. 查看控制台了解详情.`;
    console.error('WebSocket error:', error);
    log(errorMsg);
    ws.close();
  };
};

const disconnect = () => {
  if (ws) {
    clearTimeout(reconnectTimeout);
    ws.close();
    ws = null;
  }
  stopHeartbeat();
  isConnected.value = false;
};

const startHeartbeat = () => {
  stopHeartbeat();
  heartbeatInterval = setInterval(() => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({
        type: 'ping',
        machineId: machineId.value
      }));
      const hbMsg = `发送心跳 Ping, machineId: ${machineId.value}`;
      console.log('Sent heartbeat', machineId.value);
      log(hbMsg);
    }
  }, 30000);
};

const stopHeartbeat = () => {
  clearInterval(heartbeatInterval);
  heartbeatInterval = null;
};

const toggleConnection = () => {
  if (isConnected.value) {
    disconnect();
  } else {
    connect();
  }
};

const handleSaveSettings = async (newSettings) => {
  const oldBaseUrl = (() => {
    try {
      return new URL(serverAddress.value).origin;
    } catch {
      return '';
    }
  })();
  const oldCodes = channelCodes.value;

  serverAddress.value = newSettings.serverAddress;
  channelCodes.value = newSettings.channelCodes;
  enableNotifications.value = newSettings.enableNotifications;
  enableSound.value = newSettings.enableSound;
  autoConnect.value = newSettings.autoConnect;
  isSettingsOpen.value = false;

  await saveSettings({
    serverAddress: serverAddress.value,
    channelCodes: channelCodes.value,
    enableNotifications: enableNotifications.value,
    enableSound: enableSound.value,
    autoConnect: autoConnect.value
  });
  const saveMsg = `设置已保存: ${JSON.stringify(newSettings)}`;
  console.log('Settings saved:', newSettings);
  log(saveMsg);

  const newBaseUrl = (() => {
    try {
      return new URL(serverAddress.value).origin;
    } catch {
      return '';
    }
  })();

  // 如果连接处于活动状态且基础URL或代码已更改，则重新连接
  if (isConnected.value && (oldBaseUrl !== newBaseUrl || oldCodes !== channelCodes.value)) {
    console.log('Settings changed, reconnecting WebSocket...');
    disconnect();
    connect();
  }
};

const clearHistory = async () => {
  // 使用浏览器的 confirm 对话框进行确认
  if (window.confirm('您确定要清除所有历史消息吗？此操作将删除所有本地存储的数据文件，且不可恢复。')) {
    history.value = [];
    selectedMessage.value = null;
    await clearAllData();
    const clearMsg = '所有历史记录和数据文件已清除。';
    console.log('Message history and all data files cleared.');
    log(clearMsg);
    alert('所有历史记录和数据文件已清除。');
  }
};

onMounted(async () => {
  // 获取机器唯一标识码
  try {
    machineId.value = await invoke('get_machine_id');
    console.log('Machine ID:', machineId.value);
    log(`获取到本机唯一标识: ${machineId.value}`);
  } catch (e) {
    console.error('Failed to get machine ID:', e);
    log(`获取本机唯一标识失败: ${e}`);
  }

  // Load settings
  const settings = await loadSettings();
  if (settings) {
    serverAddress.value = settings.serverAddress || 'ws://127.0.0.1:8675';
    channelCodes.value = settings.channelCodes || '123';
    enableNotifications.value = settings.enableNotifications ?? true;
    enableSound.value = settings.enableSound ?? true;
    autoConnect.value = settings.autoConnect ?? false;
  } else {
    // 如果没有设置，使用默认值
    serverAddress.value = 'ws://127.0.0.1:8675';
    channelCodes.value = '123';
    enableNotifications.value = true;
    enableSound.value = true;
    autoConnect.value = false;
  }

  // Load today's message history
  const todayHistory = await loadTodayData();
  if (todayHistory && todayHistory.length > 0) {
    history.value = todayHistory;
    selectedMessage.value = todayHistory[0];
  }
  
  // 自动连接
  if (autoConnect.value) {
    connect();
  }
});

onUnmounted(() => {
  disconnect();
});

const selectMessage = (message) => {
  selectedMessage.value = message;
};
</script>

<template>
  <div class="app-container">
    <transition name="slide-left-fade">
      <LogPanel v-if="isLogPanelOpen" @close="isLogPanelOpen = false"/>
    </transition>
    <div class="main-interface" :class="{ 'panel-open': isSettingsOpen, 'log-panel-open': isLogPanelOpen }">
      <header class="header">
        <div class="header-left">
          <BotMessageSquare :size="24" class="app-icon"/>
          <span class="app-name">Message Box Client</span>
        </div>
        <div class="header-center">
          <button @click="toggleConnection" class="connect-btn" :class="{ connected: isConnected }"
                  :title="isConnected ? '断开连接' : '建立连接'">
            <Unplug v-if="!isConnected" :size="14"/>
            <Plug v-else :size="14"/>
            <span>{{ isConnected ? '已连接' : '连接' }}</span>
          </button>
          <span>{{ serverAddress }}</span>
        </div>
        <div class="header-right">
          <Cog :size="20" class="settings-icon" @click="isSettingsOpen = true"/>
        </div>
      </header>

      <main class="main-content">
        <aside class="history-panel">
          <div class="history-list">
            <div
                v-for="item in history"
                :key="item.id"
                class="history-item"
                :class="{ active: selectedMessage && selectedMessage.id === item.id }"
                @click="selectMessage(item)"
            >
              <div class="item-title">{{ item.title }}</div>
              <div class="item-meta">
                <span>{{ item.code }}</span>
                <span>{{ item.time }}</span>
              </div>
            </div>
          </div>
        </aside>

        <section class="message-panel">
          <div v-if="selectedMessage" class="message-detail">
            <h1 class="message-title">{{ selectedMessage.title }}</h1>
            <div class="message-meta">
              <span>通道代码: {{ selectedMessage.code }}</span>
              <span>时间: {{ selectedMessage.time }}</span>
            </div>
            <div class="message-body" v-html="selectedMessage.content"></div>
          </div>
          <div v-else class="no-message-selected">
            <p>请从左侧选择一条消息查看</p>
          </div>
        </section>
      </main>
    </div>
    <transition name="slide-fade">
      <SettingsPanel
          v-if="isSettingsOpen"
          :server-address="serverAddress"
          :channel-codes="channelCodes"
          :enable-notifications="enableNotifications"
          :enable-sound="enableSound"
          :auto-connect="autoConnect"
          :machine-id="machineId"
          @close="isSettingsOpen = false"
          @save="handleSaveSettings"
          @clear-history="clearHistory"
          @open-log-panel="isLogPanelOpen = true"
      />
    </transition>
  </div>
</template>

<style>
body {
  margin: 0;
  padding: 0;
}
</style>

<style scoped>
.app-container {
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100vh;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Helvetica Neue', sans-serif;
  background-color: #f5f5f7;
  color: #333;
}

.main-interface {
  transition: transform 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  width: 100%;
  display: flex;
  flex-direction: column;
  flex-grow: 1;
  min-height: 0;
}

/* Slide-in/out animation for settings panel */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: transform 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(100%);
}

.slide-fade-enter-to,
.slide-fade-leave-from {
  transform: translateX(0);
}

/* 头部样式 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 50px;
  background-color: #eef0f4;
  border-bottom: 1px solid #dcdfe6;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.app-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-center {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.connect-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background-color: #fef2f2; /* 淡红色 */
  color: #dc2626; /* 红色文字 */
  border: none;
  padding: 5px 10px;
  cursor: pointer;
  border-radius: 5px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.connect-btn.connected {
  background-color: #f0fdf4; /* 淡绿色 */
  color: #22c55e; /* 绿色文字 */
}

.connect-btn:hover {
  opacity: 0.9;
}

.header-right .settings-icon {
  cursor: pointer;
  color: #606266;
  transition: color 0.3s ease;
}

.header-right .settings-icon:hover {
  color: #303133;
}

/* 主内容区 */
.main-content {
  display: flex;
  flex-grow: 1;
  overflow: hidden; /* 防止子元素溢出 */
  min-height: 0; /* 修复flex布局下子元素overflow不生效的问题 */
}

/* 左侧历史记录 */
.history-panel {
  width: 320px;
  flex-shrink: 0;
  background-color: #f2f3f5;
  border-right: 1px solid #dcdfe6;
  overflow-y: auto;
}

.history-item {
  padding: 12px 15px;
  border-bottom: 1px solid #e4e7ed;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.history-item:hover {
  background-color: #e9eaf0;
}

.history-item.active {
  background-color: #e0e6f0;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

/* 右侧消息详情 */
.message-panel {
  flex-grow: 1;
  padding: 25px;
  overflow-y: auto;
  background-color: #fff;
}

.message-detail .message-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 15px;
  color: #333;
}

.message-meta {
  font-size: 13px;
  color: #909399;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 20px;
}

.message-meta span {
  margin-right: 20px;
}

.message-body {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
}

.no-message-selected {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #909399;
}

.slide-left-fade-enter-active,
.slide-left-fade-leave-active {
  transition: transform 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.slide-left-fade-enter-from,
.slide-left-fade-leave-to {
  transform: translateX(-100%);
}
</style>
