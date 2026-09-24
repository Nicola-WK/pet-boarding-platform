<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { providerApi, serviceApi } from '@/api'
import type { Provider, PetType } from '@/api/types'
import ProviderCard from '@/components/ProviderCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import TabBar from '@/components/TabBar.vue'
import { showError } from '@/utils/format'
import { openPage } from '@/utils/navigation'

const keyword = ref('')
const petType = ref<PetType | ''>('')
const priceIndex = ref(0)
const providers = ref<Provider[]>([])
const prices = ref<Record<string, number>>({})
const page = ref(1)
const hasMore = ref(false)
const loading = ref(false)
const failed = ref(false)
const priceOptions = [
  { label: '不限价格' },
  { label: '¥100以下', maxPrice: 100 },
  { label: '¥100–200', minPrice: 100, maxPrice: 200 },
  { label: '¥200以上', minPrice: 200 },
]
const typeOptions: { label: string; value: PetType | '' }[] = [
  { label: '全部', value: '' }, { label: '狗狗寄养', value: 'dog' },
  { label: '猫咪寄养', value: 'cat' }, { label: '其他宠物', value: 'other' },
]

async function load(reset = true) {
  if (loading.value) return
  loading.value = true
  failed.value = false
  if (reset) { page.value = 1; providers.value = []; prices.value = {} }
  try {
    const filtered = Boolean(petType.value || priceIndex.value)
    if (filtered) {
      const result = await serviceApi.list({
        petType: petType.value || undefined,
        keyword: keyword.value.trim() || undefined,
        minPrice: priceOptions[priceIndex.value].minPrice,
        maxPrice: priceOptions[priceIndex.value].maxPrice,
        page: page.value, size: 20,
      })
      const ids = [...new Set(result.items.map((item) => item.providerId))]
      const found = await Promise.allSettled(ids.map((id) => providerApi.get(id)))
      const detail = found.flatMap((item) => item.status === 'fulfilled' ? [item.value] : [])
      providers.value = [...providers.value, ...detail.filter((item) => !providers.value.some((old) => old.id === item.id))]
      for (const item of result.items) {
        const old = prices.value[item.providerId]
        prices.value[item.providerId] = old === undefined ? Number(item.price) : Math.min(old, Number(item.price))
      }
      hasMore.value = page.value * 20 < result.total
    } else {
      const result = await providerApi.list({ keyword: keyword.value.trim() || undefined, page: page.value, size: 10 })
      providers.value = [...providers.value, ...result.items]
      hasMore.value = page.value * 10 < result.total
      const results = await Promise.allSettled(result.items.map((provider) => providerApi.services(provider.id)))
      results.forEach((item, index) => {
        if (item.status === 'fulfilled' && item.value.items.length) {
          prices.value[result.items[index].id] = Math.min(...item.value.items.map((service) => Number(service.price)))
        }
      })
    }
  } catch (error) {
    failed.value = true
    showError(error)
  } finally {
    loading.value = false
  }
}

function selectType(value: PetType | '') { petType.value = value; load() }
function selectPrice(index: number) { priceIndex.value = index; load() }
function loadMore() { if (hasMore.value && !loading.value) { page.value += 1; load(false) } }

onLoad((options) => {
  keyword.value = typeof options?.keyword === 'string' ? options.keyword : ''
  const type = options?.petType
  petType.value = type === 'dog' || type === 'cat' || type === 'other' ? type : ''
  load()
})
onReachBottom(loadMore)
</script>

<template>
  <view class="screen screen-with-tabs">
    <view class="page-pad list-content">
      <text class="page-title">寄养服务</text>
      <text class="page-subtitle">让毛孩子在你不在的日子里，也被温柔照顾</text>
      <view class="search-box">
        <uni-icons type="search" :size="20" color="#8A817B" />
        <input v-model="keyword" class="search-input" placeholder="搜索商家或服务关键词" confirm-type="search" @confirm="load()" />
        <text class="search-action" @tap="load()">搜索</text>
      </view>
      <scroll-view scroll-x class="filter-scroll" :show-scrollbar="false">
        <view class="filter-row">
          <view v-for="option in typeOptions" :key="option.label" class="select-chip" :class="{ active: petType === option.value }" @tap="selectType(option.value)">{{ option.label }}</view>
        </view>
      </scroll-view>
      <scroll-view scroll-x class="filter-scroll price-filter" :show-scrollbar="false">
        <view class="filter-row">
          <view v-for="(option, index) in priceOptions" :key="option.label" class="price-chip" :class="{ active: priceIndex === index }" @tap="selectPrice(index)">{{ option.label }}</view>
        </view>
      </scroll-view>
      <view v-if="loading && !providers.length" class="loading-text">正在查找寄养服务…</view>
      <EmptyState v-else-if="failed && !providers.length" title="暂时无法加载" detail="请检查网络后重试" action="重新加载" @action="load()" />
      <EmptyState v-else-if="!providers.length" title="没有找到合适的商家" detail="试试其他宠物类型或价格条件" />
      <view v-else class="provider-list"><ProviderCard v-for="provider in providers" :key="provider.id" :provider="provider" :starting-price="prices[provider.id]" @open="openPage('pages/provider/detail', { id: provider.id })" /></view>
      <text v-if="loading && providers.length" class="load-more">正在加载…</text>
      <text v-else-if="hasMore" class="load-more" @tap="loadMore">加载更多</text>
    </view>
    <TabBar current="boarding" />
  </view>
</template>

<style scoped lang="scss">
.list-content { padding-top: 42rpx; }
.search-box { display: flex; align-items: center; gap: 14rpx; height: 88rpx; margin-top: 30rpx; padding: 0 18rpx 0 24rpx; border-radius: 999rpx; background: #fff3ef; }
.search-input { min-width: 0; flex: 1; height: 100%; font-size: 26rpx; }
.search-action { padding: 14rpx 22rpx; border-radius: 999rpx; background: #ee765b; color: white; font-size: 24rpx; }
.filter-scroll { width: 100%; margin-top: 24rpx; white-space: nowrap; }
.filter-row { display: inline-flex; gap: 12rpx; padding-right: 12rpx; }
.price-filter { margin-top: 14rpx; }
.price-chip { padding: 13rpx 24rpx; border-radius: 999rpx; color: #7f7772; border: 1rpx solid #efe8e3; background: #fff; font-size: 24rpx; }
.price-chip.active { color: #ee765b; border-color: #ee765b; background: #fff0ea; }
.provider-list { display: flex; flex-direction: column; gap: 18rpx; margin-top: 28rpx; }
.load-more { display: block; padding: 34rpx; text-align: center; color: #ee765b; font-size: 25rpx; }
</style>
