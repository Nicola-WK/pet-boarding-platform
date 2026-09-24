<script setup lang="ts">
import { openMain } from '@/utils/navigation'

defineProps<{ current: 'home' | 'boarding' | 'orders' | 'user' }>()

const tabs = [
  { key: 'home', label: '首页', icon: 'home', path: 'pages/home/index' },
  { key: 'boarding', label: '寄养', icon: 'location', path: 'pages/boarding/list' },
  { key: 'orders', label: '订单', icon: 'calendar', path: 'pages/order/list' },
  { key: 'user', label: '我的', icon: 'person', path: 'pages/user/index' },
] as const
</script>

<template>
  <view class="tab-bar">
    <view v-for="tab in tabs" :key="tab.key" class="tab-item" :class="{ active: current === tab.key }" @tap="current !== tab.key && openMain(tab.path)">
      <uni-icons :type="tab.icon" :size="25" :color="current === tab.key ? '#EE765B' : '#8A817B'" />
      <text>{{ tab.label }}</text>
    </view>
  </view>
</template>

<style scoped lang="scss">
.tab-bar { position: fixed; left: 0; right: 0; bottom: 0; z-index: 20; display: flex; padding: 12rpx 0 calc(12rpx + env(safe-area-inset-bottom)); background: #fffdf9; border-top: 1rpx solid #efe8e3; }
.tab-item { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 5rpx; color: #8a817b; font-size: 22rpx; min-height: 88rpx; }
.tab-item.active { color: #ee765b; font-weight: 600; }
</style>
