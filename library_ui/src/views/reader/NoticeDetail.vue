<template>
  <div class="notice-detail-page">
    <div class="topbar">
      <el-button @click="goBack">返回</el-button>
      <div class="top-title">{{ current?.title || '公告详情' }}</div>
    </div>

    <div class="layout">
      <el-card class="side" shadow="never">
        <div class="side-title">其他公告</div>
        <el-scrollbar height="520px">
          <div class="side-list">
            <div
              v-for="n in others"
              :key="n.id"
              class="side-item"
              :class="{ active: String(n.id) === String(id) }"
              @click="goNotice(n.id)"
            >
              <div class="s-title">{{ n.title }}</div>
              <div class="s-time">{{ formatTime(n.createdAt) }}</div>
            </div>
          </div>
        </el-scrollbar>
      </el-card>

      <el-card class="main" shadow="never">
        <div class="main-title">{{ current?.title }}</div>

        <el-carousel
          v-if="images.length > 0"
          class="carousel"
          height="320px"
          :interval="4500"
          indicator-position="outside"
        >
          <el-carousel-item v-for="(u, idx) in images" :key="u + '_' + idx">
            <el-image
              class="img"
              :src="getImageUrl(u)"
              fit="cover"
              :preview-src-list="images.map(getImageUrl)"
              preview-teleported
            />
          </el-carousel-item>
        </el-carousel>

        <div class="content" v-html="sanitizeHtml(current?.content || '')"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { listPublishedNotices } from '@/api/notices';

const route = useRoute();
const router = useRouter();

const id = computed(() => route.params.id);

const loading = ref(false);
const list = ref([]);

const load = async () => {
  loading.value = true;
  try {
    list.value = await listPublishedNotices();
  } finally {
    loading.value = false;
  }
};

const current = computed(() => {
  return list.value.find((n) => String(n.id) === String(id.value));
});

const others = computed(() => {
  return list.value.filter((n) => String(n.id) !== String(id.value));
});

const images = computed(() => {
  const n = current.value;
  const arr = Array.isArray(n?.imageUrls) ? n.imageUrls : (n?.imageUrl ? [n.imageUrl] : []);
  return arr.filter((s) => s && String(s).trim()).slice(0, 3);
});

const getImageUrl = (url) => {
  if (url && typeof url === 'string') {
    if (url.startsWith('/api/')) return 'http://localhost:8080' + url;
    return url;
  }
  return '';
};

const formatTime = (t) => {
  if (!t) return '';
  return String(t).replace('T', ' ').slice(0, 19);
};

const sanitizeHtml = (html) => {
  if (!html) return '';
  const container = document.createElement('div');
  container.innerHTML = String(html);

  const blockedTags = new Set(['SCRIPT', 'IFRAME', 'OBJECT', 'EMBED', 'LINK', 'META', 'STYLE']);
  const walker = document.createTreeWalker(container, NodeFilter.SHOW_ELEMENT, null);

  const toRemove = [];
  while (walker.nextNode()) {
    const el = walker.currentNode;
    if (blockedTags.has(el.tagName)) {
      toRemove.push(el);
      continue;
    }

    [...el.attributes].forEach((attr) => {
      const name = attr.name.toLowerCase();
      const value = (attr.value || '').toLowerCase();
      if (name.startsWith('on')) {
        el.removeAttribute(attr.name);
      }
      if ((name === 'href' || name === 'src') && value.startsWith('javascript:')) {
        el.removeAttribute(attr.name);
      }
    });
  }

  toRemove.forEach((el) => el.remove());
  return container.innerHTML;
};

const goBack = () => {
  router.back();
};

const goNotice = (nid) => {
  router.push(`/reader/notices/${nid}`);
};

watch(
  () => id.value,
  async () => {
    if (list.value.length === 0) {
      await load();
    }
  },
  { immediate: true }
);

onMounted(load);
</script>

<style scoped>
.notice-detail-page {
  padding: 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(79,70,229,0.10), rgba(6,182,212,0.10));
  min-height: calc(100vh - 32px);
}

.topbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.top-title {
  flex: 1;
  text-align: center;
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
}

.layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 20px;
  margin-top: 16px;
}

.side {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.side-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #333;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.side-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.side-item {
  padding: 12px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #eee;
  cursor: pointer;
  transition: all 0.3s ease;
}

.side-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #c7d2fe;
}

.side-item.active {
  border-color: #4f46e5;
  background-color: #eef2ff;
}

.s-title {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
  font-size: 14px;
}

.s-time {
  color: #6b7280;
  font-size: 12px;
}

.main {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.main-title {
  font-size: 24px;
  font-weight: 800;
  color: #111827;
  margin-bottom: 20px;
  line-height: 1.3;
}

.carousel {
  width: 100%;
  margin: 20px 0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.content {
  line-height: 1.8;
  color: #374151;
  font-size: 15px;
  padding: 8px 0;
}

.content :deep(p) {
  margin: 1em 0;
}

.content :deep(a) {
  color: #4f46e5;
  text-decoration: none;
  font-weight: 500;
}

.content :deep(a:hover) {
  text-decoration: underline;
}

.content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 16px 0;
  display: block;
}

.content :deep(h1),
.content :deep(h2),
.content :deep(h3) {
  margin: 1.5em 0 0.8em;
  color: #111827;
  line-height: 1.3;
}

.content :deep(h1) {
  font-size: 1.8em;
  font-weight: 800;
}

.content :deep(h2) {
  font-size: 1.5em;
  font-weight: 700;
}

.content :deep(h3) {
  font-size: 1.2em;
  font-weight: 600;
}

.content :deep(ul),
.content :deep(ol) {
  padding-left: 1.5em;
  margin: 0.8em 0;
}

.content :deep(li) {
  margin: 0.4em 0;
}

.content :deep(blockquote) {
  border-left: 4px solid #e5e7eb;
  padding: 0.5em 1em;
  margin: 1em 0;
  color: #4b5563;
  font-style: italic;
  background-color: #f9fafb;
  border-radius: 0 8px 8px 0;
}

.content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1em 0;
  font-size: 0.9em;
  box-shadow: 0 0 0 1px #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.content :deep(th),
.content :deep(td) {
  padding: 12px 15px;
  text-align: left;
  border: 1px solid #e5e7eb;
}

.content :deep(th) {
  background-color: #f3f4f6;
  font-weight: 600;
  color: #111827;
}

.content :deep(tr:nth-child(even)) {
  background-color: #f9fafb;
}

.content :deep(tr:hover) {
  background-color: #f0f2f5;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .layout {
    grid-template-columns: 1fr;
  }
  
  .side {
    margin-bottom: 20px;
  }
  
  .topbar {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .top-title {
    margin-top: 8px;
  }
}

@media (max-width: 768px) {
  .main {
    padding: 16px;
  }
  
  .main-title {
    font-size: 20px;
  }
  
  .carousel {
    margin: 12px 0;
  }
  
  .content {
    font-size: 14px;
  }
}
</style>