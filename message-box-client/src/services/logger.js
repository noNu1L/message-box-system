import { ref } from 'vue';

export const logs = ref([]);

/**
 * Adds a new log entry to the in-memory log store.
 * @param {string} message The log message.
 */
export function log(message) {
  const timestamp = new Date().toLocaleString('zh-CN', { hour12: false });
  logs.value.unshift(`[${timestamp}] ${message}`);

  // To prevent the log array from growing indefinitely, we cap it at 200 entries.
  if (logs.value.length > 200) {
    logs.value.pop();
  }
} 