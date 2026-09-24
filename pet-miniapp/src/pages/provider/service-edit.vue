<script setup lang="ts">
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { serviceApi } from '@/api'
import type { BoardingServiceForm, PetType } from '@/api/types'
import { showError } from '@/utils/format'
const id = ref('')
const saving = ref(false)
const imagesText = ref('')
const form = reactive<BoardingServiceForm>({ name: '', price: 0, petType: 'dog', capacity: 1, description: '', images: [] })
const types: { value: PetType; label: string }[] = [{ value: 'dog', label: '狗狗' }, { value: 'cat', label: '猫咪' }, { value: 'other', label: '其他' }]
async function load() { try { const service = await serviceApi.getMine(id.value); Object.assign(form, service); imagesText.value = service.images.join('\n') } catch (error) { showError(error) } }
async function save() {
  if (!form.name.trim() || Number(form.price) <= 0 || Number(form.capacity) < 1) { uni.showToast({ title: '请填写名称、价格和容量', icon: 'none' }); return }
  saving.value = true
  try { const payload = { ...form, name: form.name.trim(), price: Number(form.price), capacity: Number(form.capacity), images: imagesText.value.split(/\r?\n/).map((s) => s.trim()).filter(Boolean) }; if (id.value) await serviceApi.update(id.value, payload); else await serviceApi.create(payload); uni.showToast({ title: '保存成功', icon: 'success' }); uni.navigateBack() }
  catch (error) { showError(error) } finally { saving.value = false }
}
onLoad((options) => { id.value = String(options?.id || ''); if (id.value) load() })
</script>
<template><view class="screen page-pad content"><text class="page-title">{{ id ? '编辑服务' : '新建服务' }}</text><text class="page-subtitle">清楚介绍寄养内容与接待能力</text>
  <text class="field-label">服务名称 *</text><input v-model="form.name" class="field-input" maxlength="120" placeholder="例如家庭式狗狗寄养" />
  <text class="field-label">宠物类型 *</text><view class="select-row"><text v-for="type in types" :key="type.value" class="select-chip" :class="{active:form.petType === type.value}" @tap="form.petType=type.value">{{ type.label }}</text></view>
  <text class="field-label">每天价格（元） *</text><input v-model="form.price" class="field-input" type="digit" placeholder="例如 88" />
  <text class="field-label">接待容量（只） *</text><input v-model="form.capacity" class="field-input" type="number" placeholder="例如 5" />
  <text class="field-label">服务介绍</text><textarea v-model="form.description" class="field-textarea" maxlength="5000" placeholder="环境、活动、喂养、注意事项" />
  <text class="field-label">图片链接</text><textarea v-model="imagesText" class="field-textarea" placeholder="每行一个 HTTPS 图片地址，最多 10 张" />
  <button class="primary-button save" :disabled="saving" @tap="save">{{ saving ? '保存中…' : '保存服务' }}</button></view></template>
<style scoped lang="scss">.content { padding-top: 32rpx; padding-bottom: 60rpx; }.save { margin-top: 36rpx; }</style>
