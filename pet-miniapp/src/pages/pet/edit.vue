<script setup lang="ts">
import { reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { petApi } from '@/api'
import type { PetForm, PetType } from '@/api/types'
import { showError } from '@/utils/format'

const id = ref('')
const saving = ref(false)
const form = reactive<PetForm>({ name: '', type: 'dog', gender: 0, breed: '', avatar: '', birthday: '', weight: null, vaccine: '', character: '', diet: '', remark: '' })
const petTypes: { value: PetType; label: string }[] = [{ value: 'dog', label: '狗狗' }, { value: 'cat', label: '猫咪' }, { value: 'other', label: '其他' }]
const genderOptions = [{ value: 0, label: '未知' }, { value: 1, label: '公' }, { value: 2, label: '母' }]
async function load() { try { Object.assign(form, await petApi.get(id.value)); form.birthday = form.birthday?.slice(0, 10) || '' } catch (error) { showError(error) } }
async function save() {
  if (!form.name.trim()) { uni.showToast({ title: '请填写宠物名字', icon: 'none' }); return }
  saving.value = true
  try {
    const payload: PetForm = { ...form, name: form.name.trim(), birthday: form.birthday ? `${form.birthday}T00:00:00` : null, weight: form.weight ? Number(form.weight) : null }
    if (id.value) await petApi.update(id.value, payload); else await petApi.create(payload)
    uni.showToast({ title: '保存成功', icon: 'success' }); uni.navigateBack()
  } catch (error) { showError(error) } finally { saving.value = false }
}
onLoad((options) => { id.value = String(options?.id || ''); if (id.value) load() })
</script>
<template><view class="screen page-pad form-screen"><text class="page-title">{{ id ? '编辑宠物' : '添加宠物' }}</text><text class="page-subtitle">记录习惯，让商家更懂你的毛孩子</text>
  <text class="field-label">名字 *</text><input v-model="form.name" class="field-input" maxlength="80" placeholder="宠物怎么称呼？" />
  <text class="field-label">宠物类型 *</text><view class="select-row"><text v-for="type in petTypes" :key="type.value" class="select-chip" :class="{active:form.type === type.value}" @tap="form.type = type.value">{{ type.label }}</text></view>
  <text class="field-label">品种</text><input v-model="form.breed" class="field-input" maxlength="80" placeholder="例如柯基、英短" />
  <text class="field-label">性别</text><view class="select-row"><text v-for="item in genderOptions" :key="item.value" class="select-chip" :class="{active:form.gender === item.value}" @tap="form.gender = item.value">{{ item.label }}</text></view>
  <text class="field-label">生日</text><picker mode="date" :value="form.birthday || ''" @change="form.birthday = String($event.detail.value)"><view class="field-picker">{{ form.birthday || '请选择生日' }} <text>⌄</text></view></picker>
  <text class="field-label">体重（kg）</text><input v-model="form.weight" class="field-input" type="digit" placeholder="例如 6.5" />
  <text class="field-label">头像链接（HTTPS）</text><input v-model="form.avatar" class="field-input" maxlength="1024" placeholder="选填，粘贴图片链接" />
  <text class="field-label">疫苗情况</text><textarea v-model="form.vaccine" class="field-textarea" maxlength="2000" placeholder="疫苗名称及接种时间" />
  <text class="field-label">性格特点</text><textarea v-model="form.character" class="field-textarea" maxlength="2000" placeholder="例如怕生、爱玩" />
  <text class="field-label">饮食习惯</text><textarea v-model="form.diet" class="field-textarea" maxlength="2000" placeholder="喂食频率、忌口等" />
  <text class="field-label">其他备注</text><textarea v-model="form.remark" class="field-textarea" maxlength="2000" placeholder="需要照护者留意的事项" />
  <button class="primary-button save" :disabled="saving" @tap="save">{{ saving ? '保存中…' : '保存宠物档案' }}</button>
</view></template>
<style scoped lang="scss">.form-screen { padding-top: 34rpx; padding-bottom: 60rpx; }.save { margin-top: 38rpx; }</style>
