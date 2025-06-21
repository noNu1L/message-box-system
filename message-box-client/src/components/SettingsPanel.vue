<script setup>
import { ref, onMounted } from 'vue';
import { defineProps, defineEmits } from 'vue';
import { X } from 'lucide-vue-next';
import TagInput from './TagInput.vue';

// 定义props，接收来自父组件的设置项
const props = defineProps({
  serverAddress: String,
  channelCodes: String,
  enableNotifications: Boolean,
  enableSound: Boolean,
  autoConnect: Boolean,
  machineId: String,
});

// 定义emits，用于向父组件发送事件
const emit = defineEmits(['close', 'save', 'clear-history', 'open-log-panel']);

// 创建本地的响应式状态，用于表单绑定
const localServerAddress = ref('');
const localChannelCodes = ref([]);
const localEnableNotifications = ref(false);
const localEnableSound = ref(true);
const localAutoConnect = ref(false);
const isTesting = ref(false);

// 当组件挂载时，用 props 初始化本地状态
onMounted(() => {
  localServerAddress.value = props.serverAddress;
  localChannelCodes.value = props.channelCodes
    ? props.channelCodes.split('\n').map(code => code.trim()).filter(code => code)
    : [];
  localEnableNotifications.value = props.enableNotifications;
  localEnableSound.value = props.enableSound;
  localAutoConnect.value = props.autoConnect;
});

// 点击保存按钮时，将本地状态通过事件发送给父组件
const handleSave = () => {
  emit('save', {
    serverAddress: localServerAddress.value,
    channelCodes: localChannelCodes.value.join('\n'),
    enableNotifications: localEnableNotifications.value,
    enableSound: localEnableSound.value,
    autoConnect: localAutoConnect.value,
  });
};

const handleTestConnection = () => {
  if (!localServerAddress.value) {
    alert('请输入服务器地址');
    return;
  }
  isTesting.value = true;
  
  let baseUrl;
  try {
    // 解析用户输入的地址，并只提取协议和主机部分
    const url = new URL(localServerAddress.value);
    baseUrl = `${url.protocol}//${url.host}`.replace(/^http/, 'ws');
  } catch (error) {
    alert('无效的服务器地址格式');
    isTesting.value = false;
    return;
  }

  const testUrl = `${baseUrl}/ws/test`;
  const testWs = new WebSocket(testUrl);

  const timer = setTimeout(() => {
    isTesting.value = false;
    alert('连接测试超时');
    testWs.close();
  }, 5000); // 5秒超时

  testWs.onopen = () => {
    console.log('Test WebSocket opened');
    // 发送一个测试消息，包含 machineId
    testWs.send(JSON.stringify({ type: 'test', machineId: props.machineId }));
  };

  testWs.onmessage = (event) => {
    console.log('Test message received:', event.data);
    clearTimeout(timer);
    isTesting.value = false;
    alert('连接测试成功！');
    testWs.close();
  };

  testWs.onerror = (error) => {
    console.error('Test WebSocket error:', error);
    clearTimeout(timer);
    isTesting.value = false;
    alert('连接测试失败，请检查地址或后端服务是否正常。');
    testWs.close();
  };
};
</script>

<template>
  <div class="settings-panel">
    <div class="panel-header">
      <h2>设置</h2>
      <button @click="$emit('close')" class="close-btn">
        <X :size="20" />
      </button>
    </div>
    <div class="panel-content">
      <div class="form-item">
        <label for="server-address">服务器地址</label>
        <div class="input-with-button">
          <input
            id="server-address"
            type="text"
            v-model="localServerAddress"
            placeholder="ws://example.com/ws"
            :disabled="isTesting"
          />
          <button @click="handleTestConnection" class="test-btn" :disabled="isTesting">
            {{ isTesting ? '测试中...' : '测试' }}
          </button>
        </div>
      </div>
      <div class="form-item">
        <label for="receive-codes">通道代码</label>
        <TagInput
          id="receive-codes"
          v-model="localChannelCodes"
        />
      </div>
      <div class="form-item switch-item">
        <label for="notifications">系统通知</label>
        <label class="switch">
          <input
            id="notifications"
            type="checkbox"
            v-model="localEnableNotifications"
          />
          <span class="slider round"></span>
        </label>
      </div>
      <div class="form-item switch-item">
        <label for="sound">声音提醒</label>
        <label class="switch">
          <input
            id="sound"
            type="checkbox"
            v-model="localEnableSound"
          />
          <span class="slider round"></span>
        </label>
      </div>
      <div class="form-item switch-item">
        <label for="auto-connect">自动连接</label>
        <label class="switch">
          <input
            id="auto-connect"
            type="checkbox"
            v-model="localAutoConnect"
          />
          <span class="slider round"></span>
        </label>
      </div>
      <div class="divider"></div>
      <div class="form-item">
        <label>调试</label>
        <button @click="$emit('open-log-panel')" class="secondary-btn">
          查看程序日志
        </button>
      </div>
      <div class="divider"></div>
      <div class="form-item">
        <label>危险区域</label>
        <button @click="$emit('clear-history')" class="danger-btn">
          清除所有历史记录
        </button>
        <p class="danger-tip">此操作将清除本次会话中的所有消息记录。</p>
      </div>
    </div>
    <div class="panel-footer">
      <button @click="handleSave" class="save-btn">保存</button>
    </div>
  </div>
</template>

<style scoped>
.settings-panel {
  position: fixed;
  top: 0;
  right: 0;
  width: 400px;
  height: 100%;
  background-color: #ffffff;
  box-shadow: -5px 0 15px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  z-index: 1000;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.panel-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 5px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background-color: #f2f3f5;
}

.panel-content {
  padding: 20px;
  overflow-y: auto;
  flex-grow: 1;
}

.panel-footer {
  padding: 15px 20px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: flex-end;
}

.save-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.save-btn:hover {
  background-color: #79bbff;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  font-weight: 500;
  margin-bottom: 8px;
  font-size: 14px;
}

.form-item input[type="text"],
.form-item textarea {
  width: 100%;
  padding: 10px;
  font-size: 14px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.form-item input[type="text"]:focus,
.form-item textarea:focus {
  outline: none;
  border-color: #409eff;
}

.switch-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* The switch - the box around the slider */
.switch {
  position: relative;
  display: inline-block;
  width: 40px;
  height: 24px;
}

/* Hide default HTML checkbox */
.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

/* The slider */
.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .4s;
}

.slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: .4s;
}

input:checked + .slider {
  background-color: #22c55e;
}

input:focus + .slider {
  box-shadow: 0 0 1px #22c55e;
}

input:checked + .slider:before {
  transform: translateX(16px);
}

/* Rounded sliders */
.slider.round {
  border-radius: 34px;
}

.slider.round:before {
  border-radius: 50%;
}

.input-with-button {
  display: flex;
  gap: 8px;
}

.input-with-button input {
  flex-grow: 1;
}

.test-btn {
  background-color: #f4f4f5;
  color: #909399;
  border: 1px solid #d3d4d6;
  padding: 0 15px;
  font-size: 14px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.test-btn:hover:not(:disabled) {
  background-color: #e9e9eb;
  border-color: #c8c9cc;
}

.test-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.secondary-btn {
  background-color: #f0f2f5;
  color: #4b5563;
  border: 1px solid #e5e7eb;
  width: 100%;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.secondary-btn:hover {
  background-color: #e5e7eb;
}

.danger-btn {
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fde2e2;
  width: 100%;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.danger-btn:hover {
  background-color: #fde2e2;
  border-color: #fbc4c4;
}

.danger-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.divider {
  border-top: 1px dashed #e4e7ed;
  margin: 30px 0;
}
</style> 