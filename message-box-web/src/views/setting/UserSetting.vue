<template>
  <div>
    <div class="setting-section">
      <div class="setting-title">修改昵称</div>
      <el-form :model="nicknameForm" ref="nicknameFormRef" :rules="nicknameRules" label-width="80px">
        <el-form-item label="新昵称" prop="nickname">
          <el-input v-model="nicknameForm.nickname" placeholder="请输入新昵称"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitNicknameForm">保存</el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="setting-section">
      <div class="setting-title">修改密码</div>
      <el-form :model="passwordForm" ref="passwordFormRef" :rules="passwordRules" label-width="80px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input type="password" v-model="passwordForm.oldPassword" show-password></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input type="password" v-model="passwordForm.newPassword" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input type="password" v-model="passwordForm.confirmPassword" show-password></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitPasswordForm">确认修改</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/userStore';
import { updateUser } from '@/api/user';
import { ElMessage, ElNotification } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();

// --- 修改昵称 ---
const nicknameFormRef = ref(null);
const nicknameForm = reactive({
  nickname: ''
});
const nicknameRules = {
  nickname: [
    { required: true, message: '昵称不能为空', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ]
};

const submitNicknameForm = async () => {
  if (!nicknameFormRef.value) return;
  await nicknameFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await updateUser({ nickname: nicknameForm.nickname });
        userStore.user.nickname = nicknameForm.nickname;
        const userInfo = JSON.parse(localStorage.getItem('userInfo'));
        if (userInfo) {
          userInfo.nickname = nicknameForm.nickname;
          localStorage.setItem('userInfo', JSON.stringify(userInfo));
        }
        ElMessage.success('昵称修改成功！');
        nicknameForm.nickname = '';
      } catch (error) {
        ElNotification.error({
          title: '错误',
          message: error.message || '昵称修改失败'
        });
      }
    }
  });
};

// --- 修改密码 ---
const passwordFormRef = ref(null);
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'));
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'));
  } else {
    callback();
  }
};

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
};

const submitPasswordForm = async () => {
  if (!passwordFormRef.value) return;
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await updateUser({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        });
        ElMessage.success('密码修改成功，请重新登录。');
        userStore.logout();
        localStorage.removeItem('userInfo');
        await router.push('/login');
      } catch (error) {
        ElNotification.error({
          title: '错误',
          message: error.message || '密码修改失败'
        });
      }
    }
  });
};
</script>

<style scoped>
.setting-section {
  max-width: 500px;
}
.setting-section:not(:first-child) {
    margin-top: 20px;
}
.setting-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 20px;
}
</style> 