<script setup lang="ts">
import { reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { providerApi, serviceApi } from '@/api'
import type { BoardingService, ProviderForm, ProviderPrivate } from '@/api/types'
import EmptyState from '@/components/EmptyState.vue'
import { money, petTypeName, showError } from '@/utils/format'
import { ensureLogin, openPage } from '@/utils/navigation'

const mine = ref<ProviderPrivate | null>(null)
const services = ref<BoardingService[]>([])
const loading = ref(true)
const saving = ref(false)
const form = reactive<ProviderForm>({ name: '', contactPhone: '', address: '', description: '', images: [], licenseImage: '' })
const imagesText = ref('')
async function load() {
  if (!ensureLogin()) return
  loading.value = true
  try {
    mine.value = await providerApi.mine()
    Object.assign(form, mine.value)
    imagesText.value = mine.value.images.join('\n')
    if (mine.value.auditStatus === 1) services.value = (await serviceApi.mine()).items
  } catch (error) {
    if (error instanceof Error && /404|未找到|不存在|尚未申请/.test(error.message)) mine.value = null
    else showError(error)
  } finally { loading.value = false }
}
async function save() {
  if (!form.name.trim() || !form.contactPhone.trim() || !form.address.trim()) { uni.showToast({ title: '请填写名称、电话和地址', icon: 'none' }); return }
  saving.value = true
  try {
    const payload = { ...form, name: form.name.trim(), contactPhone: form.contactPhone.trim(), address: form.address.trim(), images: imagesText.value.split(/\r?\n/).map((s) => s.trim()).filter(Boolean), licenseImage: form.licenseImage?.trim() || null }
    mine.value = mine.value ? await providerApi.update(payload) : await providerApi.apply(payload)
    uni.showToast({ title: '资料已提交', icon: 'success' }); await load()
  } catch (error) { showError(error) } finally { saving.value = false }
}
async function toggle(service: BoardingService) { try { await serviceApi.setActive(service.id, service.status !== 1); await load() } catch (error) { showError(error) } }
onShow(load)
</script>
<template><view class="screen page-pad content"><text class="page-title">商家中心</text><text class="page-subtitle">用细致服务，赢得每一份信任</text>
  <view v-if="loading" class="loading-text">正在加载商家资料…</view><template v-else>
    <view v-if="mine" class="soft-surface status"><text>审核状态：{{ mine.auditStatus === 1 ? '已通过' : mine.auditStatus === 2 ? '未通过' : '审核中' }}</text><text v-if="mine.auditReason" class="muted">原因：{{ mine.auditReason }}</text></view>
    <text class="field-label">商家名称 *</text><input v-model="form.name" class="field-input" maxlength="120" placeholder="寄养空间名称" />
    <text class="field-label">联系电话 *</text><input v-model="form.contactPhone" class="field-input" maxlength="32" placeholder="8–32 位数字或合法电话格式" />
    <text class="field-label">地址 *</text><input v-model="form.address" class="field-input" maxlength="255" placeholder="详细地址" />
    <text class="field-label">商家介绍</text><textarea v-model="form.description" class="field-textarea" maxlength="5000" placeholder="介绍环境与照护经验" />
    <text class="field-label">商家图片链接</text><textarea v-model="imagesText" class="field-textarea" placeholder="每行一个 HTTPS 图片地址，最多 10 张" />
    <text class="field-label">资质图片链接</text><input v-model="form.licenseImage" class="field-input" maxlength="1024" placeholder="HTTPS 图片地址" />
    <button class="primary-button save" :disabled="saving" @tap="save">{{ saving ? '提交中…' : mine ? '保存商家资料' : '申请成为商家' }}</button>
    <template v-if="mine?.auditStatus === 1"><view class="space-between section-gap"><text class="section-title">服务管理</text><text class="link" @tap="openPage('pages/provider/service-edit')">＋ 新建服务</text></view>
      <EmptyState v-if="!services.length" title="还没有服务" detail="新建一个服务，开始接待宠物" />
      <view v-for="service in services" :key="service.id" class="surface service-row"><view class="service-main"><text class="service-name">{{ service.name }}</text><text class="muted">{{ petTypeName(service.petType) }} · ¥{{ money(service.price) }}/天</text></view><text class="link" @tap="openPage('pages/provider/service-edit', { id: service.id })">编辑</text><text class="link" @tap="toggle(service)">{{ service.status === 1 ? '下架' : '上架' }}</text></view>
      <button class="secondary-button orders" @tap="openPage('pages/provider/orders')">查看商家订单</button>
    </template>
  </template></view></template>
<style scoped lang="scss">.content { padding-top: 32rpx; padding-bottom: 60rpx; }.status { margin-top: 24rpx; padding: 22rpx; display: flex; flex-direction: column; gap: 8rpx; font-size: 26rpx; }.save,.orders { margin-top: 30rpx; }.link { color: #ee765b; font-size: 25rpx; }.service-row { display: flex; align-items: center; gap: 18rpx; padding: 24rpx; margin-top: 14rpx; }.service-main { display: flex; flex: 1; flex-direction: column; gap: 10rpx; font-size: 24rpx; }.service-name { font-size: 28rpx; font-weight: 700; }</style>
