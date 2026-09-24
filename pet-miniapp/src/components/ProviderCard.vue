<script setup lang="ts">
import type { Provider } from '@/api/types'
import { firstImage, money } from '@/utils/format'

defineProps<{ provider: Provider; startingPrice?: number | null }>()
defineEmits<{ open: [] }>()
</script>

<template>
  <view class="provider-card" @tap="$emit('open')">
    <image class="provider-image" :src="firstImage(provider.images)" mode="aspectFill" />
    <view class="provider-info">
      <text class="provider-name">{{ provider.name }}</text>
      <text class="provider-address"><uni-icons type="location" :size="13" color="#8A817B" /> {{ provider.address }}</text>
      <text class="provider-description">{{ provider.description || '商家暂未填写介绍' }}</text>
      <view class="provider-bottom">
        <text v-if="startingPrice != null" class="price">¥{{ money(startingPrice) }}<text class="price-unit">/天起</text></text>
        <text v-else class="provider-more">查看服务</text>
        <view class="arrow-circle"><uni-icons type="right" :size="17" color="#FFFFFF" /></view>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.provider-card { display: flex; gap: 20rpx; min-height: 238rpx; padding: 12rpx; border-radius: 22rpx; background: white; border: 1rpx solid #efe8e3; box-sizing: border-box; }
.provider-image { flex: none; width: 210rpx; height: 214rpx; border-radius: 16rpx; background: #fff0ea; }
.provider-info { min-width: 0; flex: 1; display: flex; flex-direction: column; padding: 10rpx 4rpx 6rpx 0; }
.provider-name { font-size: 31rpx; font-weight: 700; color: #2c2420; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.provider-address { margin-top: 10rpx; font-size: 23rpx; color: #7f7772; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.provider-description { margin-top: 9rpx; font-size: 23rpx; color: #7f7772; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.provider-bottom { display: flex; align-items: flex-end; justify-content: space-between; margin-top: auto; }
.provider-more { color: #ee765b; font-size: 24rpx; font-weight: 600; }
.arrow-circle { display: flex; align-items: center; justify-content: center; width: 45rpx; height: 45rpx; border-radius: 50%; background: #ee765b; }
</style>
