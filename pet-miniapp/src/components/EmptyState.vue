<script setup lang="ts">
defineProps<{ title: string; detail?: string; action?: string }>()
defineEmits<{ action: [] }>()
</script>

<template>
  <view class="empty-state">
    <view class="empty-icon"><uni-icons type="info" :size="38" color="#EE765B" /></view>
    <text class="empty-title">{{ title }}</text>
    <text v-if="detail" class="empty-detail">{{ detail }}</text>
    <button v-if="action" class="secondary-button empty-action" @tap="$emit('action')">{{ action }}</button>
  </view>
</template>

<style scoped lang="scss">
.empty-state { display: flex; flex-direction: column; align-items: center; text-align: center; padding: 90rpx 32rpx; }
.empty-icon { width: 110rpx; height: 110rpx; display: flex; align-items: center; justify-content: center; border-radius: 50%; background: #fff0ea; }
.empty-title { margin-top: 24rpx; font-size: 31rpx; color: #2c2420; font-weight: 600; }
.empty-detail { margin-top: 12rpx; font-size: 25rpx; color: #7f7772; line-height: 1.5; }
.empty-action { margin-top: 28rpx; }
</style>
