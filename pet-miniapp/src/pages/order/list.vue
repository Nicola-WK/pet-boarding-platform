<script setup lang="ts">
import { ref } from 'vue'
import { onShow, onReachBottom } from '@dcloudio/uni-app'
import { orderApi } from '@/api'
import type { BoardingOrder } from '@/api/types'
import EmptyState from '@/components/EmptyState.vue'
import TabBar from '@/components/TabBar.vue'
import { dateOnly, money, orderStatusName, showError } from '@/utils/format'
import { ensureLogin, openPage } from '@/utils/navigation'

const tabs = [{ name: '全部', value: undefined }, { name: '待付款', value: 0 }, { name: '待确认', value: 1 }, { name: '寄养中', value: 2 }, { name: '已完成', value: 3 }, { name: '已取消', value: 4 }]
const active = ref(0)
const orders = ref<BoardingOrder[]>([])
const page = ref(1)
const more = ref(false)
const loading = ref(false)
const failed = ref(false)
async function load(reset = true) {
  if (!ensureLogin() || loading.value) return
  if (reset) { page.value = 1; orders.value = [] }
  loading.value = true; failed.value = false
  try { const result = await orderApi.list(tabs[active.value].value, page.value); orders.value.push(...result.items); more.value = page.value * result.size < result.total }
  catch (error) { failed.value = true; showError(error) }
  finally { loading.value = false }
}
function select(index: number) { active.value = index; load() }
onShow(() => load())
onReachBottom(() => { if (more.value && !loading.value) { page.value++; load(false) } })
</script>
<template>
  <view class="screen screen-with-tabs"><view class="page-pad content"><text class="page-title">我的订单</text><text class="page-subtitle">每一次托付，都有迹可循</text>
    <scroll-view scroll-x class="tabs"><view class="tab-row"><text v-for="(tab,index) in tabs" :key="tab.name" class="tab" :class="{ active: active === index }" @tap="select(index)">{{ tab.name }}</text></view></scroll-view>
    <view v-if="loading && !orders.length" class="loading-text">正在加载订单…</view><EmptyState v-else-if="failed && !orders.length" title="订单加载失败" action="重试" @action="load()" /><EmptyState v-else-if="!orders.length" title="还没有订单" detail="找一家合适的寄养商家吧" action="浏览寄养" @action="openPage('pages/boarding/list')" />
    <view v-for="order in orders" :key="order.id" class="surface order-card" @tap="openPage('pages/order/detail', { id: order.id })"><view class="space-between"><text class="order-number">订单 {{ order.orderNo }}</text><text class="status-text">{{ orderStatusName(order.status, order.confirmedAt) }}</text></view><view class="divider line" /><text class="muted dates">{{ dateOnly(order.startAt) }} 至 {{ dateOnly(order.endAt) }} · {{ order.boardingDays }} 天</text><view class="space-between bottom"><text class="muted">查看详情 ›</text><text class="price">¥{{ money(order.totalAmount) }}</text></view></view>
    <text v-if="more" class="more" @tap="page++; load(false)">加载更多</text>
  </view><TabBar current="orders" /></view>
</template>
<style scoped lang="scss">
.content { padding-top: 42rpx; }.tabs { margin-top: 28rpx; white-space: nowrap; }.tab-row { display: inline-flex; gap: 20rpx; }.tab { padding: 16rpx 20rpx; font-size: 25rpx; color: #7f7772; }.tab.active { color: #ee765b; font-weight: 700; border-bottom: 4rpx solid #ee765b; }.order-card { padding: 26rpx; margin-top: 18rpx; }.order-number { font-size: 24rpx; color: #7f7772; }.line { margin: 22rpx 0; }.dates { font-size: 26rpx; }.bottom { margin-top: 20rpx; font-size: 24rpx; }.more { display: block; text-align: center; padding: 30rpx; color: #ee765b; }
</style>
