import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { LoginResult, UserProfile } from '@/api/types'

const TOKEN_KEY = 'pet_boarding_token'
const PROFILE_KEY = 'pet_boarding_profile'

export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const profile = ref<UserProfile | null>(null)

  function restore() {
    try {
      token.value = String(uni.getStorageSync(TOKEN_KEY) || '')
      profile.value = (uni.getStorageSync(PROFILE_KEY) || null) as UserProfile | null
    } catch {
      token.value = ''
      profile.value = null
    }
  }

  function setSession(result: LoginResult) {
    token.value = result.token
    profile.value = result.user
    uni.setStorageSync(TOKEN_KEY, result.token)
    uni.setStorageSync(PROFILE_KEY, result.user)
  }

  function setProfile(value: UserProfile) {
    profile.value = value
    uni.setStorageSync(PROFILE_KEY, value)
  }

  function clear() {
    token.value = ''
    profile.value = null
    uni.removeStorageSync(TOKEN_KEY)
    uni.removeStorageSync(PROFILE_KEY)
  }

  return { token, profile, restore, setSession, setProfile, clear }
})
