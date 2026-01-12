<template>
  <div class="page-container">
    <el-card class="toolbar-card panel panel-a" shadow="never">
      <div class="toolbar">
        <div class="left">
          <el-button type="primary" :icon="Plus" @click="openCreate">新增公告</el-button>
          <el-button :icon="Refresh" @click="load">刷新</el-button>
        </div>
        <div class="right">
          <el-input v-model="keyword" placeholder="搜索标题" style="width: 280px" clearable @keyup.enter="handleSearch" />
        </div>
      </div>
    </el-card>

    <el-card class="table-card panel panel-b" shadow="never">
      <el-table :data="filteredList" v-loading="loading" style="width:100%">
        <el-table-column label="图片" width="110">
          <template #default="{ row }">
            <el-image
              class="thumb"
              :src="getFirstImage(row)"
              fit="cover"
              :preview-src-list="getPreviewList(row)"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.published ? 'success' : 'info'" effect="light" round>
              {{ row.published ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该公告？" width="200" @confirm="onDelete(row)">
              <template #reference>
                <el-button link type="danger" :icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无公告">
            <el-button type="primary" @click="openCreate">立即新增</el-button>
          </el-empty>
        </template>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.mode === 'create' ? '新增公告' : '编辑公告'" width="760px" top="8vh">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="dialog-form">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="128" show-word-limit />
        </el-form-item>

        <el-form-item label="内容（支持简单 HTML）" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="可输入 <b>加粗</b>、<br/> 等简单HTML" />
        </el-form-item>

        <el-form-item label="图片（最多3张）">
          <div class="img-uploader">
            <div class="img-list">
              <el-image
                v-for="(u, idx) in form.imageUrls"
                :key="u + '_' + idx"
                class="img-preview"
                :src="getImageUrl(u)"
                fit="cover"
                :preview-src-list="form.imageUrls.map(getImageUrl)"
                preview-teleported
              />
              <el-image
                v-if="form.imageUrls.length === 0"
                class="img-preview"
                :src="getImageUrl('')"
                fit="cover"
              />
            </div>

            <div class="img-actions">
              <el-upload
                ref="uploadRef"
                :key="uploadKey"
                multiple
                :limit="3"
                :show-file-list="false"
                accept="image/*"
                :before-upload="beforeUpload"
                :http-request="onUpload"
                @change="onUploadChange"
              >
                <el-button type="primary" plain :disabled="form.imageUrls.length >= 3">上传图片</el-button>
              </el-upload>
              <el-button plain @click="clearImages" :disabled="form.imageUrls.length === 0">清空</el-button>
            </div>

            <div class="img-inputs">
              <el-input
                v-for="i in 3"
                :key="i"
                v-model="form.imageUrls[i-1]"
                placeholder="可粘贴图片URL（/api/... 或外链）"
                @input="normalizeInputs"
              />
            </div>
          </div>
        </el-form-item>

        <el-form-item label="发布" prop="published">
          <el-switch v-model="form.published" active-text="已发布" inactive-text="草稿" />
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
import { ref, reactive, computed, onMounted } from 'vue';
import { Plus, Refresh, Edit, Delete } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { listAdminNotices, createNotice, updateNotice, deleteNotice } from '@/api/notices';
import { uploadFile } from '@/api/files';

const keyword = ref('');
const loading = ref(false);
const list = ref([]);

const uploadRef = ref();
const uploadKey = ref(0);

const dialog = reactive({ visible: false, mode: 'create', loading: false, id: null });
const formRef = ref();
const form = reactive({
  title: '',
  content: '',
  imageUrls: [],
  published: false,
});

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
};

const load = async () => {
  loading.value = true;
  try {
    list.value = await listAdminNotices();
  } finally {
    loading.value = false;
  }
};

const filteredList = computed(() => {
  const k = keyword.value?.trim()?.toLowerCase();
  if (!k) return list.value;
  return list.value.filter((n) => (n.title || '').toLowerCase().includes(k));
});

const handleSearch = () => {};

const openCreate = () => {
  dialog.mode = 'create';
  dialog.id = null;
  Object.assign(form, { title: '', content: '', imageUrls: [], published: false });
  dialog.visible = true;
  formRef.value?.clearValidate();
};

const openEdit = (row) => {
  dialog.mode = 'edit';
  dialog.id = row.id;
  Object.assign(form, {
    title: row.title,
    content: row.content,
    imageUrls: Array.isArray(row.imageUrls) ? row.imageUrls.slice(0, 3) : (row.imageUrl ? [row.imageUrl] : []),
    published: !!row.published,
  });
  normalizeInputs();
  dialog.visible = true;
  formRef.value?.clearValidate();
};

const beforeUpload = (file) => {
  if (form.imageUrls.length >= 3) {
    ElMessage.warning('最多只能上传3张图片');
    return false;
  }
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    ElMessage.error('只能上传图片文件');
    return false;
  }
  const max = 10 * 1024 * 1024;
  if (file.size > max) {
    ElMessage.error('图片不能超过10MB');
    return false;
  }
  return true;
};

// Element Plus 自定义上传：每次选择多文件会对每个文件调用一次
const onUploadChange = (uploadFileObj) => {
  // 仅用于确认点击后是否成功选择文件
  // 如果这里都不触发，说明 el-upload 没有触发文件选择（多半是组件被遮挡/禁用/样式问题）
  try {
    console.log('[NoticeManage] upload change:', uploadFileObj);
  } catch (e) {
    // ignore
  }
};

const onUpload = async (options) => {
  try {
    const res = await uploadFile(options.file);
    // uploadFile 现在直接返回 { url, filename }
    if (form.imageUrls.length < 3) {
      form.imageUrls.push(res.url);
      normalizeInputs();
    }
    ElMessage.success('上传成功');

    // 重置上传组件内部状态：避免再次选择同一文件时不触发 change，出现“无响应”
    try {
      uploadRef.value?.clearFiles?.();
    } catch (e) {
      // ignore
    }
    uploadKey.value += 1;

    options.onSuccess && options.onSuccess(res);
  } catch (e) {
    ElMessage.error(e?.message || '上传失败');
    options.onError && options.onError(e);
  }
};

const clearImages = () => {
  form.imageUrls = [];
  // 清空后如果再次选择同一文件，浏览器可能不会触发 change，导致“点了没反应”
  // 这里同时重置 el-upload 内部状态 + 强制重新渲染上传组件
  try {
    uploadRef.value?.clearFiles?.();
  } catch (e) {
    // ignore
  }
  uploadKey.value += 1;
};

const normalizeInputs = () => {
  // 过滤空值、去重、限制3个
  const arr = Array.isArray(form.imageUrls) ? form.imageUrls : [];
  const cleaned = [];
  const seen = new Set();
  for (const s of arr) {
    if (!s) continue;
    const t = String(s).trim();
    if (!t) continue;
    if (seen.has(t)) continue;
    seen.add(t);
    cleaned.push(t);
    if (cleaned.length >= 3) break;
  }
  form.imageUrls = cleaned;
};

const getImageUrl = (url) => {
  if (url && typeof url === 'string') {
    if (url.startsWith('/api/')) return 'http://localhost:8080' + url;
    return url;
  }
  return '/empty.png';
};

const getPreviewList = (row) => {
  const arr = Array.isArray(row.imageUrls) ? row.imageUrls : (row.imageUrl ? [row.imageUrl] : []);
  return arr.filter(Boolean).map(getImageUrl);
};

const getFirstImage = (row) => {
  const arr = Array.isArray(row.imageUrls) ? row.imageUrls : (row.imageUrl ? [row.imageUrl] : []);
  return getImageUrl(arr && arr.length > 0 ? arr[0] : '');
};

const submit = async () => {
  await formRef.value.validate();
  dialog.loading = true;
  try {
    const payload = {
      title: form.title,
      content: form.content,
      imageUrls: form.imageUrls,
      published: form.published,
    };

    if (dialog.mode === 'create') {
      await createNotice(payload);
      ElMessage.success('新增成功');
    } else {
      await updateNotice(dialog.id, payload);
      ElMessage.success('保存成功');
    }
    dialog.visible = false;
    await load();
  } finally {
    dialog.loading = false;
  }
};

const onDelete = async (row) => {
  await deleteNotice(row.id);
  ElMessage.success('删除成功');
  await load();
};

onMounted(load);
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.toolbar { display:flex; justify-content:space-between; align-items:center; }
.left { display:flex; gap: 12px; }
.right { display:flex; gap: 12px; align-items:center; }
.thumb { width: 72px; height: 56px; border-radius: 8px; border: 1px solid #e4e7ed; }
.dialog-form { padding: 0 10px; }

.img-uploader { display:flex; flex-direction:column; gap: 10px; }
.img-list { display:flex; gap: 10px; flex-wrap: wrap; }
.img-preview { width: 160px; height: 92px; border-radius: 10px; border: 1px solid #e4e7ed; }
.img-actions { display:flex; gap: 10px; }
.img-inputs { display:flex; flex-direction:column; gap: 8px; }
</style>
