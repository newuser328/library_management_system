<template>
  <div class="auth-page">
    <div class="auth-shell">
      <!-- 左侧：品牌说明 -->
      <div class="brand-panel panel panel-c">
        <div class="brand-top">
          <div class="logo">L</div>
          <div>
            <div class="brand-title">图书馆管理系统</div>
            <div class="brand-sub">注册中心</div>
          </div>
        </div>

        <div class="brand-center">
          <div class="headline">创建你的账号</div>
          <div class="desc">读者账号可直接注册；管理员账号需要由管理员生成的一次性口令（24小时有效）。</div>

          <div class="steps">
            <div class="step">
              <div class="num">1</div>
              <div>
                <div class="s1">读者注册</div>
                <div class="s2">填写信息即可使用</div>
              </div>
            </div>
            <div class="step">
              <div class="num">2</div>
              <div>
                <div class="s1">管理员口令</div>
                <div class="s2">由管理员在管理端生成</div>
              </div>
            </div>
            <div class="step">
              <div class="num">3</div>
              <div>
                <div class="s1">管理员注册</div>
                <div class="s2">输入口令完成注册</div>
              </div>
            </div>
          </div>
        </div>

        <div class="brand-bottom">
          <div class="kbd">提示：注册成功后会跳转到登录页</div>
          <div class="muted small">如需管理员口令，请联系管理员</div>
        </div>

        <div class="glow g1" />
        <div class="glow g2" />
      </div>

      <!-- 右侧：表单卡片 -->
      <div class="form-panel panel panel-d">
        <el-card class="card" shadow="never">
          <div class="card-header">
            <div class="title">用户注册</div>
            <div class="sub muted">请选择注册类型</div>
          </div>

          <el-tabs v-model="activeTab" class="tabs">
            <el-tab-pane label="读者注册" name="reader">
              <RegisterReaderForm @success="onRegisterSuccess" />
            </el-tab-pane>
            <el-tab-pane label="管理员注册" name="admin">
              <RegisterAdminForm @success="onRegisterSuccess" />
            </el-tab-pane>
          </el-tabs>

          <div class="actions">
            <el-link type="primary" @click="$router.push('/login')">已有账号？去登录</el-link>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import RegisterReaderForm from '@/components/auth/RegisterReaderForm.vue';
import RegisterAdminForm from '@/components/auth/RegisterAdminForm.vue';

const router = useRouter();
const activeTab = ref('reader');

const onRegisterSuccess = () => {
  ElMessage.success('注册成功，请登录');
  router.push('/login');
};
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 18px;
}
.auth-shell {
  width: min(1100px, 100%);
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid rgba(229,231,235,0.9);
  background: rgba(255,255,255,0.65);
  backdrop-filter: blur(12px);
}

.brand-panel {
  position: relative;
  padding: 22px;
  color: #0f172a;
  background: linear-gradient(135deg, rgba(34,197,94,0.10), rgba(79,70,229,0.12));
}
.brand-top {
  display: flex;
  gap: 12px;
  align-items: center;
}
.logo {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  color: white;
  background: linear-gradient(135deg, #22c55e, #4f46e5);
  box-shadow: 0 14px 35px rgba(34,197,94,0.18);
}
.brand-title { font-size: 20px; font-weight: 900; line-height: 1.1; }
.brand-sub { font-size: 12px; color: rgba(15,23,42,0.65); margin-top: 2px; }

.brand-center { margin-top: 28px; max-width: 460px; }
.headline { font-size: 24px; font-weight: 900; letter-spacing: 0.3px; }
.desc { margin-top: 10px; color: rgba(15,23,42,0.75); line-height: 1.7; }
.steps { margin-top: 16px; display:flex; flex-direction:column; gap: 12px; }
.step {
  display:flex;
  gap: 12px;
  align-items:flex-start;
  padding: 12px 12px;
  border-radius: 14px;
  border: 1px solid rgba(255,255,255,0.65);
  background: rgba(255,255,255,0.55);
}
.num {
  width: 28px;
  height: 28px;
  border-radius: 10px;
  display:flex;
  align-items:center;
  justify-content:center;
  font-weight: 900;
  color: white;
  background: linear-gradient(135deg, #4f46e5, #06b6d4);
}
.s1 { font-weight: 800; }
.s2 { font-size: 12px; color: rgba(15,23,42,0.65); margin-top: 2px; }

.brand-bottom { position: absolute; left: 22px; right: 22px; bottom: 18px; }
.kbd {
  display: inline-block;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px dashed rgba(15,23,42,0.25);
  background: rgba(255,255,255,0.55);
}
.small { font-size: 12px; }

.glow {
  position: absolute;
  border-radius: 999px;
  filter: blur(40px);
  opacity: 0.7;
}
.g1 { width: 260px; height: 260px; background: rgba(34,197,94,0.30); top: -80px; left: -80px; }
.g2 { width: 240px; height: 240px; background: rgba(79,70,229,0.28); bottom: -90px; right: -90px; }

.form-panel {
  padding: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.65);
}
.card {
  width: min(480px, 100%);
  border-radius: 16px;
}
.card-header { margin-bottom: 10px; }
.title { font-size: 22px; font-weight: 900; }
.sub { margin-top: 4px; }
.actions { margin-top: 12px; text-align: center; }

:deep(.el-tabs__nav) { width: 100%; }
:deep(.el-tabs__item) { flex: 1; text-align: center; }

@media (max-width: 960px) {
  .auth-shell { grid-template-columns: 1fr; }
  .brand-panel { display: none; }
  .form-panel { padding: 18px; }
}
</style>
