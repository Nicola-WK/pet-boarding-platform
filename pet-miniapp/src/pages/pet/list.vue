<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { petApi } from '@/api'
import type { Pet } from '@/api/types'
import EmptyState from '@/components/EmptyState.vue'
import { petTypeName, showError } from '@/utils/format'
import { ensureLogin, openPage } from '@/utils/navigation'

const pets = ref<Pet[]>([])
const loading = ref(true)
async function load() { if (!ensureLogin()) return; loading.value = true; try { pets.value = await petApi.list() } catch (error) { showError(error) } finally { loading.value = false } }
async function remove(pet: Pet) {
  const confirmed = await new Promise<boolean>((resolve) => uni.showModal({ title: '删除宠物档案', content: `确定删除“${pet.name}”吗？`, success: (result) => resolve(result.confirm) }))
  if (!confirmed) return
  try { await petApi.remove(pet.id); await load() } catch (error) { showError(error) }
}
onShow(load)
</script>
<template><view class="screen page-pad content"><view class="space-between"><text class="page-title">我的宠物</text><text class="add" @tap="openPage('pages/pet/edit')">＋ 新增</text></view><text class="page-subtitle">照护信息越完整，寄养越安心</text>
  <view v-if="loading" class="loading-text">正在加载宠物档案…</view><EmptyState v-else-if="!pets.length" title="还没有宠物档案" detail="添加爱宠，开始安心预约" action="添加宠物" @action="openPage('pages/pet/edit')" />
  <view v-for="pet in pets" :key="pet.id" class="surface pet-card"><image v-if="pet.avatar" :src="pet.avatar" class="avatar" mode="aspectFill" /><view v-else class="avatar placeholder">{{ pet.type === 'dog' ? '🐶' : pet.type === 'cat' ? '🐱' : '🐾' }}</view><view class="pet-info"><text class="pet-name">{{ pet.name }}</text><text class="muted pet-meta">{{ petTypeName(pet.type) }} · {{ pet.breed || '品种未填' }}</text></view><text class="edit" @tap="openPage('pages/pet/edit', { id: pet.id })">编辑</text><text class="delete" @tap="remove(pet)">删除</text></view>
</view></template>
<style scoped lang="scss">.content { padding-top: 34rpx; }.add,.edit { color: #ee765b; font-size: 26rpx; }.pet-card { display: flex; align-items: center; gap: 18rpx; padding: 20rpx; margin-top: 18rpx; }.avatar { width: 100rpx; height: 100rpx; border-radius: 50%; flex-shrink: 0; }.placeholder { background: #fff0ea; display: flex; align-items: center; justify-content: center; font-size: 48rpx; }.pet-info { flex: 1; display: flex; flex-direction: column; gap: 10rpx; min-width: 0; }.pet-name { font-size: 30rpx; font-weight: 700; }.pet-meta,.delete { font-size: 23rpx; }.delete { color: #aaa09a; }</style>
