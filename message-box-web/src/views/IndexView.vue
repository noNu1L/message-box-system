<template>
  <div class="index-container">
    <el-card shadow="never" class="welcome-card">
      <div class="welcome-content">
        <el-icon :size="50" color="#409EFC" class="welcome-icon"><Sunrise /></el-icon>
        <div class="welcome-text">
          <h1>欢迎回来，{{ user?.nickname || '朋友' }}！</h1>
          <p>从这里开始你的消息推送管理。</p>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" class="shortcut-cards">
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="shortcut-card" @click="goTo('/email')">
          <div class="card-content">
            <el-icon :size="40" color="#409EFC"><Message /></el-icon>
            <div class="card-text">
              <h3>收发配置</h3>
              <p>管理消息发送与接收规则</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="shortcut-card" @click="goTo('/sendRecord')">
          <div class="card-content">
            <el-icon :size="40" color="#67C23A"><Tickets /></el-icon>
            <div class="card-text">
              <h3>投递记录</h3>
              <p>查看历史消息与状态</p>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-card shadow="hover" class="shortcut-card" @click="goTo('/userSetting')">
          <div class="card-content">
            <el-icon :size="40" color="#E6A23C"><User /></el-icon>
            <div class="card-text">
              <h3>用户设置</h3>
              <p>修改你的个人信息</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/userStore';
import { storeToRefs } from 'pinia';
import { Message, Tickets, User, Sunrise } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();
const { user } = storeToRefs(userStore);

const goTo = (path) => {
  router.push(path);
};
</script>

<style scoped>
.index-container {
  padding: 0;
}

.welcome-card {
  margin-bottom: 30px;
  border: none;
  background: linear-gradient(135deg, #e6f7ff 0%, #dcf8ff 100%);
  border-radius: 12px;
}

.welcome-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.welcome-icon {
  margin-right: 20px;
  opacity: 0.7;
}

.welcome-text h1 {
  font-size: 24px;
  color: #303133;
  font-weight: 600;
  margin: 0 0 10px 0;
}

.welcome-text p {
  font-size: 14px;
  color: #606266;
  margin: 0;
}

.shortcut-cards .el-col {
  margin-bottom: 20px;
}

.shortcut-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.shortcut-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.card-content {
  display: flex;
  align-items: center;
}

.card-text {
  margin-left: 15px;
}

.card-text h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
  color: #303133;
}

.card-text p {
  margin: 0;
  font-size: 12px;
  color: #909399;
}
</style>