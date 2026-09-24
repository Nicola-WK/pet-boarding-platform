import { useAuthStore } from '@/store/auth'

export function openPage(path: string, params: Record<string, string | number | undefined> = {}) {
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join('&')
  uni.navigateTo({ url: `/${path}${query ? `?${query}` : ''}` })
}

export function openMain(path: string) {
  uni.reLaunch({ url: `/${path}` })
}

export function ensureLogin() {
  if (useAuthStore().token) return true
  uni.showToast({ title: '请先登录', icon: 'none' })
  openPage('pages/user/index')
  return false
}
