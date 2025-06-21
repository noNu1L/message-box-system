<script setup>
import { X } from 'lucide-vue-next';
import { logs } from '../services/logger';

defineEmits(['close']);
</script>

<template>
  <div class="log-panel">
    <div class="panel-header">
      <h2>程序日志</h2>
      <button @click="$emit('close')" class="close-btn">
        <X :size="20" />
      </button>
    </div>
    <div class="panel-content">
      <pre v-for="(logEntry, index) in logs" :key="index">{{ logEntry }}</pre>
    </div>
  </div>
</template>

<style scoped>
.log-panel {
  position: fixed;
  top: 0;
  left: 0;
  width: 500px;
  height: 100%;
  background-color: #2c2c2c;
  color: #f1f1f1;
  box-shadow: 5px 0 15px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  z-index: 1001;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #444;
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
  color: #f1f1f1;
}

.close-btn:hover {
  background-color: #555;
}

.panel-content {
  padding: 10px 20px;
  overflow-y: auto;
  flex-grow: 1;
  font-family: 'SF Mono', 'Consolas', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
}

.panel-content pre {
  margin: 0;
  padding-bottom: 4px;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style> 