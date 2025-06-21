<template>
  <el-dialog :model-value="props.visible" title="编辑接收信息" @update:modelValue="closeEditDialog" width="600px" custom-class="responsive-dialog">
    <!-- 给 el-form 添加 ref 和 rules -->
    <el-form ref="editFormRef" :model="editForm" :rules="rules" label-width="100px">

      <el-form-item label="接收组名称" prop="groupName">
        <el-input v-model="editForm.groupName" :prefix-icon="CollectionTag"></el-input>
      </el-form-item>

      <el-form-item label="接收方式" prop="receiveMode">
        <el-checkbox-group v-model="editForm.receiveMode">
          <el-checkbox label="邮箱接收" value="email" />
          <el-checkbox label="客户端接收" value="client" />
        </el-checkbox-group>
      </el-form-item>

      <template v-if="editForm.groupId && editForm.receiveMode && editForm.receiveMode.includes('email')">
        <el-form-item label="ChannelCode" >
          <el-input disabled v-model="editForm.channelCode" :prefix-icon="Key">
            <template #append>
              <el-button :loading="channelCodeLoading" @click="reGetEmailChannelCodeFunc()" :icon="Refresh">
                重新获取
              </el-button>
            </template>
          </el-input>
        </el-form-item>
      </template>

      <template v-if="editForm.receiveMode && editForm.receiveMode.includes('email')">
        <el-form-item label="接收邮箱" prop="receiveEmails">
          <div class="flex gap-2" style="display: flex; flex-wrap: wrap; gap: 5px;">
            <el-tag
                v-for="tag in editForm.receiveEmails"
                :key="tag"
                closable
                :disable-transitions="false"
                @close="handleClose(tag)"
            >
              {{ tag }}
            </el-tag>
            <el-input
                v-if="inputEmailVisible"
                ref="inputRef"
                v-model="inputEmail"
                style="width: 150px;"
                size="small"
                @keyup.enter="handleInputEmailConfirm"
                @blur="handleInputEmailConfirm"
            />
            <el-button v-else class="button-new-tag" size="small" @click="showInput" :icon="Plus">
              添加
            </el-button>
          </div>
        </el-form-item>
      </template>

      <el-form-item label="描述" prop="description">
        <el-input v-model="editForm.description" :prefix-icon="Document"></el-input>
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
import {Refresh, Plus, CollectionTag, Document, Check, Close, Key} from '@element-plus/icons-vue'
import {ref, defineProps, defineEmits, onMounted, watch, nextTick} from 'vue';
import {ElMessage, ElInput} from 'element-plus';
import { updateReceiveConfig } from "@/api/sendReceiveConfig";
import { reGetEmailChannelCode } from "@/api/channelCode";
import dialog from "@/utils/dialog";
// import {reGetReceiveCode} from "@/api/receiveCode";

// 定义响应式数据
const inputEmail = ref('');
const inputEmailVisible = ref(false);
const inputRef = ref(null);
const channelCodeLoading = ref(false);
const props = defineProps({
  visible: Boolean,
  currentEditDescription: Object
});
const editFormRef = ref(null);

let editForm = ref({});
const emit = defineEmits(['update:visible', 'save']);

// 邮箱格式验证函数
const validateEmail = (email) => {
  const re = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  return re.test(email.trim());
};

const validateReceiveEmails = (rule, value, callback) => {
  if (editForm.value.receiveMode?.includes('email')) {
    if (!editForm.value.receiveEmails || editForm.value.receiveEmails.length === 0) {
      callback(new Error('请至少输入一个接收邮箱的地址'));
    } else {
      callback();
    }
  } else {
    callback();
  }
};

// 定义校验规则
const rules = {
  groupName: [
    { required: true, message: '请输入接收组名称', trigger: 'blur' },
    { max: 20, message: '接收组名称不能超过20个字符', trigger: 'blur' }
  ],
  receiveMode: [
    { type: 'array', required: true, message: '请至少选择一种接收方式', trigger: 'change' },
  ],
  receiveEmails: [
    { validator: validateReceiveEmails, trigger: ['blur', 'change'] }
  ],
  description: [
    { max: 50, message: '描述不能超过50个字符', trigger: 'blur' }
  ]
};

const reGetEmailChannelCodeFunc = () => {
  dialog.alter("警告", "是否刷新ChannelCode，保存后原先的ChannelCode将不可用？", async () => {
    channelCodeLoading.value = true;
    try {
      const response = await reGetEmailChannelCode();
      editForm.value.channelCode = response.data;
    } catch (error) {
      ElMessage.error(error.message);
    } finally {
      channelCodeLoading.value = false;
    }
  }, () => {})
};

// 定义方法
const handleClose = (tag) => {
  const index = editForm.value.receiveEmails.indexOf(tag);
  if (index > -1) {
    editForm.value.receiveEmails.splice(index, 1);
  }
};

const showInput = () => {
  inputEmailVisible.value = true;
  nextTick(() => {
    inputRef.value.focus();
  });
};


// 修改 handleInputEmailConfirm 方法以包含邮箱格式验证和长度限制
const handleInputEmailConfirm = () => {
  const email = inputEmail.value.trim();
  if (email && validateEmail(email)) {
    if (email.length <= 50) {
      if (!editForm.value.receiveEmails.includes(email)) {
        editForm.value.receiveEmails.push(email);
        inputEmail.value = '';
      } else {
        ElMessage.warning('该接收邮箱地址已存在');
        return ;
      }
    } else {
      ElMessage.error('接收邮箱地址不能超过50个字符');
      return ;
    }
  } else if (email) {
    ElMessage.error('请输入正确的接收邮箱地址');
    return ;
  }
  inputEmailVisible.value = false;
};

onMounted(() => {
  // getSmtpServer();
})

watch(() => editForm.value.receiveMode, (newVal, oldVal) => {
  if (oldVal && oldVal.includes('email') && newVal && !newVal.includes('email')) {
    editForm.value.receiveEmails = [];
    if (editFormRef.value) {
      editFormRef.value.clearValidate('receiveEmails');
    }
  }
}, { deep: true });


watch(() => props.visible, (newVal) => {
  if (newVal) {
    editForm.value = JSON.parse(JSON.stringify(props.currentEditDescription));
    if (!editForm.value.receiveMode) {
      if (editForm.value.receiveEmails && editForm.value.receiveEmails.length > 0) {
        editForm.value.receiveMode = ['email'];
      } else {
        editForm.value.receiveMode = [];
      }
    }
    if (editFormRef.value) {
      editFormRef.value.clearValidate();
    }
  }
});

// 关闭编辑对话框
const closeEditDialog = () => {
  emit('update:visible', false);
};

// 提交表单时进行校验
const submitForm = () => {
  editFormRef.value.validate((valid) => {
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
    await updateReceiveConfig(editForm.value);
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

.el-input-group__append .el-button {
  background-color: #f5f7fa;
  color: #909399;
  border: 1px solid #dcdfe6;
  border-left: 0;
  border-radius: 0 4px 4px 0;
}
</style>