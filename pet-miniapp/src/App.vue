<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app'
import { useAuthStore } from '@/store/auth'

onLaunch(() => {
  useAuthStore().restore()
})
</script>

<style lang="scss">
@use "@/common/theme.scss";
page {
  background: #fffdf9;
  color: #2c2420;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 28rpx;
}
button::after { border: 0; }
</style>
