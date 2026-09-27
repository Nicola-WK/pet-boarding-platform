import { apiRequest } from './http'
import type {
  BoardingOrder, BoardingService, BoardingServiceForm, CreateOrderForm, Id,
  LoginResult, Page, Payment, Pet, PetForm, Provider, ProviderForm,
  ProviderPrivate, Review, ReviewForm, UserProfile,
} from './types'

export const authApi = {
  login: (code: string) => apiRequest<LoginResult>({ method: 'post', url: '/auth/wechat/login', data: { code } }),
  demoLogin: (role: 'customer' | 'provider') => apiRequest<LoginResult>({ method: 'post', url: '/demo/auth/login', data: { role } }),
  profile: () => apiRequest<UserProfile>({ method: 'get', url: '/users/profile' }),
}

export const providerApi = {
  list: (params: { keyword?: string; page?: number; size?: number } = {}) =>
    apiRequest<Page<Provider>>({ method: 'get', url: '/providers', params }),
  get: (id: Id) => apiRequest<Provider>({ method: 'get', url: `/providers/${id}` }),
  services: (id: Id, page = 1) =>
    apiRequest<Page<BoardingService>>({ method: 'get', url: `/providers/${id}/services`, params: { page } }),
  reviews: (id: Id, page = 1) =>
    apiRequest<Page<Review>>({ method: 'get', url: `/providers/${id}/reviews`, params: { page } }),
  mine: () => apiRequest<ProviderPrivate>({ method: 'get', url: '/provider/me' }),
  apply: (form: ProviderForm) => apiRequest<ProviderPrivate>({ method: 'post', url: '/provider/apply', data: form }),
  update: (form: ProviderForm) => apiRequest<ProviderPrivate>({ method: 'put', url: '/provider/me', data: form }),
}

export const serviceApi = {
  list: (params: { petType?: string; keyword?: string; minPrice?: number; maxPrice?: number; page?: number; size?: number } = {}) =>
    apiRequest<Page<BoardingService>>({ method: 'get', url: '/services', params }),
  get: (id: Id) => apiRequest<BoardingService>({ method: 'get', url: `/services/${id}` }),
  reviews: (id: Id, page = 1) =>
    apiRequest<Page<Review>>({ method: 'get', url: `/services/${id}/reviews`, params: { page } }),
  mine: () => apiRequest<Page<BoardingService>>({ method: 'get', url: '/provider/services' }),
  getMine: (id: Id) => apiRequest<BoardingService>({ method: 'get', url: `/provider/services/${id}` }),
  create: (form: BoardingServiceForm) => apiRequest<BoardingService>({ method: 'post', url: '/provider/services', data: form }),
  update: (id: Id, form: BoardingServiceForm) => apiRequest<BoardingService>({ method: 'put', url: `/provider/services/${id}`, data: form }),
  setActive: (id: Id, active: boolean) =>
    apiRequest<BoardingService>({ method: 'post', url: `/provider/services/${id}/status`, data: { active } }),
}

export const petApi = {
  list: () => apiRequest<Pet[]>({ method: 'get', url: '/pets' }),
  get: (id: Id) => apiRequest<Pet>({ method: 'get', url: `/pets/${id}` }),
  create: (form: PetForm) => apiRequest<Pet>({ method: 'post', url: '/pets', data: form }),
  update: (id: Id, form: PetForm) => apiRequest<Pet>({ method: 'put', url: `/pets/${id}`, data: form }),
  remove: (id: Id) => apiRequest<null>({ method: 'delete', url: `/pets/${id}` }),
}

export const orderApi = {
  create: (form: CreateOrderForm) => apiRequest<BoardingOrder>({ method: 'post', url: '/orders', data: form }),
  list: (status?: number, page = 1) =>
    apiRequest<Page<BoardingOrder>>({ method: 'get', url: '/orders', params: { status, page } }),
  get: (id: Id) => apiRequest<BoardingOrder>({ method: 'get', url: `/orders/${id}` }),
  cancel: (id: Id) => apiRequest<BoardingOrder>({ method: 'post', url: `/orders/${id}/cancel` }),
  providerList: (status?: number) =>
    apiRequest<Page<BoardingOrder>>({ method: 'get', url: '/provider/orders', params: { status } }),
  confirm: (id: Id) => apiRequest<BoardingOrder>({ method: 'post', url: `/provider/orders/${id}/confirm` }),
  start: (id: Id) => apiRequest<BoardingOrder>({ method: 'post', url: `/provider/orders/${id}/start` }),
  complete: (id: Id) => apiRequest<BoardingOrder>({ method: 'post', url: `/provider/orders/${id}/complete` }),
}

export const paymentApi = {
  createDemo: (orderId: Id) =>
    apiRequest<Payment>({ method: 'post', url: `/demo/payments/orders/${orderId}` }),
  completeDemo: (outTradeNo: string) =>
    apiRequest<Payment>({ method: 'post', url: `/demo/payments/${encodeURIComponent(outTradeNo)}/complete` }),
}

export const reviewApi = {
  create: (orderId: Id, form: ReviewForm) =>
    apiRequest<Review>({ method: 'post', url: `/orders/${orderId}/review`, data: form }),
  mine: (orderId: Id) => apiRequest<Review>({ method: 'get', url: `/orders/${orderId}/review` }),
}
