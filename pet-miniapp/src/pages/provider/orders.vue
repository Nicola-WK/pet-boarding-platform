<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { orderApi } from '@/api'
import type { BoardingOrder } from '@/api/types'
import EmptyState from '@/components/EmptyState.vue'
import { dateOnly, money, orderStatusName, showError } from '@/utils/format'

const orders = ref<BoardingOrder[]>([])
const loading = ref(true)
const busy = ref('')
async function load() { loading.value = true; try { orders.value = (await orderApi.providerList()).items } catch (error) { showError(error) } finally { loading.value = false } }
async function act(order: BoardingOrder, action: 'confirm' | 'start' | 'complete') { busy.value = order.id; try { await orderApi[action](order.id); await load() } catch (error) { showError(error) } finally { busy.value = '' } }
onShow(load)
</script>
<template><view class="screen page-pad content"><text class="page-title">商家订单</text><text class="page-subtitle">按订单进度安排接待与照护</text><view v-if="loading" class="loading-text">正在加载…</view><EmptyState v-else-if="!orders.length" title="暂无订单" detail="有用户预约后会显示在这里" />
  <view v-for="order in orders" :key="order.id" class="surface order-card"><view class="space-between"><text class="number">{{ order.orderNo }}</text><text class="status-text">{{ orderStatusName(order.status, order.confirmedAt) }}</text></view><text class="line">{{ dateOnly(order.startAt) }} 至 {{ dateOnly(order.endAt) }} · {{ order.boardingDays }} 天</text><view class="space-between"><text class="price">¥{{ money(order.totalAmount) }}</text><button v-if="order.status === 1 && !order.confirmedAt" class="secondary-button action" :disabled="busy === order.id" @tap="act(order, 'confirm')">确认订单</button><button v-else-if="order.status === 1 && order.confirmedAt" class="secondary-button action" :disabled="busy === order.id" @tap="act(order, 'start')">开始寄养</button><button v-else-if="order.status === 2" class="secondary-button action" :disabled="busy === order.id" @tap="act(order, 'complete')">完成服务</button></view></view>
</view></template>
<style scoped lang="scss">.content { padding-top: 32rpx; padding-bottom: 60rpx; }.order-card { padding: 26rpx; margin-top: 18rpx; }.number { color: #7f7772; font-size: 23rpx; }.line { display: block; margin: 22rpx 0; font-size: 27rpx; }.action { min-height: 68rpx; font-size: 24rpx; }</style>
