<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { authApi } from '@/api'
import TabBar from '@/components/TabBar.vue'
import { useAuthStore } from '@/store/auth'
import { showError } from '@/utils/format'
import { openPage } from '@/utils/navigation'

const auth = useAuthStore()
const loggingIn = ref(false)
const demoEnabled = import.meta.env.VITE_DEMO_MODE_ENABLED === 'true'
async function demoLogin(role: 'customer' | 'provider') {
  loggingIn.value = true
  try {
    auth.setSession(await authApi.demoLogin(role))
    uni.showToast({ title: '演示登录成功', icon: 'success' })
  } catch (error) { showError(error) } finally { loggingIn.value = false }
}
async function login() {
  loggingIn.value = true
  try {
    // #ifdef MP-WEIXIN
    if (uni.getAccountInfoSync().miniProgram.appId === 'touristappid') {
      uni.showToast({ title: '请先配置小程序 AppID', icon: 'none' })
      return
    }
    const result = await uni.login({ provider: 'weixin' })
    if (!result.code) throw new Error('微信登录未返回授权码')
    auth.setSession(await authApi.login(result.code))
    uni.showToast({ title: '登录成功', icon: 'success' })
    // #endif
    // #ifdef H5
    uni.showToast({ title: '请在微信开发者工具中登录', icon: 'none' })
    // #endif
  } catch (error) { showError(error) } finally { loggingIn.value = false }
}
async function refresh() { if (!auth.token) return; try { auth.setProfile(await authApi.profile()) } catch { auth.clear() } }
function goProtected(path: string) { if (!auth.token) { uni.showToast({ title: '请先登录', icon: 'none' }); return }; openPage(path) }
function logout() { auth.clear(); uni.showToast({ title: '已退出登录', icon: 'none' }) }
onShow(refresh)
</script>
<template><view class="screen screen-with-tabs"><view class="page-pad content"><text class="page-title">我的</text><text class="page-subtitle">和毛孩子一起，放心出发</text>
  <view class="surface profile"><view class="avatar">{{ auth.profile?.nickname?.slice(0, 1) || '🐾' }}</view><view class="profile-text"><text class="profile-name">{{ auth.profile?.nickname || (auth.token ? '宠物主人' : '欢迎来到宠爱管家') }}</text><text class="muted small">{{ auth.token ? '已登录' : '登录后管理宠物、订单与商家资料' }}</text></view></view>
  <button v-if="!auth.token && !demoEnabled" class="primary-button login" :disabled="loggingIn" @tap="login">{{ loggingIn ? '登录中…' : '微信一键登录' }}</button>
  <view v-if="demoEnabled" class="demo-panel"><text class="muted small">本机作品演示，不使用微信身份或真实支付</text><button class="secondary-button demo-button" :disabled="loggingIn" @tap="demoLogin('customer')">体验宠物主人</button><button class="secondary-button demo-button" :disabled="loggingIn" @tap="demoLogin('provider')">体验寄养商家</button></view>
  <view class="menu surface"><view class="menu-item" @tap="goProtected('pages/pet/list')"><text>🐾　我的宠物</text><text>›</text></view><view class="divider" /><view class="menu-item" @tap="goProtected('pages/order/list')"><text>📋　我的订单</text><text>›</text></view><view class="divider" /><view class="menu-item" @tap="goProtected('pages/provider/manage')"><text>🏠　商家中心</text><text>›</text></view></view>
  <button v-if="auth.token" class="secondary-button logout" @tap="logout">退出登录</button>
</view><TabBar current="user" /></view></template>
<style scoped lang="scss">.content { padding-top: 42rpx; }.profile { display: flex; align-items: center; gap: 22rpx; padding: 28rpx; margin-top: 36rpx; }.avatar { width: 100rpx; height: 100rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: #fff0ea; color: #ee765b; font-size: 42rpx; }.profile-text { display: flex; flex-direction: column; gap: 12rpx; }.profile-name { font-size: 30rpx; font-weight: 700; }.small { font-size: 24rpx; }.login { margin-top: 26rpx; }.demo-panel { margin-top: 24rpx; display: flex; flex-direction: column; gap: 12rpx; }.demo-button { width: 100%; }.menu { margin-top: 30rpx; padding: 0 24rpx; }.menu-item { height: 108rpx; display: flex; align-items: center; justify-content: space-between; color: #2c2420; font-size: 28rpx; }.menu-item text:last-child { color: #b3aaa4; font-size: 40rpx; }.logout { margin-top: 36rpx; }</style>
