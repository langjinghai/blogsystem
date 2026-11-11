<template>
  <div class="app-page-container app-profile-page">
    <div class="app-section-header">
      <h3 class="app-section-header__title">个人中心</h3>
    </div>

    <div class="app-card" v-loading="loading">
      <!-- 头像 & 基本信息 -->
      <div class="app-profile-page__header">
        <div class="app-profile-page__avatar-wrap" @click="triggerUpload">
          <img
            v-if="avatarPreview || userStore.avatar"
            :src="avatarPreview || userStore.avatar"
            class="app-profile-page__avatar"
            alt="avatar"
          />
          <div v-else class="app-profile-page__avatar-placeholder">
            {{ userStore.nickname.charAt(0).toUpperCase() }}
          </div>
          <div class="app-profile-page__avatar-overlay">
            📷 更换头像
          </div>
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            style="display:none"
            @change="handleFileChange"
          />
        </div>

        <div class="app-profile-page__info">
          <h2>{{ userStore.nickname }}</h2>
          <p>手机号：{{ maskedPhone }}</p>
          <div class="app-profile-page__stats">
            <div>
              <div class="stat-value">{{ profileStats.articleCount }}</div>
              <div class="stat-label">文章</div>
            </div>
            <div>
              <div class="stat-value">{{ profileStats.likeCount }}</div>
              <div class="stat-label">获赞</div>
            </div>
            <div class="stat-clickable" @click="showFollowList('followers')">
              <div class="stat-value">{{ profileStats.followerCount }}</div>
              <div class="stat-label">粉丝</div>
            </div>
            <div class="stat-clickable" @click="showFollowList('following')">
              <div class="stat-value">{{ profileStats.followingCount }}</div>
              <div class="stat-label">关注</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 编辑表单 -->
      <div style="margin-top:32px">
        <el-form label-position="top">
          <el-form-item label="昵称" class="app-form-item">
            <el-input
              v-model="editNickname"
              placeholder="请输入新昵称"
              maxlength="20"
              show-word-limit
              class="app-input"
            />
          </el-form-item>

          <div style="display:flex;gap:16px;justify-content:flex-end;margin-top:24px">
            <el-button class="app-btn--outline" @click="resetForm">重置</el-button>
            <el-button
              class="app-btn--primary"
              :loading="saving"
              :disabled="!editNickname.trim()"
              @click="handleSave"
            >
              保存修改
            </el-button>
          </div>
        </el-form>
      </div>

      <!-- 修改密码按钮 -->
      <div style="margin-top:40px; padding-top:24px; border-top:1px solid var(--border-color)">
        <el-button class="app-btn--outline" @click="showPasswordDialog = true">
          🔒 修改密码
        </el-button>
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      width="460px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-position="top"
      >
        <el-form-item label="当前密码" prop="password" class="app-form-item">
          <el-input
            v-model="passwordForm.password"
            type="password"
            placeholder="请输入当前密码"
            show-password
            class="app-input"
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword" class="app-form-item">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（4~32位）"
            show-password
            class="app-input"
          />
          <div class="password-strength" v-if="passwordForm.newPassword">
            <div class="strength-bar">
              <div class="strength-fill" :class="strengthClass" :style="{ width: strengthPercent + '%' }"></div>
            </div>
            <span class="strength-text" :class="strengthClass">{{ strengthLabel }}</span>
          </div>
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword" class="app-form-item">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            class="app-input"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button
          type="primary"
          :loading="changingPassword"
          :disabled="!passwordForm.password || !passwordForm.newPassword || !passwordForm.confirmPassword"
          @click="handleChangePassword"
        >
          确认修改
        </el-button>
      </template>
    </el-dialog>

    <!-- 粉丝/关注列表弹窗 -->
    <el-dialog
      v-model="showFollowDialog"
      :title="followDialogTitle"
      width="420px"
      destroy-on-close
    >
      <div v-loading="loadingFollowList">
        <div v-if="followList.length === 0 && !loadingFollowList" class="app-empty-state">
          <div class="app-empty-state__text">暂无数据</div>
        </div>
        <div
          v-for="user in followList"
          :key="user.id"
          class="follow-list-item"
          @click="goToUser(user.id)"
        >
          <img v-if="user.icon" :src="user.icon" class="follow-list-item__avatar" />
          <div v-else class="follow-list-item__avatar-placeholder">
            {{ (user.nickName || 'U').charAt(0).toUpperCase() }}
          </div>
          <span class="follow-list-item__name">{{ user.nickName }}</span>
        </div>
        <div v-if="followTotal > followPageSize" style="text-align:center;margin-top:16px">
          <el-pagination
            small
            layout="prev, pager, next"
            :total="followTotal"
            :page-size="followPageSize"
            :current-page="followPage"
            @current-change="onFollowPageChange"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateProfile, updatePassword } from '@/api/user'
import { uploadImage } from '@/api/article'
import { getFollowers, getFollowing, type FollowUser } from '@/api/follow'

const userStore = useUserStore()
const router = useRouter()

const fileInput = ref<HTMLInputElement>()
const avatarPreview = ref('')
const editNickname = ref('')
const saving = ref(false)
const loading = ref(false)

const profileStats = ref({
  articleCount: 0,
  likeCount: 0,
  followerCount: 0,
  followingCount: 0,
})

// ---------- 粉丝/关注列表弹窗 ----------
const showFollowDialog = ref(false)
const followDialogMode = ref<'followers' | 'following'>('followers')
const followList = ref<FollowUser[]>([])
const followTotal = ref(0)
const followPage = ref(1)
const followPageSize = 10
const loadingFollowList = ref(false)

const followDialogTitle = computed(() =>
  followDialogMode.value === 'followers' ? '粉丝列表' : '关注列表',
)

// ---------- 修改密码弹窗 ----------
const showPasswordDialog = ref(false)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  password: '',
  newPassword: '',
  confirmPassword: '',
})
const changingPassword = ref(false)

const validatePwd = (_: any, value: string, callback: any) => {
  if (!value) return callback(new Error('请输入新密码'))
  if (value.length < 4 || value.length > 32) return callback(new Error('密码长度为4~32位'))
  if (!/^\w{4,32}$/.test(value)) return callback(new Error('密码仅支持字母、数字、下划线'))
  callback()
}

const validateConfirm = (_: any, value: string, callback: any) => {
  if (!value) return callback(new Error('请再次输入新密码'))
  if (value !== passwordForm.newPassword) return callback(new Error('两次密码不一致'))
  callback()
}

const passwordRules: FormRules = {
  password: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, validator: validatePwd, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirm, trigger: 'blur' }],
}

const strength = computed(() => {
  const p = passwordForm.newPassword; let s = 0
  if (p.length >= 8) s++; if (p.length >= 12) s++
  if (/[a-z]/.test(p) && /[A-Z]/.test(p)) s++
  if (/\d/.test(p)) s++; if (/[^a-zA-Z\d]/.test(p)) s++
  if (s <= 1) return { pct: 25, label: '强度：弱', cls: 'weak' }
  if (s <= 2) return { pct: 50, label: '强度：中', cls: 'medium' }
  if (s <= 3) return { pct: 75, label: '强度：强', cls: 'strong' }
  return { pct: 100, label: '强度：极强', cls: 'very-strong' }
})
const strengthPercent = computed(() => strength.value.pct)
const strengthLabel = computed(() => strength.value.label)
const strengthClass = computed(() => strength.value.cls)

async function handleChangePassword() {
  try {
    await passwordFormRef.value!.validate()
  } catch {
    return
  }
  changingPassword.value = true
  try {
    const res = await updatePassword({
      password: passwordForm.password,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword,
    })
    if (res.success) {
      ElMessage.success('密码修改成功')
      showPasswordDialog.value = false
    } else {
      ElMessage.error(res.errorMag || '密码修改失败')
    }
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.errorMag || '网络异常')
  } finally {
    changingPassword.value = false
  }
}

const maskedPhone = computed(() => {
  const phone = userStore.userInfo?.phone || ''
  if (phone.length >= 11) {
    return phone.slice(0, 3) + '****' + phone.slice(7)
  }
  return phone
})

async function showFollowList(mode: 'followers' | 'following') {
  followDialogMode.value = mode
  followPage.value = 1
  showFollowDialog.value = true
  await loadFollowList()
}

async function loadFollowList() {
  const userId = userStore.userInfo?.id
  if (!userId) return
  loadingFollowList.value = true
  try {
    const fetcher = followDialogMode.value === 'followers' ? getFollowers : getFollowing
    const res = await fetcher(userId, followPage.value, followPageSize)
    if (res.success && res.data) {
      followList.value = res.data as FollowUser[]
      followTotal.value = res.total || 0
    }
  } catch {
    ElMessage.error('加载列表失败')
  } finally {
    loadingFollowList.value = false
  }
}

async function onFollowPageChange(page: number) {
  followPage.value = page
  await loadFollowList()
}

function goToUser(userId: number) {
  showFollowDialog.value = false
  router.push(`/user/${userId}`)
}

onMounted(async () => {
  loading.value = true
  await userStore.loadUserInfo()
  if (userStore.userInfo) {
    editNickname.value = userStore.userInfo.nickName || ''
    profileStats.value.articleCount = userStore.userInfo.articleCount || 0
    profileStats.value.likeCount = userStore.userInfo.likeCount || 0
    profileStats.value.followerCount = userStore.userInfo.followerCount || 0
    profileStats.value.followingCount = userStore.userInfo.followingCount || 0
  }
  loading.value = false
})

onUnmounted(() => {
  if (avatarPreview.value) URL.revokeObjectURL(avatarPreview.value)
})

function triggerUpload() {
  fileInput.value?.click()
}

async function handleFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  const oldUrl = avatarPreview.value
  avatarPreview.value = URL.createObjectURL(file)
  if (oldUrl) URL.revokeObjectURL(oldUrl)
  try {
    const res = await uploadImage(file)
    if (res.success && res.data) {
      const iconUrl = res.data as string
      await updateProfile({ icon: iconUrl })
      if (userStore.userInfo) {
        userStore.userInfo.icon = iconUrl
      }
      ElMessage.success('头像更新成功')
    } else {
      ElMessage.warning(res.errorMag || '头像上传失败')
    }
  } catch {
    ElMessage.warning('头像上传失败')
  }
}

function resetForm() {
  editNickname.value = userStore.userInfo?.nickName || ''
  avatarPreview.value = ''
}
async function handleSave() {
  const name = editNickname.value.trim()
  if (!name) {
    ElMessage.warning('昵称不能为空')
    return
  }
  saving.value = true
  try {
    const res = await updateProfile({ nickName: name })
    if (res.success) {
      if (userStore.userInfo) {
        userStore.userInfo.nickName = name
      }
      ElMessage.success('信息已更新')
    } else {
      ElMessage.error(res.errorMag || '保存失败')
    }
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.errorMag || '网络异常')
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.password-strength { display: flex; align-items: center; gap: 10px; margin-top: 8px; padding: 0 4px; }
.strength-bar { flex: 1; height: 4px; background: var(--bg-input); border-radius: 2px; overflow: hidden; }
.strength-fill {
  height: 100%; border-radius: 2px; transition: width .4s ease, background .4s ease;
  &.weak { background: #ef4444; }
  &.medium { background: #f59e0b; }
  &.strong { background: #10b981; }
  &.very-strong { background: var(--color-primary); box-shadow: 0 0 8px var(--color-primary-glow); }
}
.strength-text {
  font-size: 11px; white-space: nowrap; transition: color .3s ease;
  &.weak { color: #ef4444; } &.medium { color: #f59e0b; }
  &.strong { color: #10b981; } &.very-strong { color: var(--color-primary); }
}

.stat-clickable {
  cursor: pointer;
  transition: transform var(--transition-fast);
  &:hover {
    transform: translateY(-2px);
    .stat-value { color: var(--color-primary); }
  }
}

.follow-list-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 8px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background var(--transition-fast);
  &:hover { background: var(--bg-input); }

  &__avatar {
    width: 40px; height: 40px; border-radius: 50%; object-fit: cover;
  }
  &__avatar-placeholder {
    width: 40px; height: 40px; border-radius: 50%;
    background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
    color: #fff; display: flex; align-items: center; justify-content: center;
    font-weight: 600; font-size: 16px;
  }
  &__name {
    font-weight: 500; color: var(--text-primary);
  }
}
</style>
