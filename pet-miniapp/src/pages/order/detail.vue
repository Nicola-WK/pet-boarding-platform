<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { orderApi, paymentApi, petApi, providerApi, serviceApi } from '@/api'
import type { BoardingOrder, Pet, Provider, BoardingService } from '@/api/types'
import EmptyState from '@/components/EmptyState.vue'
import { dateOnly, money, orderStatusName, showError } from '@/utils/format'
import { openPage } from '@/utils/navigation'

const id = ref('')
const order = ref<BoardingOrder | null>(null)
const pet = ref<Pet | null>(null)
const provider = ref<Provider | null>(null)
const service = ref<BoardingService | null>(null)
const loading = ref(true)
const busy = ref(false)
const demoEnabled = import.meta.env.VITE_DEMO_PAYMENT_ENABLED === 'true'
async function load() {
  if (!id.value) return
  loading.value = true
  try {
    order.value = await orderApi.get(id.value)
    const current = order.value
    const details = await Promise.allSettled([petApi.get(current.petId), providerApi.get(current.providerId), serviceApi.get(current.serviceId)])
    pet.value = details[0].status === 'fulfilled' ? details[0].value as Pet : null
    provider.value = details[1].status === 'fulfilled' ? details[1].value as Provider : null
    service.value = details[2].status === 'fulfilled' ? details[2].value as BoardingService : null
  } catch (error) { showError(error) }
  finally { loading.value = false }
}
async function cancel() {
  if (!order.value) return
  const confirmed = await new Promise<boolean>((resolve) => uni.showModal({ title: '取消订单', content: '确定取消这笔预约吗？', success: (result) => resolve(result.confirm) }))
  if (!confirmed) return
  busy.value = true
  try { await orderApi.cancel(order.value.id); await load() } catch (error) { showError(error) } finally { busy.value = false }
}
async function demoPay() {
  if (!order.value || !demoEnabled) return
  busy.value = true
  try { const payment = await paymentApi.createDemo(order.value.id); await paymentApi.completeDemo(payment.outTradeNo); await load(); uni.showToast({ title: '模拟支付完成', icon: 'success' }) }
  catch (error) { showError(error) } finally { busy.value = false }
}
onLoad((options) => { id.value = String(options?.id || '') })
onShow(load)
</script>
<template>
  <view class="screen page-pad detail-page"><view v-if="loading" class="loading-text">正在加载订单…</view><EmptyState v-else-if="!order" title="订单暂时无法查看" />
    <template v-else><view class="soft-surface status-panel"><text class="status-title">{{ orderStatusName(order.status, order.confirmedAt) }}</text><text class="muted">订单号 {{ order.orderNo }}</text></view>
      <view class="surface card"><text class="section-title">寄养信息</text><text class="info">服务：{{ service?.name || order.serviceId }}</text><text class="info">商家：{{ provider?.name || order.providerId }}</text><text class="info">宠物：{{ pet?.name || order.petId }}</text><text class="info">日期：{{ dateOnly(order.startAt) }} 至 {{ dateOnly(order.endAt) }}</text><text class="info">寄养 {{ order.boardingDays }} 天</text><text v-if="order.remark" class="info">备注：{{ order.remark }}</text></view>
      <view class="surface card"><view class="space-between"><text class="section-title">订单金额</text><text class="price">¥{{ money(order.totalAmount) }}</text></view><text class="muted info">单价 ¥{{ money(order.unitPrice) }}/天</text></view>
      <view v-if="order.status === 0" class="soft-surface payment-note">{{ demoEnabled ? '演示支付已开启。本按钮仅模拟支付完成，不涉及真实资金。' : '当前未接入真实支付。可联系项目维护者开启本地演示支付。' }}</view>
      <button v-if="order.status === 0 && demoEnabled" class="primary-button action" :disabled="busy" @tap="demoPay">模拟付款</button>
      <button v-if="order.status === 0 || order.status === 1" class="secondary-button action" :disabled="busy" @tap="cancel">取消订单</button>
      <button v-if="order.status === 3" class="primary-button action" @tap="openPage('pages/review/create', { orderId: order.id })">评价服务</button>
    </template></view>
</template>
<style scoped lang="scss">
.detail-page { padding-top: 26rpx; padding-bottom: 60rpx; }.status-panel { padding: 32rpx; display: flex; flex-direction: column; gap: 12rpx; }.status-title { color: #ee765b; font-size: 42rpx; font-weight: 700; }.card { padding: 28rpx; margin-top: 20rpx; }.info { display: block; margin-top: 18rpx; font-size: 27rpx; }.payment-note { margin-top: 24rpx; padding: 20rpx; color: #7f7772; font-size: 24rpx; line-height: 1.5; }.action { margin-top: 20rpx; }
</style>
