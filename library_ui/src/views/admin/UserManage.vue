<template>
  <div class="page-container">
    <!-- 筛选区 -->
    <el-card class="toolbar-card panel panel-b" shadow="never">
      <div class="toolbar">
        <div class="filters">
          <el-input v-model="keyword" placeholder="搜索用户名 / 姓名" style="width: 260px" clearable @keyup.enter="handleSearch">
            <template #prepend>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <el-select v-model="role" placeholder="角色" clearable style="width: 160px" @change="handleSearch">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="读者" value="READER" />
          </el-select>

          <el-button :icon="Refresh" @click="load">刷新</el-button>
        </div>

        <div class="right">
          <el-button type="primary" :icon="Plus" @click="openCreate">新增用户</el-button>
        </div>
      </div>
    </el-card>

    <!-- 列表区 -->
    <el-card shadow="never" class="panel panel-a">
      <el-table :data="list" v-loading="loading" style="width:100%" row-class-name="table-row">
        <el-table-column prop="username" label="用户名" width="160" />
        <el-table-column prop="name" label="姓名" width="140" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'success' : 'info'" effect="light" round>
              {{ row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />

        <el-table-column label="启用" width="120">
          <template #default="{ row }">
            <div class="enabled-cell">
              <el-switch v-model="row.enabled" disabled />
              <span class="muted">{{ row.enabled ? '启用' : '禁用' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该用户？" width="220" @confirm="onDelete(row)">
              <template #reference>
                <el-button link type="danger" :icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无用户" />
        </template>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          @current-change="load"
          layout="total, prev, pager, next, jumper"
        />
      </div>
    </el-card>

    <!-- 弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增用户' : '编辑用户'" width="640px" top="8vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username"><el-input v-model="form.username" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" prop="role">
              <el-select v-model="form.role" style="width:100%">
                <el-option label="管理员" value="ADMIN" />
                <el-option label="读者" value="READER" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                show-password
                :placeholder="dialog.mode==='edit' ? '不修改可留空' : '请输入密码（至少6位）'"
              />
              <div class="help muted">{{ dialog.mode==='edit' ? '留空表示不修改密码' : '创建用户必须设置初始密码' }}</div>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用" prop="enabled"><el-switch v-model="form.enabled" /></el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" /></el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible=false">取消</el-button>
        <el-button type="primary" :loading="dialog.loading" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue';
import { listUsers, createUser, updateUser, deleteUser } from '@/api/users';

const keyword = ref('');
const role = ref('');
const loading = ref(false);
const list = ref([]);

const pagination = reactive({ current: 1, size: 10, total: 0 });

const dialog = reactive({ visible: false, mode: 'create', loading: false, id: null });
const formRef = ref();
const form = reactive({
  username: '', password: '', name: '', phone: '', email: '', role: 'READER', enabled: true,
});

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
};

const load = async () => {
  loading.value = true;
  try {
    const params = { page: pagination.current - 1, size: pagination.size };
    if (keyword.value) params.keyword = keyword.value;
    if (role.value) params.role = role.value;
    const res = await listUsers(params);
    list.value = res.content;
    pagination.total = res.totalElements;
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  load();
};

const openCreate = () => {
  dialog.mode = 'create';
  dialog.id = null;
  Object.assign(form, { username: '', password: '', name: '', phone: '', email: '', role: 'READER', enabled: true });
  dialog.visible = true;
  formRef.value?.clearValidate();
};

const openEdit = (row) => {
  dialog.mode = 'edit';
  dialog.id = row.id;
  Object.assign(form, {
    username: row.username,
    password: '',
    name: row.name,
    phone: row.phone,
    email: row.email,
    role: row.role,
    enabled: row.enabled,
  });
  dialog.visible = true;
  formRef.value?.clearValidate();
};

const submit = async () => {
  await formRef.value.validate();
  dialog.loading = true;
  try {
    if (dialog.mode === 'create') {
      if (!form.password) {
        throw new Error('密码不能为空');
      }
      await createUser(form);
      ElMessage.success('新增成功');
    } else {
      await updateUser(dialog.id, { ...form });
      ElMessage.success('保存成功');
    }
    dialog.visible = false;
    load();
  } finally {
    dialog.loading = false;
  }
};

const onDelete = async (row) => {
  await deleteUser(row.id);
  ElMessage.success('删除成功');
  load();
};

onMounted(load);
</script>

<style scoped>
.page-container { display:flex; flex-direction:column; gap: 16px; }
.toolbar { display:flex; justify-content:space-between; align-items:center; gap: 12px; flex-wrap: wrap; }
.filters { display:flex; gap: 12px; align-items:center; flex-wrap: wrap; }
.pagination { margin-top: 16px; display:flex; justify-content:flex-end; }
:deep(.table-row) { cursor: pointer; }
.enabled-cell { display:flex; align-items:center; gap: 8px; }
.help { font-size: 12px; margin-top: 6px; }
</style>
