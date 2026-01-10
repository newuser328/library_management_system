<template>
  <div class="page-container">
    <el-card class="toolbar-card panel panel-a" shadow="never">
      <div class="toolbar">
        <div class="left">
          <div class="page-title">分类管理</div>
          <div class="muted">维护图书分类，支持新增 / 编辑 / 删除</div>
        </div>
        <div class="right">
          <el-button type="primary" :icon="Plus" @click="openCreate">新增分类</el-button>
          <el-button :icon="Refresh" @click="load">刷新</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card panel panel-b" shadow="never">
      <el-table :data="list" v-loading="loading" style="width:100%" row-class-name="table-row">
        <el-table-column prop="name" label="分类名称" min-width="240" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该分类？" width="220" @confirm="onDelete(row)">
              <template #reference>
                <el-button link type="danger" :icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无分类">
            <el-button type="primary" @click="openCreate">立即新增</el-button>
          </el-empty>
        </template>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增分类' : '编辑分类'" width="520px" top="10vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：文学 / 计算机 / 科幻" />
        </el-form-item>
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
import { Plus, Refresh, Edit, Delete } from '@element-plus/icons-vue';
import { listCategories, createCategory, updateCategory, deleteCategory } from '@/api/categories';

const loading = ref(false);
const list = ref([]);

const dialog = reactive({ visible: false, mode: 'create', loading: false, id: null });
const formRef = ref();
const form = reactive({ name: '' });

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
};

const load = async () => {
  loading.value = true;
  try {
    const res = await listCategories();
    list.value = res;
  } finally {
    loading.value = false;
  }
};

const openCreate = () => {
  dialog.mode = 'create';
  dialog.id = null;
  form.name = '';
  dialog.visible = true;
  formRef.value?.clearValidate();
};

const openEdit = (row) => {
  dialog.mode = 'edit';
  dialog.id = row.id;
  form.name = row.name;
  dialog.visible = true;
  formRef.value?.clearValidate();
};

const submit = async () => {
  await formRef.value.validate();
  dialog.loading = true;
  try {
    if (dialog.mode === 'create') {
      await createCategory({ name: form.name });
      ElMessage.success('新增成功');
    } else {
      await updateCategory(dialog.id, { name: form.name });
      ElMessage.success('保存成功');
    }
    dialog.visible = false;
    load();
  } finally {
    dialog.loading = false;
  }
};

const onDelete = async (row) => {
  try {
    await deleteCategory(row.id);
    ElMessage.success('删除成功');
    load();
  } catch (e) {
    // request.js 一般会把后端 msg 透出，这里兜底提示
    ElMessage.error(e?.message || '删除失败');
    throw e;
  }
};

onMounted(load);
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.toolbar{ display:flex; justify-content:space-between; align-items:center; gap: 12px; flex-wrap: wrap; }
.left{ display:flex; flex-direction:column; gap: 4px; }
.right{ display:flex; gap: 12px; }
.dialog-form { padding: 0 10px; }
:deep(.table-row) { cursor: pointer; }
</style>
