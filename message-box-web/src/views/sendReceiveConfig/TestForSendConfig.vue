<template>
  <el-dialog :model-value="props.visible" title="测试发送" @update:modelValue="closeEditDialog" width="600px" custom-class="responsive-dialog">
    <el-form ref="sendFormRef" :model="editForm" :rules="rules" label-width="100px">
      <el-form-item label="接收邮箱地址" prop="email">
        <el-input v-model="editForm.email" :prefix-icon="Message"></el-input>
      </el-form-item>
      <el-form-item label="主题" prop="subject">
        <el-input maxlength="100"  show-word-limit v-model="editForm.subject" :prefix-icon="Document"></el-input>
      </el-form-item>
      <el-form-item label="内容" prop="content">
        <el-input maxlength="1000" show-word-limit v-model="editForm.content"
                  :rows="4"
                  type="textarea"
        ></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeEditDialog" :icon="Close">返回</el-button>
        <el-button type="primary" @click="submitForm" :icon="Position">提交发送</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import {ref, defineProps, defineEmits, watch} from 'vue';
import {ElMessage} from 'element-plus';
import { testSendConfig } from "@/api/sendReceiveConfig";
import { Message, Document, Close, Position } from '@element-plus/icons-vue';

// 定义 props 和 emit
const props = defineProps({
  visible: Boolean
});
const emit = defineEmits(['update:visible']);

// 定义表单引用
const sendFormRef = ref(null);
let editForm = ref({
  email: '',
  subject: '',
  content: ''
});

// 定义校验规则
const rules = {
  email: [
    { required: true, message: '请输入接收邮箱地址', trigger: 'blur' },
    { max: 50, message: '接收邮箱地址地址不能超过50个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '请输入正确的邮件地址', trigger: 'blur' }
  ],
  subject: [
    {required: true, message: '请输入主题', trigger: 'blur'},
    {max: 100, message: '主题不能超过100个字符', trigger: 'blur'}
  ],
  content: [
    {required: true, message: '请输入内容', trigger: 'blur'}
  ]
};

watch(() => props.visible, (newVal) => {
  if (newVal) {
    editForm.value = { email: '', subject: '这是一封测试邮件', content: '这是一封测试邮件' };
    if (sendFormRef.value) {
      sendFormRef.value.clearValidate();
    }
  }
});

// 关闭编辑对话框
const closeEditDialog = () => {
  emit('update:visible', false);
};

// 提交表单时进行校验
const submitForm = () => {
  sendFormRef.value.validate((valid) => {
    if (valid) {
      commitSend();
    } else {
      return false;
    }
  });
};

const commitSend = async () => {
  try {
    await testSendConfig(editForm.value);
    emit('update:visible', false);
    ElMessage.success('发送成功');
  } catch (error) {
    ElMessage.error(error.message);
  }
};
</script>

<style scoped>
/* 媒体查询，用于适配小屏幕设备 */
@media (max-width: 768px) {
  :deep(.responsive-dialog) {
    width: 90% !important;
  }
}
</style>