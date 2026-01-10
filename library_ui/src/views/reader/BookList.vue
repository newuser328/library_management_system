<template>
  <div class="page-container">
    <!-- 顶部：搜索 + 快捷信息 -->
    <el-card shadow="never" class="toolbar-card panel panel-a">
      <div class="toolbar">
        <div class="left">
          <div class="page-title">图书浏览</div>
          <div class="muted">登录后可直接点击分类名称筛选图书</div>
        </div>

        <div class="right">
          <el-input
            v-model="keyword"
            placeholder="搜索书名 / 作者 / ISBN"
            clearable
            style="width: 320px"
            @keyup.enter="handleSearch"
          >
            <template #prepend>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <el-button type="primary" :icon="Reading" :disabled="selectedBooks.length===0" @click="handleBorrow">
            借阅
          </el-button>
        </div>
      </div>

      <!-- 分类快捷入口：点击分类名称筛选 -->
      <div class="category-tags">
        <el-tag
          class="cat-tag"
          round
          :type="!categoryId ? 'success' : 'info'"
          effect="light"
          @click="selectCategory(null)"
        >全部</el-tag>

        <el-tag
          v-for="c in categories"
          :key="c.id"
          class="cat-tag"
          round
          :type="categoryId === c.id ? 'success' : 'info'"
          effect="light"
          @click="selectCategory(c.id)"
        >{{ c.name }}</el-tag>
      </div>
    </el-card>

    <!-- 内容：表格 -->
    <el-card shadow="never" class="panel panel-b">
      <el-table
        :data="bookList"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        row-class-name="table-row"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="" width="72">
          <template #default="{ row }">
            <el-image
              class="cover"
              :src="getCoverUrl(row)"
              fit="cover"
              :preview-src-list="[getCoverUrl(row)]"
              preview-teleported
            />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="书名" min-width="240" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="140" />
        <el-table-column prop="isbn" label="ISBN" width="180" />
        <el-table-column label="分类" min-width="160">
          <template #default="{ row }">
            <span>{{ formatCategories(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="publisher" label="出版社" width="160" show-overflow-tooltip />
        <el-table-column label="可借数量" width="140">
          <template #default="{ row }">
            <el-tag :type="row.availableCount > 0 ? 'success' : 'danger'" effect="light" round>
              {{ row.availableCount }} / {{ row.totalCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="showBookDetail(row)">详情</el-button>
            <el-button
              link
              type="success"
              :icon="Reading"
              :disabled="row.availableCount <= 0"
              @click="borrowSingle(row)"
            >借阅</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无图书" />
        </template>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          @current-change="handlePageChange"
          layout="total, prev, pager, next, jumper"
        />
      </div>
    </el-card>

    <!-- 图书详情对话框 -->
    <el-dialog v-model="detailDialog.visible" title="图书详情" width="640px" top="8vh">
      <BookDetail :book="detailDialog.book" />
    </el-dialog>

    <!-- 借阅申请对话框 -->
    <el-dialog v-model="borrowDialog.visible" title="借阅申请" width="600px" top="8vh">
      <el-alert type="info" show-icon :closable="false" class="tip">
        提交申请后需管理员审核。通过后将自动生成应还日期。
      </el-alert>

      <!-- 当前借阅书籍列表 -->
      <div class="borrow-books-section">
        <div class="section-title">
          <el-icon><Reading /></el-icon>
          <span>待借阅书籍（{{ selectedBooks.length }} 本）</span>
        </div>
        <div class="books-list">
          <div v-for="book in selectedBooks" :key="book.id" class="book-item">
            <el-image
              class="book-cover"
              :src="getCoverUrl(book)"
              fit="cover"
              :preview-src-list="[getCoverUrl(book)]"
              preview-teleported
            />
            <div class="book-info">
              <div class="book-title">{{ book.title }}</div>
              <div class="book-meta">
                <span>作者：{{ book.author || '-' }}</span>
                <span>ISBN：{{ book.isbn }}</span>
              </div>
              <div class="book-stock">
                <el-tag :type="book.availableCount > 0 ? 'success' : 'danger'" size="small" effect="light">
                  可借 {{ book.availableCount }} / {{ book.totalCount }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>

      <el-form :model="borrowForm" label-position="top">
        <el-form-item label="备注（可选）">
          <el-input v-model="borrowForm.remark" type="textarea" :rows="4" placeholder="例如：尽快审核 / 用于课程作业等" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="borrowDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="borrowDialog.loading" @click="submitBorrow">
            确认借阅（{{ selectedBooks.length }} 本）
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { Search, Reading, View } from '@element-plus/icons-vue';
import { listBooks } from '@/api/books';
import { listCategories } from '@/api/categories';
import { createBorrow } from '@/api/borrows';
import BookDetail from '@/components/books/BookDetail.vue';

const keyword = ref('');
const categoryId = ref(null);
const categories = ref([]);
const loading = ref(false);
const bookList = ref([]);
const selectedBooks = ref([]);

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
});

const detailDialog = reactive({
  visible: false,
  book: null,
});

const borrowDialog = reactive({
  visible: false,
  loading: false,
});

const borrowForm = reactive({
  remark: '',
});

const loadBooks = async () => {
  loading.value = true;
  try {
    const params = { page: pagination.current - 1, size: pagination.size };
    if (keyword.value) params.keyword = keyword.value;
    if (categoryId.value) params.categoryId = categoryId.value;

    const res = await listBooks(params);
    bookList.value = res.content;
    pagination.total = res.totalElements;
  } finally {
    loading.value = false;
  }
};

const loadCategories = async () => {
  categories.value = await listCategories();
};

const getCoverUrl = (book) => {
  if (book && book.coverUrl) {
    if (book.coverUrl.startsWith('/api/')) {
      return 'http://localhost:8080' + book.coverUrl;
    }
    return book.coverUrl;
  }
  // 前端占位图（放在 library_ui/public/empty.png）
  return '/empty.png';
};

const formatCategories = (row) => {
  const arr = row?.categories;
  if (Array.isArray(arr) && arr.length > 0) {
    return arr.map((c) => c.name).join(' / ');
  }
  if (row?.category) return row.category;
  return '未分类';
};

const selectCategory = (id) => {
  categoryId.value = id;
  pagination.current = 1;
  loadBooks();
};

const handleSearch = () => {
  pagination.current = 1;
  loadBooks();
};

const handlePageChange = () => {
  loadBooks();
};

const handleSelectionChange = (selection) => {
  // 选择超过 3 本时，回退为前 3 本并提示
  const arr = Array.isArray(selection) ? selection : [];
  if (arr.length > 3) {
    selectedBooks.value = arr.slice(0, 3);
    ElMessage.warning('每个人一次最多可借三本书');
    return;
  }
  selectedBooks.value = arr;
};

const showBookDetail = (book) => {
  detailDialog.book = book;
  detailDialog.visible = true;
};

const handleBorrow = () => {
  if (!selectedBooks.value || selectedBooks.value.length === 0) return;

  // 过滤掉无库存的书
  const available = selectedBooks.value.filter((b) => b.availableCount > 0);
  const noStock = selectedBooks.value.filter((b) => b.availableCount <= 0);
  if (noStock.length > 0) {
    ElMessage.warning(`有 ${noStock.length} 本图书暂无库存，已自动忽略`);
  }
  if (available.length === 0) {
    return;
  }

  borrowDialog.visible = true;
  borrowForm.remark = '';
};

const borrowSingle = (book) => {
  if (book.availableCount <= 0) {
    ElMessage.warning('该图书暂无库存');
    return;
  }

  // 如果当前已选满 3 本且该书不在已选中，则提示
  if (selectedBooks.value.length >= 3 && !selectedBooks.value.some((b) => b.id === book.id)) {
    ElMessage.warning('每个人一次最多可借三本书');
    return;
  }

  selectedBooks.value = [book];
  borrowDialog.visible = true;
  borrowForm.remark = '';
};

const submitBorrow = async () => {
  if (!selectedBooks.value || selectedBooks.value.length === 0) return;

  // 前端兜底：一次最多提交 3 本
  const toSubmit = selectedBooks.value.slice(0, 3).filter((b) => b.availableCount > 0);
  if (toSubmit.length === 0) return;

  borrowDialog.loading = true;
  try {
    const results = await Promise.all(
      toSubmit.map((book) =>
        createBorrow({ bookId: book.id, remark: borrowForm.remark }).then(
          () => ({ ok: true, book }),
          (error) => ({ ok: false, book, error })
        )
      )
    );

    const successCount = results.filter((r) => r.ok).length;
    const failCount = results.length - successCount;

    if (successCount > 0) {
      ElMessage.success(`成功提交 ${successCount} 本书的借阅申请`);
      if (failCount > 0) {
        // 后端会校验“最多 3 本”，这里提示即可
        ElMessage.warning(`有 ${failCount} 本提交失败（可能已达上限/重复借阅/无库存）`);
      }
    borrowDialog.visible = false;
      await loadBooks();
      selectedBooks.value = [];
    } else {
      ElMessage.error('借阅失败，请稍后重试');
    }
  } finally {
    borrowDialog.loading = false;
  }
};

onMounted(async () => {
  await loadCategories();
  await loadBooks();
});
</script>

<style scoped>
.page-container { display:flex; flex-direction:column; gap: 16px; }
.toolbar { display:flex; justify-content:space-between; gap: 16px; align-items:center; flex-wrap: wrap; }
.left { display:flex; flex-direction:column; gap: 4px; }
.right { display:flex; gap: 12px; align-items:center; flex-wrap: wrap; }

.category-tags {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.cat-tag {
  cursor: pointer;
  user-select: none;
}

.pagination { margin-top: 16px; display:flex; justify-content:flex-end; }
.tip { margin-bottom: 16px; }
:deep(.table-row) { cursor: pointer; }
.cover {
  width: 40px;
  height: 52px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

/* 借阅对话框样式 */
.borrow-books-section {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  margin-bottom: 12px;
}

.books-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.book-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: white;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
  transition: all 0.2s;
}

.book-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.book-cover {
  width: 60px;
  height: 80px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  flex-shrink: 0;
  cursor: pointer;
}

.book-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.book-title {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.book-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.book-meta span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-stock {
  margin-top: auto;
}
</style>
