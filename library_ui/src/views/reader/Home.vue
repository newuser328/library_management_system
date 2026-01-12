<template>
  <div class="notice-page">
    <div class="page-container">
      <el-card class="hero panel panel-a" shadow="never">
        <div class="hero-row">
          <div>
            <div class="hero-title">公告栏</div>
            <div class="hero-sub">这里展示管理员发布的最新公告</div>
          </div>
          <el-button :icon="Refresh" @click="load" :loading="loading">刷新</el-button>
        </div>
      </el-card>

      <el-card class="panel panel-b" shadow="never">
        <el-skeleton v-if="loading" :rows="6" animated />

        <template v-else>
          <div v-if="list.length === 0" class="empty">
            <el-empty description="暂无公告" />
          </div>

          <div v-else class="notices">
            <el-card v-for="n in list" :key="n.id" class="notice" shadow="never">
              <div class="notice-header">
                <div class="notice-title clickable" @click="goDetail(n.id)">{{ n.title }}</div>
                <div class="notice-time">{{ formatTime(n.createdAt) }}</div>
              </div>

              <el-carousel
                v-if="getNoticeImages(n).length > 0"
                class="notice-carousel clickable"
                height="240px"
                :interval="4500"
                indicator-position="outside"
                @click="goDetail(n.id)"
              >
                <el-carousel-item v-for="(u, idx) in getNoticeImages(n)" :key="u + '_' + idx">
                  <el-image
                    class="notice-image"
                    :src="getImageUrl(u)"
                    fit="cover"
                    :preview-src-list="getNoticeImages(n).map(getImageUrl)"
                    preview-teleported
                  />
                </el-carousel-item>
              </el-carousel>

              <div class="notice-content" v-html="sanitizeHtml(n.content)"></div>
            </el-card>
          </div>
        </template>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Refresh } from '@element-plus/icons-vue';
import { listPublishedNotices } from '@/api/notices';

const router = useRouter();

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

const getImageUrl = (url) => {
  if (url && typeof url === 'string') {
    if (url.startsWith('/api/')) return 'http://localhost:8080' + url;
    return url;
  }
  return '';
};

const getNoticeImages = (n) => {
  const arr = Array.isArray(n?.imageUrls) ? n.imageUrls : (n?.imageUrl ? [n.imageUrl] : []);
  return arr.filter((s) => s && String(s).trim()).slice(0, 3);
};

const formatTime = (t) => {
  if (!t) return '';
  return String(t).replace('T', ' ').slice(0, 19);
};

const goDetail = (noticeId) => {
  router.push(`/reader/notices/${noticeId}`);
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

onMounted(load);
</script>

<style scoped>
.notice-page {
  padding: 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(79,70,229,0.10), rgba(6,182,212,0.10));
}

.hero-row { display:flex; justify-content:space-between; align-items:center; }
.hero-title { font-weight: 900; font-size: 18px; }
.hero-sub { color: #6b7280; margin-top: 2px; }

.notices { display:flex; flex-direction:column; gap: 12px; }
.notice { border: 1px solid rgba(229,231,235,0.9); }
.notice-header { display:flex; justify-content:space-between; align-items:center; gap: 12px; }
.notice-title { font-weight: 800; font-size: 16px; }
.notice-time { color: #6b7280; font-size: 12px; }

.notice-carousel { width: 100%; margin-top: 12px; }
.notice-image {
  width: 100%;
  height: 240px;
  border-radius: 10px;
}

.clickable { cursor: pointer; }

.notice-content { margin-top: 12px; line-height: 1.7; color: #111827; }
</style>
