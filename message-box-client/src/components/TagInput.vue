<script setup>
import { ref, watch } from 'vue';
import { X } from 'lucide-vue-next';

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['update:modelValue']);

const newTag = ref('');
const tags = ref([...props.modelValue]);

watch(() => props.modelValue, (newValue) => {
  tags.value = [...newValue];
});

const addTag = () => {
  const tagToAdd = newTag.value.trim();
  if (tagToAdd && !tags.value.includes(tagToAdd)) {
    tags.value.push(tagToAdd);
    emit('update:modelValue', tags.value);
  }
  newTag.value = '';
};

const removeTag = (index) => {
  tags.value.splice(index, 1);
  emit('update:modelValue', tags.value);
};
</script>

<template>
  <div class="tag-input-container">
    <div class="tags-wrapper">
      <span v-for="(tag, index) in tags" :key="tag" class="tag">
        {{ tag }}
        <button @click="removeTag(index)" class="remove-tag-btn" :title="`移除 ${tag}`">
          <X :size="14" />
        </button>
      </span>
      <input
        v-model="newTag"
        @keydown.enter.prevent="addTag"
        @keydown.backspace="newTag === '' && tags.length > 0 && removeTag(tags.length - 1)"
        type="text"
        class="tag-input"
        placeholder="输入后按回车添加..."
      />
    </div>
  </div>
</template>

<style scoped>
.tag-input-container {
  width: 100%;
  padding: 8px 10px;
  font-size: 14px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
  transition: border-color 0.2s;
  background-color: #fff;
  min-height: 40px;
}

.tag-input-container:focus-within {
  border-color: #409eff;
}

.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.tag {
  display: inline-flex;
  align-items: center;
  background-color: #f0f2f5;
  color: #606266;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
}

.remove-tag-btn {
  background: none;
  border: none;
  cursor: pointer;
  margin-left: 6px;
  padding: 0;
  display: flex;
  align-items: center;
  color: #909399;
  transition: color 0.2s;
}
.remove-tag-btn:hover {
  color: #303133;
}

.tag-input {
  border: none;
  outline: none;
  flex-grow: 1;
  padding: 4px 0;
  background-color: transparent;
  min-width: 120px;
}
</style> 