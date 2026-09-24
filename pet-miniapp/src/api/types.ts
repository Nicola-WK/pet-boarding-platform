export type Id = string

export interface ApiEnvelope<T> { code: number; message: string; data: T }
export interface Page<T> { items: T[]; total: number; page: number; size: number }

export interface UserProfile { id: Id; nickname: string | null; avatar: string | null; phone: string | null }
export interface LoginResult { token: string; tokenType: string; expiresIn: number; user: UserProfile }

export interface Provider {
  id: Id; name: string; contactPhone: string; address: string
  latitude: number | null; longitude: number | null; description: string | null
  images: string[]
}
export interface ProviderPrivate extends Provider {
  ownerUserId: Id; licenseImage: string | null; auditStatus: number
  auditReason: string | null; auditedAt: string | null
  createdAt: string; updatedAt: string
}
export interface ProviderForm {
  name: string; contactPhone: string; address: string; description?: string
  latitude?: number | null; longitude?: number | null; images?: string[]
  licenseImage?: string | null
}

export interface BoardingService {
  id: Id; providerId: Id; name: string; price: number; petType: PetType
  capacity: number; description: string | null; images: string[]; status: number
  createdAt: string; updatedAt: string
}
export type PetType = 'dog' | 'cat' | 'other'
export interface BoardingServiceForm {
  name: string; price: number; petType: PetType; capacity: number
  description?: string; images?: string[]
}

export interface Pet {
  id: Id; name: string; avatar: string | null; type: PetType; breed: string | null
  gender: number; birthday: string | null; weight: number | null; vaccine: string | null
  character: string | null; diet: string | null; remark: string | null
}
export interface PetForm {
  name: string; avatar?: string | null; type: PetType; breed?: string | null
  gender?: number; birthday?: string | null; weight?: number | null
  vaccine?: string | null; character?: string | null; diet?: string | null
  remark?: string | null
}

export interface BoardingOrder {
  id: Id; orderNo: string; userId: Id; petId: Id; providerId: Id; serviceId: Id
  startAt: string; endAt: string; boardingDays: number; unitPrice: number
  totalAmount: number; status: number; remark: string | null
  expiresAt: string | null; paidAt: string | null; confirmedAt: string | null
  completedAt: string | null; canceledAt: string | null; createdAt: string
}
export interface CreateOrderForm {
  petId: Id; serviceId: Id; startDate: string; endDate: string; remark?: string
}
export interface Payment {
  id: Id; orderId: Id; outTradeNo: string; amount: number
  status: number; paidAt: string | null; createdAt: string
}
export interface Review {
  id: Id; providerId: Id; serviceId: Id; score: number; content: string | null
  images: string[]; createdAt: string
}
export interface ReviewForm { score: number; content?: string; images?: string[] }
