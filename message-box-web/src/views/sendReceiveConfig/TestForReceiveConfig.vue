<template>
  <el-dialog :model-value="props.visible" title="测试发送" @update:modelValue="closeEditDialog" width="600px" custom-class="responsive-dialog">
    <!-- 给 el-form 添加 ref 和 rules -->
    <el-form ref="editFormRef" :model="editForm" :rules="rules" label-width="100px">
      <el-form-item label="接收组名称">
        <el-input disabled v-model="displayData.groupName" :prefix-icon="CollectionTag"/>
      </el-form-item>
      <el-form-item label="ChannelCode" v-if="displayData.channelCode">
        <el-input disabled v-model="displayData.channelCode" :prefix-icon="PriceTag"/>
      </el-form-item>
      <el-form-item label="描述">
        <el-input disabled v-model="displayData.description" :prefix-icon="Document"/>
      </el-form-item>
      <el-form-item label="接收邮件">
        <div style="display: flex; flex-wrap: wrap; gap: 5px;">
          <el-tag
                  v-for="tag in displayData.receiveEmails"
                  :key="tag"
                  :disable-transitions="false"
          >
            {{ tag }}
          </el-tag>
        </div>
      </el-form-item>
      <el-divider />
      <el-form-item label="主题" prop="subject">
        <el-input maxlength="100" show-word-limit v-model="editForm.subject" :prefix-icon="Document"></el-input>
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
import {ref, reactive, defineProps, defineEmits, onMounted, watch} from 'vue';
import {ElMessage} from 'element-plus';
import { testReceiveConfig } from "@/api/sendReceiveConfig";
import { CollectionTag, PriceTag, Document, Close, Position } from '@element-plus/icons-vue'

// 定义响应式数据
const props = defineProps({
  visible: Boolean,
  currentEditDescription: Object
});
const editFormRef = ref(null);
const displayData = ref({});
let editForm = reactive({
  subject: '',
  content: ''
});
const emit = defineEmits(['update:visible']);

// 定义校验规则
const rules = {
  subject: [
    {required: true, message: '请输入主题', trigger: 'blur'},
    {max: 100, message: '主题不能超过100个字符', trigger: 'blur'}
  ],
  content: [
    {required: true, message: '请输入内容', trigger: 'blur'}
  ]
};

onMounted(() => {
  // getSmtpServer();
})

watch(() => props.visible, (newVal) => {
  if (newVal) {
    displayData.value = {...props.currentEditDescription};
    editForm.subject = '这是一封测试邮件';
    editForm.content = `这是一封发往【${displayData.value.groupName}】的测试邮件。`;
    if (editFormRef.value) {
      editFormRef.value.clearValidate();
    }
  }
});

const closeEditDialog = () => {
  emit('update:visible', false);
};

// 提交表单时进行校验
const submitForm = () => {
  editFormRef.value.validate((valid) => {
    if (valid) {
      commitSend();
    } else {
      console.log('校验失败');
      return false;
    }
  });
};

const commitSend = async () => {
  let postData = {
    channelCode: displayData.value.channelCode,
    subject: editForm.subject,
    content: editForm.content
  }
  try {
    await testReceiveConfig(postData);
    ElMessage.success('发送成功');
    emit('update:visible', false);
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