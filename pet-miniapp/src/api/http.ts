import axios, { AxiosError, AxiosHeaders } from 'axios'
import type { AxiosAdapter, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/store/auth'
import type { ApiEnvelope } from './types'

function requestUrl(config: InternalAxiosRequestConfig) {
  const base = (config.baseURL || '').replace(/\/$/, '')
  const path = config.url || ''
  const url = `${base}${path.startsWith('/') ? path : `/${path}`}`
  const params = config.params as Record<string, string | number | boolean | null | undefined> | undefined
  if (!params) return url
  const query = Object.entries(params)
    .filter(([, value]) => value !== null && value !== undefined && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join('&')
  return query ? `${url}${url.includes('?') ? '&' : '?'}${query}` : url
}

const uniAdapter: AxiosAdapter = (config) => new Promise((resolve, reject) => {
  const headers = AxiosHeaders.from(config.headers).toJSON()
  const header = Object.fromEntries(
    Object.entries(headers).map(([key, value]) => [key, String(value)]),
  )
  const task = uni.request({
    url: requestUrl(config),
    method: (config.method || 'get').toUpperCase() as UniApp.RequestOptions['method'],
    data: config.data,
    header,
    timeout: config.timeout || 15000,
    success: (result) => {
      const response: AxiosResponse = {
        data: result.data,
        status: result.statusCode,
        statusText: String(result.statusCode),
        headers: result.header,
        config,
        request: task,
      }
      resolve(response)
    },
    fail: (error) => reject(new AxiosError(error.errMsg || '网络连接失败', 'ERR_NETWORK', config, task)),
  })
  config.signal?.addEventListener?.('abort', () => task.abort?.(), { once: true })
})

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  adapter: uniAdapter,
  headers: { 'Content-Type': 'application/json' },
})

client.interceptors.request.use((config) => {
  const token = useAuthStore().token
  if (token) config.headers.set('Authorization', `Bearer ${token}`)
  return config
})

export async function apiRequest<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await client.request<ApiEnvelope<T>>(config)
  if (response.status === 401) useAuthStore().clear()
  const envelope = response.data
  if (response.status < 200 || response.status >= 300 || envelope?.code !== 200) {
    throw new Error(envelope?.message || `请求失败（${response.status}）`)
  }
  return envelope.data
}
