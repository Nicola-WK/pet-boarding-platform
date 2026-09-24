<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { orderApi, petApi, serviceApi } from '@/api'
import type { BoardingService, Pet } from '@/api/types'
import EmptyState from '@/components/EmptyState.vue'
import { money, petTypeName, showError } from '@/utils/format'
import { ensureLogin, openPage } from '@/utils/navigation'

const service = ref<BoardingService | null>(null)
const pets = ref<Pet[]>([])
const petIndex = ref(0)
const startDate = ref('')
const endDate = ref('')
const remark = ref('')
const saving = ref(false)
const loading = ref(true)
const today = new Date().toISOString().slice(0, 10)
const eligiblePets = computed(() => pets.value.filter((pet) => pet.type === service.value?.petType))
const days = computed(() => {
  if (!startDate.value || !endDate.value) return 0
  return Math.max(0, Math.round((Date.parse(endDate.value) - Date.parse(startDate.value)) / 86400000))
})
async function load(id: string) {
  if (!ensureLogin()) return
  try { [service.value, pets.value] = await Promise.all([serviceApi.get(id), petApi.list()]) }
  catch (error) { showError(error) }
  finally { loading.value = false }
}
async function submit() {
  if (!service.value || !eligiblePets.value[petIndex.value]) return
  if (!startDate.value || days.value <= 0) { uni.showToast({ title: '请选择正确的寄养日期', icon: 'none' }); return }
  saving.value = true
  try {
    const order = await orderApi.create({ petId: eligiblePets.value[petIndex.value].id, serviceId: service.value.id, startDate: startDate.value, endDate: endDate.value, remark: remark.value.trim() })
    uni.showToast({ title: '预约已创建', icon: 'success' })
    openPage('pages/order/detail', { id: order.id })
  } catch (error) { showError(error) }
  finally { saving.value = false }
}
onLoad((options) => { if (options?.serviceId) load(String(options.serviceId)); else loading.value = false })
</script>

<template>
  <view class="screen page-pad form-screen">
    <text class="page-title">预约寄养</text><text class="page-subtitle">选择宠物与日期，确认后生成订单</text>
    <view v-if="loading" class="loading-text">正在加载…</view>
    <EmptyState v-else-if="!service" title="服务暂时不可预约" />
    <template v-else>
      <view class="surface summary"><text class="service-name">{{ service.name }}</text><text class="muted">{{ petTypeName(service.petType) }} · ¥{{ money(service.price) }}/天</text></view>
      <text class="field-label">寄养宠物</text>
      <view v-if="!eligiblePets.length" class="soft-surface hint">还没有{{ petTypeName(service.petType) }}档案，先添加宠物吧。<button class="text-button" @tap="openPage('pages/pet/edit')">新增宠物</button></view>
      <picker v-else :range="eligiblePets" range-key="name" :value="petIndex" @change="petIndex = Number($event.detail.value)"><view class="field-picker">{{ eligiblePets[petIndex]?.name }} <text>⌄</text></view></picker>
      <text class="field-label">入住日期</text><picker mode="date" :start="today" :value="startDate" @change="startDate = String($event.detail.value)"><view class="field-picker">{{ startDate || '请选择入住日期' }} <text>⌄</text></view></picker>
      <text class="field-label">离店日期</text><picker mode="date" :start="startDate || today" :value="endDate" @change="endDate = String($event.detail.value)"><view class="field-picker">{{ endDate || '请选择离店日期' }} <text>⌄</text></view></picker>
      <text class="field-label">照护备注（选填）</text><textarea v-model="remark" class="field-textarea" maxlength="500" placeholder="例如饮食习惯、用药提醒" />
      <view class="space-between total"><text>预计 {{ days }} 天</text><text class="price">¥{{ money(days * Number(service.price)) }}</text></view>
      <view class="soft-surface notice">订单金额由服务单价和寄养天数计算；提交前请核对日期。</view>
      <button class="primary-button submit" :disabled="saving || !eligiblePets.length" @tap="submit">{{ saving ? '正在提交…' : '确认预约' }}</button>
    </template>
  </view>
</template>

<style scoped lang="scss">
.form-screen { padding-top: 32rpx; padding-bottom: 60rpx; }.summary { margin-top: 30rpx; padding: 28rpx; display: flex; flex-direction: column; gap: 12rpx; }.service-name { font-size: 32rpx; font-weight: 700; }.hint,.notice { padding: 22rpx; color: #6c5c53; font-size: 25rpx; line-height: 1.6; }.total { margin: 38rpx 0 20rpx; font-size: 28rpx; }.submit { margin-top: 30rpx; }
</style>
