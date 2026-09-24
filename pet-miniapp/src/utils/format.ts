import type { PetType } from '@/api/types'

export const heroImage = '/static/hero-boarding.webp'

export function petTypeName(type: PetType | string) {
  return ({ dog: '狗狗', cat: '猫咪', other: '其他宠物' } as Record<string, string>)[type] || '宠物'
}

export function money(value: number | string | null | undefined) {
  const amount = Number(value ?? 0)
  return Number.isFinite(amount) ? amount.toFixed(2) : '0.00'
}

export function dateOnly(value: string | null | undefined) {
  return value ? value.slice(0, 10) : '—'
}

export function orderStatusName(status: number, confirmedAt?: string | null) {
  if (status === 1 && confirmedAt) return '待入店'
  return ['待付款', '待确认', '寄养中', '已完成', '已取消'][status] || '未知状态'
}

export function firstImage(images?: string[] | null) {
  return images?.find(Boolean) || heroImage
}

export function showError(error: unknown) {
  const message = error instanceof Error ? error.message : '操作失败，请稍后重试'
  uni.showToast({ title: message.slice(0, 28), icon: 'none' })
}
