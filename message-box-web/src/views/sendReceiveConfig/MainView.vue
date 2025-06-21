<template>
  <el-card class="card">
  <el-row align="middle">
    <el-icon :size="24" style="margin-right: 10px;"><Message /></el-icon>
    <span style="font-size: 1.2em;font-weight: bold">消息接收 / 发送配置</span>
  </el-row>
<!--  <p>关于消息收发，都在此进行配置</p>-->
  <el-divider/>


  <el-row style="align-items: center; height: 80px;">
    <el-icon :size="20" style="margin-right: 10px;"><Position /></el-icon>
    <h3>发送配置</h3>
  </el-row>

  <el-card v-loading="sendConfigLoading"
           @mouseenter="editSendConfigButton = true"
           @mouseleave="editSendConfigButton = false"
           shadow="hover"
           style="margin-bottom: 20px;"
  >
    <template #header>
      <el-row class="card-header responsive-header" align="middle" :gutter="10">
        <el-col :xs="24" :sm="24" :md="4">
          <div style="display: flex; align-items: center;">
            <el-icon style="margin-right: 8px;"><Postcard/></el-icon>
            <span>发件邮箱信息</span>
          </div>
        </el-col>
        <el-col :xs="24" :sm="24" :md="10" class="alert-col">
          <el-alert :type="alertType" :closable="false" style="width: 100%"> {{ sendConfig.emailStatusTip }}</el-alert>
        </el-col>
        <el-col :xs="0" :sm="0" :md="1"/>
        <el-col :xs="24" :sm="24" :md="9" class="button-col">
          <template  v-if="editSendConfigButton || isMobile">
            <el-button-group >
              <el-button :icon="Position" @click="testForSend">测试发送</el-button>
              <el-button :icon="Edit" @click="editSendConfig">编辑</el-button>
            </el-button-group>
          </template>
        </el-col>
      </el-row>
    </template>
    <el-row class="description-row">
      <el-col :xs="24" :sm="4" class="description-label">邮箱服务器：</el-col>
      <el-col :xs="24" :sm="20" class="description-content">{{ sendConfig.serverName }}</el-col>
    </el-row>
    <el-row class="description-row">
      <el-col :xs="24" :sm="4" class="description-label">发件地址：</el-col>
      <el-col :xs="24" :sm="20" class="description-content">{{ sendConfig.email }}</el-col>
    </el-row>
    <el-row class="description-row">
      <el-col :xs="24" :sm="4" class="description-label">授权码：</el-col>
      <el-col :xs="24" :sm="20" class="description-content">{{ maskedPassword(sendConfig.password) }}</el-col>
    </el-row>
    <p class="text item" hidden="hidden">server ID：{{ sendConfig.serverId }}</p>

  </el-card>
  <el-divider/>

  <el-row style="align-items: center; height: 80px;">
    <el-icon :size="20" style="margin-right: 10px;"><Management /></el-icon>
    <h3>接收配置</h3>
    <el-button style="margin-left: 30px;" @click="editReceiveConfig(emptyDescription)" :icon="Plus" type="primary">新增一组</el-button>
  </el-row>

  <template v-if="descriptions===null || descriptions.length===0">
    <el-card shadow="never">
      <el-empty style="height: 120px" :image-size="70" description="暂无接收组，请配置后使用"/>
    </el-card>
  </template>

  <template v-for="(description, index) in descriptions" :key="index">
    <el-card shadow="hover"
             style="margin-bottom: 20px"
             @mouseenter="setEditReceiveConfigButtons(index, true)"
             @mouseleave="setEditReceiveConfigButtons(index, false)"
    >
      <el-descriptions border :column="1"
                       :title="description.groupName"
                       :class="{ 'mobile-layout': isMobile }"
      >
        <template #extra v-if="description.showButtons || isMobile">
          <el-button-group class="ml-4 mobile-button-group">
            <el-button :icon="Position" @click="testForReceive(description)">测试发送</el-button>
            <el-button :icon="Edit" @click="editReceiveConfig(description)">编辑</el-button>
            <el-button :icon="Delete" @click="deleteReceiveConfig(description)">删除</el-button>
            <el-button :icon="DocumentCopy" @click="copyQuickSendCode(description)">复制</el-button>
          </el-button-group>
        </template>

        <el-descriptions-item label="接收方式">
          <template v-if="description.receiveMode && description.receiveMode.length">
            <el-tag v-for="mode in description.receiveMode" :key="mode" style="margin-right: 5px;">
              {{ mode === 'email' ? '邮箱接收' : (mode === 'client' ? '客户端接收' : mode) }}
            </el-tag>
          </template>
          <span v-else>未设置</span>
        </el-descriptions-item>

        <!-- 描述项内容 -->
        <el-descriptions-item v-if="description.receiveMode && description.receiveMode.includes('email')" label="通道代码 (ChannelCode)">{{ description.channelCode }}</el-descriptions-item>
        <el-descriptions-item v-if="description.receiveMode && description.receiveMode.includes('email')" label="接收邮箱地址">
          <template v-for="(receiveEmail, index) in description.receiveEmails" :key="index">
            <el-tag style="margin: 5px 5px 0 0">{{ receiveEmail }}</el-tag>
          </template>
        </el-descriptions-item>
        <el-descriptions-item label="描述">{{ description.description }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </template>

  <EditSendConfig
      v-model="editSendConfigDialogVisible"
      :visible="editSendConfigDialogVisible"
      :sendConfig="sendConfig"
      @update:visible="editSendConfigDialogVisible = $event"
      @save="handleSave"
  />

  <EditReceiveConfig
      v-model="editReceiveConfigDialogVisible"
      :visible="editReceiveConfigDialogVisible"
      :currentEditDescription="currentEditDescription"
      @update:visible="editReceiveConfigDialogVisible = $event"
      @save="handleSave"
  />


<!--  测试窗口-->
  <TestForSendConfig
      v-model="testForSendDialogVisible"
      :visible="testForSendDialogVisible"
      @update:visible="testForSendDialogVisible = $event"
  />

  <TestForReceiveConfig
      v-model="testForReceiveDialogVisible"
      :visible="testForReceiveDialogVisible"
      :currentEditDescription="currentEditDescription"
      @update:visible="testForReceiveDialogVisible = $event"
  />
  </el-card>
</template>

<script setup>
import {Plus, Edit, Delete, DocumentCopy, Position, Message, Management, Postcard} from '@element-plus/icons-vue'
import EditSendConfig from './EditSendConfig.vue';
import EditReceiveConfig from './EditReceiveConfig.vue';
import TestForSendConfig from './TestForSendConfig.vue';
import TestForReceiveConfig from './TestForReceiveConfig.vue';
import {onMounted, ref, onUnmounted} from "vue";
import {
  getSendConfig as apiGetSendConfig,
  getReceiveConfig as apiGetReceiveConfig,
  deleteReceiveConfig as apiDeleteReceiveConfig
} from "@/api/sendReceiveConfig";
import {ElMessage} from "element-plus";
import dialog from "@/utils/dialog";

const {SEND_SERVICE_URL} = require('@/config/index');

let descriptions = ref([]);

let currentEditDescription = ref(null);
let editSendConfigDialogVisible = ref(false);
let editReceiveConfigDialogVisible = ref(false);
let testForSendDialogVisible = ref(false);
let testForReceiveDialogVisible = ref(false);
let editSendConfigButton = ref(false);
let emptyDescription = ref({
  groupName: null,
  channelCode: null,
  // receiveCode: null,
  description: null,
  receiveEmails: [],
  receiveMode: []
});

// --- 新增移动端检测逻辑 ---
const isMobile = ref(window.innerWidth <= 768);
const handleResize = () => {
  isMobile.value = window.innerWidth <= 768;
};
// --- 结束 ---

let alertType = ref(null);
let sendConfig = ref({
  serverName: null,
  serverId: null,
  email: null,
  password: null,
  emailStatus: null,
  emailStatusTip: null
});
let sendConfigLoading = ref(false);

const setEditReceiveConfigButtons = (index, value) => {
  descriptions.value[index].showButtons = value;
};

onMounted(() => {
  loadSendConfig();
  loadReceiveConfig();
  window.addEventListener('resize', handleResize);
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
})

const loadSendConfig = async () => {
  sendConfigLoading.value = true;
  const cardHeader = document.querySelector('.el-card__header');
  try {
    const response = await apiGetSendConfig();
    sendConfig.value = response.data;
    switch (response.data.emailStatus) {
      case -1:
        alertType.value = "error";
        if (cardHeader) cardHeader.style.backgroundColor = '#faf1f1';
        break;
      case 0:
        alertType.value = "warning";
        if (cardHeader) cardHeader.style.backgroundColor = '#faf6ed';
        break;
      case 1:
        alertType.value = "success";
        if (cardHeader) cardHeader.style.backgroundColor = '#f2f8ed';
        break;
    }
  } catch (e) {
    ElMessage.error('无法获取发件配置，请稍后再试');
    console.log(e);
  } finally {
    sendConfigLoading.value = false;
  }
}

const loadReceiveConfig = async () => {
  try {
    const response = await apiGetReceiveConfig();
    descriptions.value = response.data;
  } catch (e) {
    ElMessage.error('无法获取接收配置，请稍后再试');
    console.log(e);
  }
}


const editSendConfig = () => {
  editSendConfigDialogVisible.value = true;
}

const copyQuickSendCode = (description) => {
  console.log(description)
  let channelCode = description.channelCode;
  let code = `${SEND_SERVICE_URL}/ms?channelCode=${channelCode}&subject=标题&content=内容`;
  console.log(code)
  navigator.clipboard.writeText(code).then(() => {
    ElMessage.success('代码已复制到剪贴板');
  }).catch(() => {
    ElMessage.error('复制失败');
  });
}

const editReceiveConfig = (description) => {
  editReceiveConfigDialogVisible.value = true;
  currentEditDescription.value = JSON.parse(JSON.stringify(description));
};

const testForSend = () => {
  testForSendDialogVisible.value = true;
};

const testForReceive = (description) => {
  testForReceiveDialogVisible.value = true;
  currentEditDescription.value = JSON.parse(JSON.stringify(description));
};

const deleteReceiveConfig = (description) => {
  dialog.alter("提示", "是否要删除配置？", async () => {
    try {
      await apiDeleteReceiveConfig(description);
      ElMessage.success('删除成功');
      await loadReceiveConfig();
    } catch (e) {
      ElMessage.error('删除失败');
    }
  }, () => {});
};

// 授权码掩码函数
const maskedPassword = (password) => {
  if (!password) return '';
  const visibleLength = 4; // 显示的明文字数
  const maskChar = '*'; // 用来替换隐藏部分的字符
  const maskedPart = password.slice(visibleLength).replace(/./g, maskChar);
  return password.slice(0, visibleLength) + maskedPart;
};

// 处理保存事件
const handleSave = () => {
  loadSendConfig();
  loadReceiveConfig();
};


</script>

<style scoped>

.card {
  margin: 20px;
}

.responsive-header {
  width: 100%;
}
.button-col {
  display: flex;
  justify-content: flex-end;
}

.description-row {
  margin-bottom: 20px;
  font-size: 16px;
}
.description-label {
  color: #606266;
  padding-right: 12px;
}
@media (max-width: 991px) { /* Corresponds to md breakpoint for stacking header */
  .responsive-header .alert-col, .responsive-header .button-col {
    margin-top: 10px;
  }
  .button-col {
    justify-content: flex-start;
  }
  .mobile-button-group {
    display: flex;
    flex-wrap: wrap;
  }
  .mobile-button-group .el-button {
    margin: 0 !important; /* 覆盖 el-button-group 的默认 margin */
  }
}
@media (max-width: 767px) {
  .description-label {
    text-align: left;
    font-weight: bold;
  }
}

/* 使用 :deep() 增加特异性 */
:deep(.el-descriptions__header) {
  height: 40px;
  line-height: 40px;
}

.mobile-button-container {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-start; /* 或者 center / flex-end */
}

:deep(.mobile-layout .el-descriptions__header) {
  height: auto; /* 移除固定高度以适应堆叠 */
  flex-direction: column;
  align-items: flex-start; /* 左对齐 */
}

:deep(.mobile-layout .el-descriptions__extra) {
  margin-top: 10px;
  margin-left: 0 !important; /* 覆盖Element Plus的默认样式 */
}

@media (max-width: 767px) {
  :deep(.mobile-button-group .el-button) {
    padding: 6px 8px;
    font-size: 14px;
  }
}
</style>