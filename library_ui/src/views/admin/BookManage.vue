<template>
  <div class="page-container">
    <!-- 1. 工具栏 -->
    <el-card class="toolbar-card panel panel-a" shadow="never">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索书名 / 作者 / ISBN" style="width: 320px" clearable @keyup.enter="handleSearch">
          <template #prepend>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <div class="right">
          <el-button type="primary" :icon="Plus" @click="openCreate">新增图书</el-button>
          <el-button :icon="Refresh" @click="load">刷新</el-button>
        </div>
      </div>
    </el-card>

    <!-- 2. 表格 -->
    <el-card class="table-card panel panel-b" shadow="never">
      <el-table :data="list" v-loading="loading" style="width:100%" row-class-name="table-row">
        <el-table-column prop="title" label="书名" min-width="220" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="140" />
        <el-table-column prop="isbn" label="ISBN" width="180" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="publisher" label="出版社" width="160" show-overflow-tooltip />
        <el-table-column label="库存" width="130">
          <template #default="{ row }">
            <el-tag :type="row.availableCount > 0 ? 'success' : 'danger'" effect="light" round>
              {{ row.availableCount }} / {{ row.totalCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该图书？" width="200" @confirm="onDelete(row)">
              <template #reference>
                <el-button link type="danger" :icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无图书">
            <el-button type="primary" @click="openCreate">立即新增</el-button>
          </el-empty>
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

    <!-- 3. 弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增图书' : '编辑图书'" width="640px" top="8vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="ISBN" prop="isbn"><el-input v-model="form.isbn" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="书名" prop="title"><el-input v-model="form.title" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作者" prop="author"><el-input v-model="form.author" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category"><el-input v-model="form.category" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版社" prop="publisher"><el-input v-model="form.publisher" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总库存" prop="totalCount"><el-input-number v-model="form.totalCount" :min="0" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="封面URL" prop="coverUrl"><el-input v-model="form.coverUrl" /></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="简介" prop="description"><el-input v-model="form.description" type="textarea" :rows="4" /></el-form-item>
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
import { Search, Plus, Refresh, Edit, Delete } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { listBooks, createBook, updateBook, deleteBook } from '@/api/books';

const keyword = ref('');
const loading = ref(false);
const list = ref([]);

const pagination = reactive({ current: 1, size: 10, total: 0 });

const dialog = reactive({ visible: false, mode: 'create', loading: false, id: null });
const formRef = ref();
const form = reactive({
  isbn: '', title: '', author: '', category: '', publisher: '', description: '', coverUrl: '', totalCount: 1,
});

const rules = {
  isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  totalCount: [{ required: true, message: '请输入库存', trigger: 'blur' }],
};

const load = async () => {
  loading.value = true;
  try {
    const params = { page: pagination.current - 1, size: pagination.size };
    if (keyword.value) params.keyword = keyword.value;
    const res = await listBooks(params);
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
  Object.assign(form, { isbn: '', title: '', author: '', category: '', publisher: '', description: '', coverUrl: '', totalCount: 1 });
  dialog.visible = true;
  formRef.value?.clearValidate();
};

const openEdit = (row) => {
  dialog.mode = 'edit';
  dialog.id = row.id;
  Object.assign(form, {
    isbn: row.isbn,
    title: row.title,
    author: row.author,
    category: row.category,
    publisher: row.publisher,
    description: row.description,
    coverUrl: row.coverUrl,
    totalCount: row.totalCount,
  });
  dialog.visible = true;
  formRef.value?.clearValidate();
};

const submit = async () => {
  await formRef.value.validate();
  dialog.loading = true;
  try {
    if (dialog.mode === 'create') {
      await createBook(form);
      ElMessage.success('新增成功');
    } else {
      await updateBook(dialog.id, form);
      ElMessage.success('保存成功');
    }
    dialog.visible = false;
    load();
  } finally {
    dialog.loading = false;
  }
};

const onDelete = async (row) => {
  await deleteBook(row.id);
  ElMessage.success('删除成功');
  load();
};

onMounted(load);
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.toolbar-card { margin-bottom: 0; }
.toolbar{ display:flex; justify-content:space-between; align-items:center; gap: 12px; }
.right{ display:flex; gap: 12px; }
.pagination{ margin-top: 16px; display:flex; justify-content:flex-end; }
.dialog-form { padding: 0 10px; }
:deep(.table-row) { cursor: pointer; }
</style>
