# Step 3–5 API 与测试说明

所有业务响应使用 `{"code":200,"message":"success","data":...}`；错误保留合适的 HTTP 状态码，并在响应体中给出业务码。示例中的 code 必须来自微信小程序 `wx.login()`，且只能使用一次。

## 接口

| 方法 | 路径 | 登录 | 说明 |
| --- | --- | --- | --- |
| GET | `/health` | 否 | 服务健康响应 |
| POST | `/auth/wechat/login` | 否 | 微信 code 登录 |
| GET | `/users/profile` | 是 | 当前用户资料 |
| POST | `/pets` | 是 | 新增宠物 |
| GET | `/pets` | 是 | 当前用户宠物列表 |
| GET | `/pets/{id}` | 是 | 查看自己的宠物 |
| PUT | `/pets/{id}` | 是 | 完整更新自己的宠物 |
| DELETE | `/pets/{id}` | 是 | 软删除自己的宠物 |

登录请求：

```json
{"code":"wx.login 返回的一次性 code"}
```

成功后 `data` 包含 `token`、`tokenType`（Bearer）、`expiresIn`（秒）和 `user`。服务端只保存 openid/可选 unionid，不向小程序返回微信 session_key。

宠物写入示例：

```json
{
  "name":"咪咪",
  "avatar":null,
  "type":"cat",
  "breed":"英短",
  "gender":2,
  "birthday":"2024-01-01T00:00:00",
  "weight":4.25,
  "vaccine":"已完成年度接种",
  "character":"亲人",
  "diet":"猫粮",
  "remark":null
}
```

`type` 目前允许 `dog`、`cat`、`other`。`gender`：0 未知、1 公、2 母。PUT 是完整更新：省略可选字段将清空该字段；省略 `gender` 会设为 0。删除使用软删除，历史订单仍可引用宠物记录。

## PowerShell 接口测试

启动 MySQL 8 和后端，准备真实微信 code 后：

```powershell
$base = "http://localhost:8080"
$wxCode = "填入 wx.login 返回的 code"
$login = Invoke-RestMethod -Uri "$base/auth/wechat/login" -Method Post -ContentType "application/json" -Body (@{ code = $wxCode } | ConvertTo-Json)
$headers = @{ Authorization = "Bearer $($login.data.token)" }
Invoke-RestMethod -Uri "$base/users/profile" -Headers $headers
$pet = Invoke-RestMethod -Uri "$base/pets" -Method Post -Headers $headers -ContentType "application/json" -Body (@{ name = "咪咪"; type = "cat"; gender = 2; weight = 4.25 } | ConvertTo-Json)
Invoke-RestMethod -Uri "$base/pets" -Headers $headers
Invoke-RestMethod -Uri "$base/pets/$($pet.data.id)" -Headers $headers
```

继续用同一 JWT 测试 PUT 和 DELETE；删除后对同一 ID 的 GET 应返回 HTTP 404、业务码 `40401`。另用第二个用户的 JWT 请求该 ID，也应返回 404，且无法修改。

## 错误码

| 业务码 | HTTP | 情形 |
| --- | --- | --- |
| 40001 | 400 | 请求参数无效 |
| 40029 | 400 | 微信 code 无效 |
| 40101 | 401 | 未登录或 JWT 无效 |
| 40301 | 403 | 账号被禁用 |
| 40401 | 404 | 宠物不存在、不属于当前用户或已删除 |
| 40901 | 409 | 唯一键或数据约束冲突 |
| 50201 | 502 | 微信接口暂不可用 |
| 50000 | 500 | 未预期的服务端错误 |

## 自动化测试

`mvn test` 覆盖 JWT 签名/验签、登录复用与禁用用户、宠物所有权条件以及 HTTP 层的统一错误响应。真实 MySQL 建表和微信接口联调需要具备外部服务的环境。小程序页面尚未开发，本阶段无页面测试流程。
