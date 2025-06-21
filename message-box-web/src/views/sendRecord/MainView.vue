<template>
  <el-card class="card">
  <el-row align="middle">
    <el-icon :size="24" style="margin-right: 10px;"><Memo /></el-icon>
    <span style="font-size: 1.2em;font-weight: bold">消息投递记录</span>
  </el-row>
  <el-divider/>

  <el-row align="middle">
    <el-radio-group v-model="showType">
      <el-radio-button label="所有" value="all" />
      <el-radio-button label="邮件" value="email" />
      <el-radio-button label="客户端" value="client" />
    </el-radio-group>

    <el-button @click="pullSendRecord" :icon="Refresh" type="primary" style="margin-left: 20px">刷新</el-button>
  </el-row>


  <div v-loading="loading" style="margin: 20px 0"></div>
  <template v-for="(sendRecord, index) in currentShowSendRecordData" :key="index">
    <el-card shadow="hover" style="margin-bottom: 20px;">
      <el-descriptions :title=sendRecord.title column="2">
        <el-descriptions-item v-if="sendRecord.r1c1_label !=null" :label=sendRecord.r1c1_label>{{ sendRecord.r1c1_value }}</el-descriptions-item>
        <el-descriptions-item v-if="sendRecord.r1c2_label !=null" :label=sendRecord.r1c2_label>{{ sendRecord.r1c2_value }}</el-descriptions-item>
        <el-descriptions-item v-if="sendRecord.r2c1_label !=null" :label=sendRecord.r2c1_label>{{ sendRecord.r2c1_value }}</el-descriptions-item>
        <el-descriptions-item v-if="sendRecord.r2c2_label !=null" :label=sendRecord.r2c2_label>{{ sendRecord.r2c2_value }}</el-descriptions-item>
        <el-descriptions-item v-if="sendRecord.r3c1_label !=null" :span="2" :label=sendRecord.r3c1_label>
          {{ sendRecord.r3c1_value }}
        </el-descriptions-item>
        <el-descriptions-item v-if="sendRecord.r4c1_label !=null" :span="2" :label=sendRecord.r4c1_label>
          {{ sendRecord.r4c1_value }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </template>

</el-card>
</template>
<script setup>
import {Refresh, Memo} from '@element-plus/icons-vue'
import {ElMessage} from 'element-plus';
import {ref,onMounted, watch} from 'vue';
import { pullSendRecord } from "@/api/sendRecord";

const loading = ref(false);
const sendRecordData = ref(null);
const currentShowSendRecordData = ref([{
  r1c1_label: "",
  r1c1_value: "",
  r1c2_label: "",
  r1c2_value: "",
  r2c1_label: "",
  r2c1_value: "",
  r2c2_label: "",
  r2c2_value: "",
  r3c1_label: "",
  r3c1_value: "",
  r4c1_label: "",
  r4c1_value: "",
}]);

const showType = ref('all');

const pullSendRecordFunc = async () => {
  loading.value = true;
  try {
    const response = await pullSendRecord();
    sendRecordData.value = response.data;
    currentShowSendRecordData.value = sendRecordData.value[showType.value];
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  pullSendRecordFunc()
})

watch(() => showType.value, (newVal) => {
  if (sendRecordData.value) {
    currentShowSendRecordData.value = sendRecordData.value[newVal];
  }
});

</script>

<style scoped>
.card {
  margin: 20px;
}

</style>
