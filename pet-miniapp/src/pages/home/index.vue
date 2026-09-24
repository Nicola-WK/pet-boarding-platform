<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { providerApi } from '@/api'
import type { Provider } from '@/api/types'
import ProviderCard from '@/components/ProviderCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import TabBar from '@/components/TabBar.vue'
import { heroImage, showError } from '@/utils/format'
import { openPage } from '@/utils/navigation'

const keyword = ref('')
const providers = ref<Provider[]>([])
const prices = ref<Record<string, number>>({})
const loading = ref(false)
const failed = ref(false)

async function load() {
  loading.value = true
  failed.value = false
  try {
    const page = await providerApi.list({ page: 1, size: 6 })
    providers.value = page.items
    const results = await Promise.allSettled(page.items.map((provider) => providerApi.services(provider.id)))
    prices.value = Object.fromEntries(results.flatMap((result, index) => {
      if (result.status !== 'fulfilled' || !result.value.items.length) return []
      return [[page.items[index].id, Math.min(...result.value.items.map((item) => Number(item.price)))]]
    }))
  } catch (error) {
    failed.value = true
    showError(error)
  } finally {
    loading.value = false
  }
}

function search() {
  openPage('pages/boarding/list', { keyword: keyword.value.trim() })
}

onShow(load)
</script>

<template>
  <view class="screen screen-with-tabs">
    <view class="page-pad home-top">
      <text class="brand">宠爱管家<text class="brand-paw">✿</text></text>
      <text class="page-subtitle">让每一份牵挂，都有温暖的归宿</text>
      <view class="search-box">
        <uni-icons type="search" :size="20" color="#8A817B" />
        <input v-model="keyword" class="search-input" placeholder="搜索寄养商家或服务" confirm-type="search" @confirm="search" />
      </view>
      <view class="category-row">
        <view class="category category-dog" @tap="openPage('pages/boarding/list', { petType: 'dog' })"><text class="category-icon">🐶</text><text>狗狗寄养</text></view>
        <view class="category category-cat" @tap="openPage('pages/boarding/list', { petType: 'cat' })"><text class="category-icon">🐱</text><text>猫咪寄养</text></view>
        <view class="category category-other" @tap="openPage('pages/boarding/list', { petType: 'other' })"><text class="category-icon">🐰</text><text>其他宠物</text></view>
      </view>
      <view class="hero-wrap" @tap="openPage('pages/boarding/list')">
        <image :src="heroImage" class="hero-image" mode="aspectFill" />
        <view class="hero-caption"><text>好的寄养</text><text>是一种陪伴</text></view>
      </view>
      <view class="space-between section-gap"><text class="section-title">精选寄养</text><text class="section-link" @tap="openPage('pages/boarding/list')">查看全部 ›</text></view>
      <view v-if="loading" class="loading-text">正在寻找温暖的寄养空间…</view>
      <EmptyState v-else-if="failed" title="暂时无法加载商家" detail="请检查网络后重试" action="重新加载" @action="load" />
      <EmptyState v-else-if="!providers.length" title="还没有入驻商家" detail="稍后再来看看新的寄养空间" />
      <view v-else class="provider-list"><ProviderCard v-for="provider in providers" :key="provider.id" :provider="provider" :starting-price="prices[provider.id]" @open="openPage('pages/provider/detail', { id: provider.id })" /></view>
    </view>
    <TabBar current="home" />
  </view>
</template>

<style scoped lang="scss">
.home-top { padding-top: 46rpx; }
.brand { display: block; color: #2c2420; font-size: 54rpx; font-weight: 800; letter-spacing: 1rpx; }
.brand-paw { margin-left: 8rpx; font-size: 37rpx; color: #ee765b; }
.search-box { display: flex; align-items: center; gap: 16rpx; height: 88rpx; margin-top: 34rpx; padding: 0 26rpx; border-radius: 999rpx; background: #fff3ef; }
.search-input { flex: 1; height: 100%; color: #2c2420; font-size: 27rpx; }
.category-row { display: flex; gap: 14rpx; margin-top: 24rpx; }
.category { flex: 1; min-width: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; height: 154rpx; gap: 10rpx; border-radius: 22rpx; font-size: 22rpx; font-weight: 600; }
.category-dog { background: #fff0e5; }.category-cat { background: #fff0ef; }.category-other { background: #fff4e6; }
.category-icon { font-size: 54rpx; line-height: 1; }
.hero-wrap { position: relative; height: 350rpx; margin-top: 26rpx; overflow: hidden; border-radius: 24rpx; background: #ead9ca; }
.hero-image { width: 100%; height: 100%; }
.hero-caption { position: absolute; left: 26rpx; top: 26rpx; display: flex; flex-direction: column; color: #2c2420; font-size: 34rpx; line-height: 1.45; font-weight: 700; text-shadow: 0 1rpx 8rpx #fffdf9; }
.section-link { font-size: 24rpx; color: #ee765b; }
.provider-list { display: flex; flex-direction: column; gap: 18rpx; margin-top: 22rpx; }
</style>
