<template>
  <div class="page-container">
    <!-- 顶部：搜索 + 快捷信息 -->
    <el-card shadow="never" class="toolbar-card panel panel-a">
      <div class="toolbar">
        <div class="left">
          <div class="page-title">图书浏览</div>
          <div class="muted">搜索你想借阅的书籍，点击“借阅”提交申请</div>
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

          <el-button type="primary" :icon="Reading" :disabled="!selectedBook" @click="handleBorrow">
            借阅
          </el-button>
        </div>
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
        <el-table-column prop="title" label="书名" min-width="240" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="140" />
        <el-table-column prop="isbn" label="ISBN" width="180" />
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
    <el-dialog v-model="borrowDialog.visible" title="借阅申请" width="520px" top="10vh">
      <el-alert type="info" show-icon :closable="false" class="tip">
        提交申请后需管理员审核。通过后将自动生成应还日期。
      </el-alert>

      <el-form :model="borrowForm" label-position="top">
        <el-form-item label="备注（可选）">
          <el-input v-model="borrowForm.remark" type="textarea" :rows="4" placeholder="例如：尽快审核 / 用于课程作业等" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="borrowDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="borrowDialog.loading" @click="submitBorrow">确认借阅</el-button>
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
import { createBorrow } from '@/api/borrows';
import BookDetail from '@/components/books/BookDetail.vue';

const keyword = ref('');
const loading = ref(false);
const bookList = ref([]);
const selectedBook = ref(null);

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

    const res = await listBooks(params);
    bookList.value = res.content;
    pagination.total = res.totalElements;
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.current = 1;
  loadBooks();
};

const handlePageChange = () => {
  loadBooks();
};

const handleSelectionChange = (selection) => {
  selectedBook.value = selection.length > 0 ? selection[0] : null;
};

const showBookDetail = (book) => {
  detailDialog.book = book;
  detailDialog.visible = true;
};

const handleBorrow = () => {
  if (!selectedBook.value) return;
  borrowSingle(selectedBook.value);
};

const borrowSingle = (book) => {
  if (book.availableCount <= 0) {
    ElMessage.warning('该图书暂无库存');
    return;
  }
  borrowDialog.visible = true;
  borrowForm.remark = '';
  // 记录当前要借的书
  selectedBook.value = book;
};

const submitBorrow = async () => {
  if (!selectedBook.value) return;

  borrowDialog.loading = true;
  try {
    await createBorrow({
      bookId: selectedBook.value.id,
      remark: borrowForm.remark,
    });

    ElMessage.success('借阅申请已提交，请等待审核');
    borrowDialog.visible = false;
    loadBooks();
  } finally {
    borrowDialog.loading = false;
  }
};

onMounted(loadBooks);
</script>

<style scoped>
.page-container { display:flex; flex-direction:column; gap: 16px; }
.toolbar { display:flex; justify-content:space-between; gap: 16px; align-items:center; flex-wrap: wrap; }
.left { display:flex; flex-direction:column; gap: 4px; }
.right { display:flex; gap: 12px; align-items:center; flex-wrap: wrap; }
.pagination { margin-top: 16px; display:flex; justify-content:flex-end; }
.tip { margin-bottom: 12px; }
:deep(.table-row) { cursor: pointer; }
</style>