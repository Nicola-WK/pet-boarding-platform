<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { providerApi } from '@/api'
import type { BoardingService, Provider, Review } from '@/api/types'
import EmptyState from '@/components/EmptyState.vue'
import { firstImage, money, petTypeName, showError } from '@/utils/format'
import { openPage } from '@/utils/navigation'

const provider = ref<Provider | null>(null)
const services = ref<BoardingService[]>([])
const reviews = ref<Review[]>([])
const loading = ref(true)
const demoEnabled = import.meta.env.VITE_DEMO_MODE_ENABLED === 'true'
const failed = ref(false)

async function load(id: string) {
  loading.value = true
  failed.value = false
  try {
    provider.value = await providerApi.get(id)
    const [serviceResult, reviewResult] = await Promise.all([providerApi.services(id), providerApi.reviews(id)])
    services.value = serviceResult.items
    reviews.value = reviewResult.items
  } catch (error) {
    failed.value = true
    showError(error)
  } finally { loading.value = false }
}
onLoad((options) => { if (options?.id) load(String(options.id)); else loading.value = false })
</script>

<template>
  <view class="screen detail-screen">
    <view v-if="loading" class="loading-text">正在加载商家资料…</view>
    <EmptyState v-else-if="failed || !provider" title="商家暂时无法查看" detail="请返回后重试" />
    <template v-else>
      <swiper class="cover" :indicator-dots="provider.images.length > 1" circular>
        <swiper-item v-for="(image, index) in provider.images.length ? provider.images : [firstImage()]" :key="index"><image :src="image" mode="aspectFill" class="cover-image" /></swiper-item>
      </swiper>
      <view class="page-pad detail-body">
        <text class="page-title">{{ provider.name }}</text>
        <text class="address">⌖ {{ provider.address }}</text>
        <view class="surface intro"><text class="section-title">关于我们</text><text class="body-text">{{ provider.description || '商家暂未填写介绍。' }}</text><text class="phone">咨询电话：{{ provider.contactPhone }}</text></view>
        <view class="section-gap space-between"><text class="section-title">寄养服务</text><text class="muted small">{{ services.length }} 项</text></view>
        <EmptyState v-if="!services.length" title="暂无可预约服务" detail="商家正在准备新的寄养服务" />
        <view v-for="service in services" :key="service.id" class="surface service-card" @tap="openPage('pages/order/create', { serviceId: service.id })">
          <image :src="firstImage(service.images)" class="service-image" mode="aspectFill" />
          <view class="service-info"><text class="service-name">{{ service.name }}</text><text class="muted small">{{ petTypeName(service.petType) }} · 可接待 {{ service.capacity }} 只</text><text class="price">¥{{ money(service.price) }}<text class="price-unit">/天</text></text></view>
          <text class="arrow">›</text>
        </view>
        <view class="section-gap space-between"><text class="section-title">{{ demoEnabled ? '演示评价' : '真实评价' }}</text><text class="muted small">{{ reviews.length }} 条</text></view>
        <EmptyState v-if="!reviews.length" title="还没有评价" detail="完成寄养后，用户可以在这里分享体验" />
        <view v-for="review in reviews" :key="review.id" class="surface review-card"><text class="stars">{{ '★'.repeat(review.score) }}{{ '☆'.repeat(5 - review.score) }}</text><text class="muted small">{{ review.createdAt?.slice(0, 10) }}</text><text class="body-text">{{ review.content || '用户留下了评分。' }}</text></view>
      </view>
    </template>
  </view>
</template>

<style scoped lang="scss">
.detail-screen { padding-bottom: 40rpx; }.cover { height: 430rpx; background: #f7e9df; }.cover-image { width: 100%; height: 100%; }.detail-body { padding-top: 28rpx; }.address { display: block; margin-top: 12rpx; color: #7f7772; font-size: 26rpx; }.intro { padding: 28rpx; margin-top: 30rpx; }.body-text { display: block; margin-top: 16rpx; color: #514943; font-size: 27rpx; line-height: 1.65; white-space: pre-wrap; }.phone { display: block; margin-top: 22rpx; color: #ee765b; font-size: 25rpx; }.small { font-size: 24rpx; }.service-card { display: flex; align-items: center; gap: 20rpx; padding: 16rpx; margin-top: 16rpx; }.service-image { width: 144rpx; height: 144rpx; border-radius: 16rpx; flex-shrink: 0; }.service-info { flex: 1; display: flex; flex-direction: column; gap: 12rpx; min-width: 0; }.service-name { color: #2c2420; font-size: 29rpx; font-weight: 700; }.arrow { color: #ee765b; font-size: 42rpx; }.review-card { padding: 24rpx; margin-top: 16rpx; }.stars { color: #f3a34d; font-size: 28rpx; margin-right: 16rpx; }
</style>
