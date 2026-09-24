# Step 6 商家模块 API

服务在默认配置下使用 `/provider`、`/providers` 和 `/admin/providers` 路径。所有响应使用统一格式 `{ "code": 200, "message": "success", "data": ... }`。

## 商家申请与资料

| 方法 | 路径 | 身份 | 说明 |
| --- | --- | --- | --- |
| POST | `/provider/apply` | 已登录用户 | 创建一份待审核申请；同一用户只能申请一次 |
| GET | `/provider/me` | 申请人 | 查看自己的申请、审核状态与驳回原因 |
| PUT | `/provider/me` | 申请人 | 修改资料并重新进入待审核状态 |
| GET | `/providers?page=1&size=20&keyword=...` | 公开 | 分页查询已通过的商家，可按名称或地址搜索 |
| GET | `/providers/{id}` | 公开 | 查询已通过的商家详情 |

申请和修改使用相同 JSON：

```json
{
  "name": "暖心宠舍",
  "contactPhone": "13800138000",
  "address": "上海市徐汇区示例路 8 号",
  "latitude": 31.1888890,
  "longitude": 121.4377780,
  "description": "小型犬与猫寄养",
  "images": ["https://cos.example.com/provider/front.jpg"],
  "licenseImage": "https://cos.example.com/provider/license.jpg"
}
```

经纬度要同时提供或同时省略。图片字段只接受 HTTPS URL；本阶段保存已经上传到 COS 的地址，COS 直传授权接口将在文件上传模块实现。公开响应不包含营业资质图片、审核状态和驳回原因。商家修改已通过的资料后，公开列表立即隐藏该商家，直到再次审核通过。

## 审核

管理员使用普通微信账号登录，并由部署者在服务环境变量 `ADMIN_OPENIDS` 中配置允许审核的 openid，多个值以逗号分隔。未配置时，所有审核请求返回 403。JWT 的用户 ID 只用于查询服务端用户资料；客户端不能自行声明管理员身份。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/admin/providers/pending?page=1&size=20` | 查看待审核申请 |
| POST | `/admin/providers/{id}/audit` | 通过或驳回待审核申请 |

通过：

```json
{"approved": true}
```

驳回必须填写原因：

```json
{"approved": false, "reason": "请补充完整的经营资料"}
```

只有状态 0（待审核）能被审核。状态 1（已通过）、2（已驳回）重复审核返回 40901；并发审核用条件更新保证只成功一次。驳回后申请人可调用 `PUT /provider/me` 修改并重新提交。

## API 测试

1. 在微信小程序调用 `wx.login()`，将 code 发送到 `POST /auth/wechat/login`，取得 JWT。
2. 用 Bearer JWT 调用 `POST /provider/apply`；返回 `auditStatus: 0`，重复申请返回 HTTP 409。
3. 无 JWT 请求 `GET /providers`，申请中的商家不应出现；无 JWT 请求 `GET /providers/{id}` 返回 HTTP 404。
4. 将审核员 openid 配到 `ADMIN_OPENIDS` 并重启服务。审核员请求 `GET /admin/providers/pending`，再调用审核接口。非审核员请求同一接口返回 HTTP 403。
5. 审核通过后，公开列表和详情可读取商家资料，但不含 `licenseImage`。修改资料后重新变为待审核。

PowerShell 示例（`$token` 是微信登录接口返回值）：

```powershell
$headers = @{ Authorization = "Bearer $token" }
$body = @{
  name = "暖心宠舍"
  contactPhone = "13800138000"
  address = "上海市徐汇区示例路 8 号"
  images = @()
} | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8080/provider/apply -Headers $headers -ContentType 'application/json' -Body $body
Invoke-RestMethod -Uri 'http://localhost:8080/providers?page=1&size=20'
```

## 页面测试流程

小程序页面属于 Step 11，当前尚无页面可执行。页面接入后依次检查：登录后提交申请、查看待审核状态、被驳回后修改重提、审核通过后公开列表和详情可见、再次编辑后公开列表暂时隐藏。管理员审核入口应只对已配置审核员开放。