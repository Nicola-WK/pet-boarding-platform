<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { reviewApi } from '@/api'
import { showError } from '@/utils/format'
const orderId = ref('')
const score = ref(5)
const content = ref('')
const imagesText = ref('')
const saving = ref(false)
const existing = ref(false)
async function check() { try { await reviewApi.mine(orderId.value); existing.value = true } catch { existing.value = false } }
async function save() { if (!orderId.value || existing.value) return; saving.value = true; try { await reviewApi.create(orderId.value, { score: score.value, content: content.value.trim(), images: imagesText.value.split(/\r?\n/).map((s) => s.trim()).filter(Boolean) }); uni.showToast({ title: '评价已发布', icon: 'success' }); uni.navigateBack() } catch (error) { showError(error) } finally { saving.value = false } }
onLoad((options) => { orderId.value = String(options?.orderId || ''); if (orderId.value) check() })
</script>
<template><view class="screen page-pad content"><text class="page-title">评价服务</text><text class="page-subtitle">分享真实感受，帮助更多宠物主人</text><view v-if="existing" class="soft-surface existing">这笔订单已经评价过了，感谢你的分享。</view><template v-else>
  <text class="field-label">服务评分</text><view class="stars"><text v-for="n in 5" :key="n" :class="{selected:n<=score}" @tap="score=n">★</text></view>
  <text class="field-label">寄养体验</text><textarea v-model="content" class="field-textarea" maxlength="2000" placeholder="商家如何照顾你的宠物？" />
  <text class="field-label">评价图片链接（选填）</text><textarea v-model="imagesText" class="field-textarea" placeholder="每行一个 HTTPS 图片地址，最多 10 张" />
  <button class="primary-button save" :disabled="saving" @tap="save">{{ saving ? '发布中…' : '发布评价' }}</button>
</template></view></template>
<style scoped lang="scss">.content { padding-top: 32rpx; padding-bottom: 60rpx; }.stars { display: flex; gap: 16rpx; color: #ded6d0; font-size: 62rpx; }.stars .selected { color: #f3a34d; }.save { margin-top: 36rpx; }.existing { margin-top: 36rpx; padding: 28rpx; color: #7f7772; }</style>
