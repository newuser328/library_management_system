<template>
  <div v-if="book">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="书名" :span="2">{{ book.title }}</el-descriptions-item>
      <el-descriptions-item label="作者">{{ book.author }}</el-descriptions-item>
      <el-descriptions-item label="ISBN">{{ book.isbn }}</el-descriptions-item>
      <el-descriptions-item label="分类">{{ formatCategories(book) }}</el-descriptions-item>
      <el-descriptions-item label="出版社">{{ book.publisher }}</el-descriptions-item>
      <el-descriptions-item label="库存" :span="2">
        <el-tag :type="book.availableCount > 0 ? 'success' : 'danger'">
          可借 {{ book.availableCount }} / 总数 {{ book.totalCount }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="简介" :span="2">
        <div style="white-space: pre-wrap">{{ book.description || '-' }}</div>
      </el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup>
const props = defineProps({
  book: { type: Object, default: null },
});

const formatCategories = (row) => {
  const arr = row?.categories;
  if (Array.isArray(arr) && arr.length > 0) {
    return arr.map((c) => c.name).join(' / ');
  }
  if (row?.category) return row.category;
  return '未分类';
};
</script>

