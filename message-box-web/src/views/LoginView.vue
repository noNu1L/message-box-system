<template>
  <el-card class="login-card">
    <template #header>
      <div class="card-header">
        <span>用户登录</span>
      </div>
    </template>
    <el-form :model="loginForm" status-icon :rules="rules" ref="loginFormRef" label-width="100px" @submit.prevent="submitForm">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="loginForm.username" :prefix-icon="User"></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input type="password" v-model="loginForm.password" autocomplete="off" :prefix-icon="Lock" show-password></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm">
          <el-icon style="margin-right: 5px"><Right /></el-icon>
          登录
        </el-button>
        <el-button @click="resetForm">
          <el-icon style="margin-right: 5px"><RefreshLeft /></el-icon>
          重置
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElNotification, ElMessage } from 'element-plus';
import { useUserStore } from '@/store/userStore';
import { login } from "@/api/authn";
import { User, Lock, Right, RefreshLeft } from '@element-plus/icons-vue';
import {encrypt} from "@/utils/crypto";

const router = useRouter();
const userStore = useUserStore();
const loginFormRef = ref(null);

const loginForm = reactive({
  username: '',
  password: ''
});

const rules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ]
});

const submitForm = async () => {
  if (!loginFormRef.value) return;
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const encryptedData = {
          username: encrypt(loginForm.username),
          password: encrypt(loginForm.password)
        };
        const response = await login(encryptedData);
        ElMessage({
          message: '登陆成功',
          type: 'success',
        });
        const lastPath = sessionStorage.getItem('lastPath') || '/';
        sessionStorage.removeItem('lastPath');
        userStore.login(response.data);
        localStorage.setItem('userInfo', JSON.stringify(response.data));
        await router.push(lastPath);
      } catch (error) {
        ElNotification({
          title: '警告',
          message: error.message,
          type: 'warning',
        });
      }
    } else {
      console.log('提交失败!');
      return false;
    }
  });
};

const resetForm = () => {
  if (!loginFormRef.value) return;
  loginFormRef.value.resetFields();
};
</script>

<style scoped>
.login-card {
  max-width: 500px;
  margin: 50px auto;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
</style>  