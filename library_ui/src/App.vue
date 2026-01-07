<template>
  <router-view v-slot="{ Component, route }">
    <transition name="fade-slide" mode="out-in">
      <component :is="Component" :key="route.fullPath" />
    </transition>
  </router-view>
</template>

<style>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 180ms ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
