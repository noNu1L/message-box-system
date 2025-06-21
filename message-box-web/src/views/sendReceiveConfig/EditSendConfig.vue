<template>
  <el-dialog :model-value="props.visible" title="编辑发件方信息" @update:modelValue="closeEditDialog" width="500px" custom-class="responsive-dialog">
    <!-- 添加 ref 和 rules -->
    <el-form ref="sendFormRef" :model="editForm" :rules="rules" label-width="100px">
      <el-form-item label="邮箱服务器" prop="serverId">
        <el-select v-model="editForm.serverId" placeholder="请选择邮箱服务器" style="width: 100%">
          <el-option
              v-for="server in emailServerOptions"
              :key="server.serverId"
              :label="server.serverName"
              :value="server.serverId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="发件地址" prop="email">
        <el-input v-model="editForm.email" :prefix-icon="Message"></el-input>
      </el-form-item>
      <el-form-item label="授权码" prop="password">
        <el-input
            v-model="editForm.password"
            type="password"
            :prefix-icon="Lock"
            show-password
        >
        </el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeEditDialog" :icon="Close">取消</el-button>
        <el-button type="primary" @click="submitForm" :icon="Check">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, defineProps, defineEmits, onMounted, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { getSmtpServer, updateSendConfig } from "@/api/sendReceiveConfig";
import { Message, Lock, Check, Close } from '@element-plus/icons-vue';

// 定义 props 和 emit
const props = defineProps({
  visible: Boolean,
  sendConfig: Object
});
let editForm = ref({...props.sendConfig});
let emailServerOptions = ref([]);
const emit = defineEmits(['update:visible', 'save']);

// 定义表单引用
const sendFormRef = ref(null);

// 控制授权码显示/隐藏 - a-input's show-password prop handles this automatically
// const passwordVisible = ref(false);

// 切换授权码可见性
// const togglePasswordVisibility = () => {
//   passwordVisible.value = !passwordVisible.value;
// };

// 定义校验规则
const rules = {
  serverId: [
    { required: true, message: '请选择邮箱服务器', trigger: 'change' }
  ],
  email: [
    { required: true, message: '请输入发件地址', trigger: 'blur' },
    { max: 50, message: '发件地址不能超过50个字符', trigger: 'blur' },
    // 可选：添加邮箱格式验证
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '请输入正确的邮件地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入授权码', trigger: 'blur' },
    { min: 6, max: 50, message: '授权码长度应在6到50个字符之间', trigger: 'blur' }
  ]
};

onMounted(() => {
  getSmtpServerList();
})

watch(() => props.visible, (newVal) => {
  if (newVal) {
    editForm.value = {...props.sendConfig};
    if (sendFormRef.value) {
      sendFormRef.value.clearValidate();
    }
  }
});

watch(() => props.sendConfig, (newConfig) => {
  editForm.value = {...newConfig};
}, { deep: true });

// 获取服务器列表
const getSmtpServerList = async () => {
  try {
    const response = await getSmtpServer();
    emailServerOptions.value = response.data;
  } catch (e) {
    ElMessage.error('获取服务器列表失败');
  }
};

// 关闭编辑对话框
const closeEditDialog = () => {
  emit('update:visible', false);
};

// 提交表单时进行校验
const submitForm = () => {
  sendFormRef.value.validate((valid) => {
    if (valid) {
      saveEdit();
    } else {
      console.log('校验失败');
      return false;
    }
  });
};

// 保存编辑
const saveEdit = async () => {
  try {
    await updateSendConfig(editForm.value);
    ElMessage.success("保存成功");
    emit('update:visible', false);
    emit('save', editForm.value);
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